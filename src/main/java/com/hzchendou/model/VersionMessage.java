package com.hzchendou.model;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import com.google.common.net.InetAddresses;
import com.hzchendou.utils.Sha256HashUtils;
import com.hzchendou.utils.TypeUtils;

/**
 * 版本消息.
 *
 * @author hzchendou
 * @date 18-11-13
 * @since 1.0
 */
public class VersionMessage extends AbstractBitcoinMessage {

    /** The version of this library release, as a string. */
    public static final String BITCOINJ_VERSION = "0.15-SNAPSHOT";
    /** The value that is prepended to the subVer field of this application. */
    public static final String LIBRARY_SUBVER = "/bitcoinj:" + BITCOINJ_VERSION + "/";

    /**
     * The network address of the node receiving this message.
     */
    public PeerAddress receivingAddr;
    /**
     * The network address of the node emitting this message. Not used.
     */
    public PeerAddress fromAddr;

    /**
     * User-Agent as defined in <a href="https://github.com/bitcoin/bips/blob/master/bip-0014.mediawiki">BIP 14</a>.
     * Bitcoin Core sets it to something like "/Satoshi:0.9.1/".
     */
    public String subVer = "/bitcoinj:0.15-SNAPSHOT/PeerMonitor:1.0/";
    /**
     * How many blocks are in the chain, according to the other side.
     */
    public long bestHeight;
    /**
     * Whether or not to relay tx invs before a filter is received.
     * See <a href="https://github.com/bitcoin/bips/blob/master/bip-0037.mediawiki#extensions-to-existing-messages">BIP 37</a>.
     */
    public boolean relayTxesBeforeFilter;

    protected int length = Integer.MIN_VALUE;

    public VersionMessage() {
        InetAddress localhost = InetAddresses.forString("127.0.0.1");
        this.receivingAddr = new PeerAddress();
        receivingAddr.setAddr(localhost);
        receivingAddr.setPort(8333);
        receivingAddr.setProtocolVersion(clientVersion);
        receivingAddr.setServices(BigInteger.ZERO);
        this.fromAddr = new PeerAddress();
        fromAddr.setAddr(localhost);
        fromAddr.setPort(8333);
        fromAddr.setProtocolVersion(clientVersion);
        fromAddr.setServices(BigInteger.ZERO);

        subVer = LIBRARY_SUBVER;
        bestHeight = 0;

        relayTxesBeforeFilter = false;
    }

    public void bitcoinSerializeToStream(OutputStream buf) throws IOException {
        TypeUtils.uint32ToByteStreamLE(clientVersion, buf);
        TypeUtils.uint32ToByteStreamLE(localServices, buf);
        TypeUtils.uint32ToByteStreamLE(localServices >> 32, buf);
        TypeUtils.uint32ToByteStreamLE(time, buf);
        TypeUtils.uint32ToByteStreamLE(time >> 32, buf);
        receivingAddr.bitcoinSerializeToStream(buf);
        if (clientVersion >= 106) {
            fromAddr.bitcoinSerializeToStream(buf);
            // Next up is the "local host nonce", this is to detect the case of connecting
            // back to yourself. We don't care about this as we won't be accepting inbound
            // connections.
            TypeUtils.uint32ToByteStreamLE(0, buf);
            TypeUtils.uint32ToByteStreamLE(0, buf);
            // Now comes subVer.
            byte[] subVerBytes = subVer.getBytes(StandardCharsets.UTF_8);
            buf.write(new VarInt(subVerBytes.length).encode());
            buf.write(subVerBytes);
            // Size of known block chain.
            TypeUtils.uint32ToByteStreamLE(bestHeight, buf);
            buf.write(relayTxesBeforeFilter ? 1 : 0);
        }
    }



    /**
     * Writes message to to the output stream.
     */
    public void serialize(String name, byte[] message, OutputStream out) throws IOException {
        byte[] header = new byte[4 + COMMAND_LEN + 4 + 4 /* checksum */];
        uint32ToByteArrayBE(packetMagic, header, 0);

        // The header array is initialized to zero by Java so we don't have to worry about
        // NULL terminating the string here.
        for (int i = 0; i < name.length() && i < COMMAND_LEN; i++) {
            header[4 + i] = (byte) (name.codePointAt(i) & 0xFF);
        }

        TypeUtils.uint32ToByteArrayLE(message.length, header, 4 + COMMAND_LEN);

        byte[] hash = Sha256HashUtils.hashTwice(message);
        System.arraycopy(hash, 0, header, 4 + COMMAND_LEN + 4, 4);
        out.write(header);
        out.write(message);
    }

    /** Write 4 bytes to the byte array (starting at the offset) as unsigned 32-bit integer in big endian format. */
    public static void uint32ToByteArrayBE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & (val >> 24));
        out[offset + 1] = (byte) (0xFF & (val >> 16));
        out[offset + 2] = (byte) (0xFF & (val >> 8));
        out[offset + 3] = (byte) (0xFF & val);
    }

}
