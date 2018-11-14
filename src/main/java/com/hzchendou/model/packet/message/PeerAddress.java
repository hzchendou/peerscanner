package com.hzchendou.model.packet.message;

import java.math.BigInteger;
import java.net.InetAddress;

import com.hzchendou.utils.TypeUtils;

import io.netty.buffer.ByteBuf;

/**
 * 对等节点地址信息.
 * (services(8) + addr(16) + port(4))
 *
 * @author hzchendou
 * @date 18-11-14
 * @since 1.0
 */
public class PeerAddress {

    static final int MESSAGE_SIZE = 30;

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
     * 序列化,转换为二进制
     *
     * @return
     */
    public void serialzeToByte(ByteBuf stream) {
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
}
