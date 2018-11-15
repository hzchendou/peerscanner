package com.hzchendou.model.packet.message;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.hzchendou.model.packet.MessagePacket;
import com.hzchendou.model.packet.Packet;
import com.hzchendou.model.packet.PacketPayload;
import com.hzchendou.utils.TypeUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 对等节点地址信息.
 * (services(8) + addr(16) + port(4))
 *
 * @author hzchendou
 * @date 18-11-14
 * @since 1.0
 */
public class PeerAddress implements PacketPayload, Packet {

    static final int MESSAGE_SIZE = 30;

    public static final int UNKNOWN_LENGTH = Integer.MIN_VALUE;

    protected int length = UNKNOWN_LENGTH;

    protected MessagePacket parent;

    /**
     * 地址
     */
    private InetAddress addr;

    /**
     * 主机名（Used for .onion addresses）
     */
    private String hostname;

    /**
     * 端口号
     */
    private int port;

    /**
     * 提供服务标识
     */
    private BigInteger services;

    /**
     * 协议版本号
     */
    private int protocolVersion;

    /**
     * 时间戳
     */
    private long time;

    /**
     * 构造函数
     *
     * @param addr
     * @param port
     * @param protocolVersion
     * @param service
     */
    public PeerAddress(InetAddress addr, int port, int protocolVersion, BigInteger service) {
        this.addr = addr;
        this.port = port;
        this.services = service;
        this.protocolVersion = protocolVersion;
    }

    /**
     * 构建默认节点地址
     *
     * @param protocolVersion
     * @return
     */
    public static PeerAddress defaultPeerAddr(int protocolVersion) {
        return new PeerAddress(null, -1, protocolVersion, BigInteger.ZERO);
    }


    /**
     * 序列化
     *
     * @param stream
     */
    @Override
    public void serializePacket(ByteBuf stream) {
        TypeUtils.uint64ToByteBufLE(services, stream);
        // Java does not provide any utility to map an IPv4 address into IPv6 space, so we have to do it by hand.
        byte[] ipBytes = addr.getAddress();
        if (ipBytes.length == 4) {
            byte[] v6addr = new byte[16];
            System.arraycopy(ipBytes, 0, v6addr, 12, 4);
            v6addr[10] = (byte) 0xFF;
            v6addr[11] = (byte) 0xFF;
            ipBytes = v6addr;
        }
        stream.writeBytes(ipBytes);
        // And write out the port. Unlike the rest of the protocol, address and port is in big endian byte order.
        TypeUtils.uint16ToByteStreamBE(port, stream);
    }

    /**
     * 反序列化
     *
     * @param body
     */
    @Override
    public void deserialize(byte[] body) {
        // Format of a serialized address:
        //   uint32 timestamp
        //   uint64 services   (flags determining what the node can do)
        //   16 bytes ip address
        //   2 bytes port num
        int cursor = 0;
        if (isSerializeTime()) {
            time = readUint32(body, cursor);
            time += 4;
        } else {
            time = -1;
        }
        services = readUint64(body, cursor);
        cursor += 8;

        byte[] addrBytes = readBytes(body, cursor, 16);
        cursor += 16;
        try {
            addr = InetAddress.getByAddress(addrBytes);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
        port = TypeUtils.readUint16BE(body, cursor);
        // The 4 byte difference is the uint32 timestamp that was introduced in version 31402
        length = isSerializeTime() ? MESSAGE_SIZE : MESSAGE_SIZE - 4;

    }

    /**
     * 获取请求体数据
     *
     * @return
     */
    @Override
    public byte[] getBody() {
        ByteBuf byteBuf = Unpooled.buffer();
        serializePacket(byteBuf);
        return byteBuf.array();
    }

    /**
     * 是否需要序列化时间
     *
     * @return
     */
    private boolean isSerializeTime() {
        return protocolVersion >= 31402 && !(parent instanceof VersionMessagePacket);
    }


    public InetAddress getAddr() {
        return addr;
    }

    public void setAddr(InetAddress addr) {
        this.addr = addr;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public BigInteger getServices() {
        return services;
    }

    public void setServices(BigInteger services) {
        this.services = services;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public MessagePacket getParent() {
        return parent;
    }

    public void setParent(MessagePacket parent) {
        this.parent = parent;
    }
}


