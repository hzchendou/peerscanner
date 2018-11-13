package com.hzchendou.model;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;

import com.hzchendou.utils.TypeUtils;

/**
 * 对等地址.
 *
 * @author hzchendou
 * @date 18-11-13
 * @since 1.0
 */
public class PeerAddress {

    static final int MESSAGE_SIZE = 30;

    private InetAddress addr;
    private String hostname; // Used for .onion addresses
    private int port;
    private BigInteger services;
    private long time;
    protected int protocolVersion;

    public PeerAddress() {
    }

    public PeerAddress(InetAddress addr, String hostname, int port, BigInteger services, long time) {
        this.addr = addr;
        this.hostname = hostname;
        this.port = port;
        this.services = services;
        this.time = time;
    }

    /**
     * 内容编码
     *
     * @param stream
     * @throws IOException
     */
    public void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        TypeUtils.uint64ToByteStreamLE(services, stream);  // nServices.
        // Java does not provide any utility to map an IPv4 address into IPv6 space, so we have to do it by hand.
        byte[] ipBytes = addr.getAddress();
        if (ipBytes.length == 4) {
            byte[] v6addr = new byte[16];
            System.arraycopy(ipBytes, 0, v6addr, 12, 4);
            v6addr[10] = (byte) 0xFF;
            v6addr[11] = (byte) 0xFF;
            ipBytes = v6addr;
        }
        stream.write(ipBytes);
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
