package com.hzchendou.model.packet.message;

import static com.hzchendou.enums.ProtocolVersion.BLOOM_FILTER;

import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.google.common.net.InetAddresses;
import com.hzchendou.enums.CommandTypeEnums;
import com.hzchendou.enums.ProtocolVersion;
import com.hzchendou.model.VarInt;
import com.hzchendou.model.packet.MessagePacket;
import com.hzchendou.utils.TypeUtils;

import io.netty.buffer.ByteBuf;

/**
 * version消息.
 *
 * @author hzchendou
 * @date 18-11-14
 * @since 1.0
 */
public class VersionMessagePacket extends MessagePacket {

    /**
     * The version of this library release, as a string.
     */
    public static final String BITCOINJ_VERSION = "0.15-SNAPSHOT";
    /**
     * The value that is prepended to the subVer field of this application.
     */
    public static final String LIBRARY_SUBVER = "/bitcoinj:" + BITCOINJ_VERSION + "/";

    /**
     * A service bit that denotes whether the peer has a copy of the block chain or not.
     */
    public static final int NODE_NETWORK = 1 << 0;
    /**
     * A service bit that denotes whether the peer supports the getutxos message or not.
     */
    public static final int NODE_GETUTXOS = 1 << 1;
    /**
     * Indicates that a node can be asked for blocks and transactions including witness data.
     */
    public static final int NODE_WITNESS = 1 << 3;
    /**
     * A service bit used by Bitcoin-ABC to announce Bitcoin Cash nodes.
     */
    public static final int NODE_BITCOIN_CASH = 1 << 5;


    /**
     * 客户端版本号（The version number of the protocol spoken.）
     */
    private int clientVersion;

    /**
     * 本地服务（Flags defining what optional services are supported.）
     */
    private long localServices;

    /**
     * 本地时间戳,单位秒（What the other side believes the current time to be, in seconds.）
     */
    private long time;

    /**
     * The network address of the node receiving this message.
     */
    public PeerAddress receivingAddr;

    /**
     * The network address of the node emitting this message. Not used.
     */
    private PeerAddress fromAddr;

    /**
     * User-Agent as defined in <a href="https://github.com/bitcoin/bips/blob/master/bip-0014.mediawiki">BIP 14</a>.
     * Bitcoin Core sets it to something like "/Satoshi:0.9.1/".
     */
    public String subVer;

    /**
     * How many blocks are in the chain, according to the other side.
     */
    public long bestHeight;

    /**
     * Whether or not to relay tx invs before a filter is received.
     * See <a href="https://github.com/bitcoin/bips/blob/master/bip-0037.mediawiki#extensions-to-existing-messages">BIP 37</a>.
     */
    public boolean relayTxesBeforeFilter;

    /**
     * 构造函数
     */
    public VersionMessagePacket() {
        clientVersion = ProtocolVersion.CURRENT.getBitcoinProtocolVersion();
        localServices = 0;
        time = System.currentTimeMillis() / 1000;
        // Note that the Bitcoin Core doesn't do anything with these, and finding out your own external IP address
        // is kind of tricky anyway, so we just put nonsense here for now.
        InetAddress localhost = InetAddresses.forString("127.0.0.1");
        receivingAddr = new PeerAddress(localhost, 8333, clientVersion, BigInteger.ZERO);
        fromAddr = new PeerAddress(localhost, 8333, clientVersion, BigInteger.ZERO);
        subVer = LIBRARY_SUBVER;
        bestHeight = 0;
        relayTxesBeforeFilter = true;
        command = CommandTypeEnums.VERSION.getName();
    }

    /**
     * 序列化
     *
     * @param buf
     */
    @Override
    public void serializeToByteBuf(ByteBuf buf) {
        TypeUtils.uint32ToByteStreamLE(clientVersion, buf);
        TypeUtils.uint32ToByteStreamLE(localServices, buf);
        TypeUtils.uint32ToByteStreamLE(localServices >> 32, buf);
        TypeUtils.uint32ToByteStreamLE(time, buf);
        TypeUtils.uint32ToByteStreamLE(time >> 32, buf);
        receivingAddr.serializePacket(buf);
        if (clientVersion > 160) {
            fromAddr.serializePacket(buf);
            // Next up is the "local host nonce", this is to detect the case of connecting
            // back to yourself. We don't care about this as we won't be accepting inbound
            // connections.
            TypeUtils.uint32ToByteStreamLE(0, buf);
            TypeUtils.uint32ToByteStreamLE(0, buf);
            // Now comes subVer.
            byte[] subVerBytes = subVer.getBytes(StandardCharsets.UTF_8);
            buf.writeBytes(new VarInt(subVerBytes.length).encode());
            buf.writeBytes(subVerBytes);
            // Size of known block chain.
            TypeUtils.uint32ToByteStreamLE(bestHeight, buf);
            if (clientVersion > BLOOM_FILTER.getBitcoinProtocolVersion()) {
                buf.writeInt(relayTxesBeforeFilter ? 1 : 0);
            }
        }

    }

    /**
     * 反序列化
     */
    @Override
    public void deserialize() {
        clientVersion = (int) readUint32();
        localServices = readUint64().longValue();
        time = readUint64().longValue();
        receivingAddr = PeerAddress.defaultPeerAddr(0);
        byte[] addr = new byte[PeerAddress.MESSAGE_SIZE - 4];
        System.arraycopy(body, cursor, addr, 0,
                addr.length);
        receivingAddr.deserialize(addr);
        cursor += addr.length;
        if (clientVersion >= 106) {
            fromAddr = PeerAddress.defaultPeerAddr(0);
            addr = new byte[PeerAddress.MESSAGE_SIZE - 4];
            System.arraycopy(body, cursor, addr, 0,
                    addr.length);
            fromAddr.deserialize(addr);
            cursor += (PeerAddress.MESSAGE_SIZE - 4);
            // uint64 localHostNonce (random data)
            // We don't care about the localhost nonce. It's used to detect connecting back to yourself in cases where
            // there are NATs and proxies in the way. However we don't listen for inbound connections so it's
            // irrelevant.
            readUint64();
            // string subVer (currently "")
            subVer = readStr();
            // int bestHeight (size of known block chain).
            bestHeight = readUint32();
            if (clientVersion >= BLOOM_FILTER.getBitcoinProtocolVersion()) {
                relayTxesBeforeFilter = readBytes(1)[0] != 0;
            } else {
                relayTxesBeforeFilter = true;
            }
        } else {
            // Default values for flags which may not be sent by old nodes
            fromAddr = null;
            subVer = "";
            bestHeight = 0;
            relayTxesBeforeFilter = true;
        }
    }

    /**
     * Returns true if the version message indicates the sender has a full copy of the block chain,
     * or if it's running in client mode (only has the headers).
     */
    public boolean hasBlockChain() {
        return (localServices & NODE_NETWORK) == NODE_NETWORK;
    }

    /**
     * Returns true if the protocol version and service bits both indicate support for the getutxos message.
     */
    public boolean isGetUTXOsSupported() {
        return clientVersion >= 70002 && (localServices & NODE_GETUTXOS) == NODE_GETUTXOS;
    }

    /**
     * Returns true if a peer can be asked for blocks and transactions including witness data.
     */
    public boolean isWitnessSupported() {
        return (localServices & NODE_WITNESS) == NODE_WITNESS;
    }
}
