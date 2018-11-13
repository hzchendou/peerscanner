package com.hzchendou.model.seed;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * DNS节点发现.
 *
 * @author hzchendou
 * @date 18-11-13
 * @since 1.0
 */
public class DnsSeedDiscovery {

    private final String hostname;
    private final Integer port;

    /**
     * 构造函数
     *
     * @param port
     * @param hostname
     */
    public DnsSeedDiscovery(Integer port, String hostname) {
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * 获取对等节点
     *
     * @param services
     * @param timeoutValue
     * @param timeoutUnit
     * @return
     */
    public InetSocketAddress[] getPeers(long services, long timeoutValue, TimeUnit timeoutUnit) {
        if (services != 0)
            throw new RuntimeException("DNS seeds cannot filter by services: " + services);
        try {
            InetAddress[] response = InetAddress.getAllByName(hostname);
            InetSocketAddress[] result = new InetSocketAddress[response.length];
            for (int i = 0; i < response.length; i++)
                result[i] = new InetSocketAddress(response[i], port);
            return result;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return hostname;
    }
}
