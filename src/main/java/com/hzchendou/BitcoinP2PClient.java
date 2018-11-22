package com.hzchendou;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.hzchendou.handler.P2PMessagePacketHandler;
import com.hzchendou.handler.codec.PacketDecoder;
import com.hzchendou.model.seed.DNSDiscovery;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * bitcoin客户端.
 *
 * @author hzchendou
 * @date 18-11-13
 * @since 1.0
 */
public class BitcoinP2PClient {

    private EventLoopGroup group;

    private Bootstrap bootstrap;

    //线程安全数列
    private List<ChannelFuture> channelFutures;

    public BitcoinP2PClient() {
        channelFutures = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * 初始化
     */
    public void init() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("decoder", new PacketDecoder());
                        ch.pipeline().addLast(new P2PMessagePacketHandler());
                    }
                });
    }

    /**
     * 连接管理
     *
     * @param address
     * @throws InterruptedException
     */
    public ChannelFuture connect(InetSocketAddress address) throws InterruptedException {
        ChannelFuture f = bootstrap.connect(address).sync();
        channelFutures.add(f);
        return f;
    }

    /**
     * 关闭
     */
    public void close() {
        if (channelFutures.size() > 0) {
            for (ChannelFuture future : channelFutures) {
                if (future.isDone()) {
                    continue;
                }
                future.channel().close();
            }
        }
        group.shutdownGracefully();
    }

    public static void main(String[] args) {
        BitcoinP2PClient p2PClient = new BitcoinP2PClient();
        p2PClient.init();
        InetSocketAddress[] address = DNSDiscovery.defaultDnsDiscovery().getPeers(0, 5000, TimeUnit.MILLISECONDS);
        if (address == null || address.length < 1) {
            System.out.println("暂无可用地址");
            p2PClient.close();
            return;
        }
        for (InetSocketAddress addr : address) {
//            if (addr.getAddress() instanceof Inet6Address) {
//                System.out.println("暂不支持IPV6地址连接,");
//                continue;
//            }
            System.out.format("connect to %s:%s", addr.getAddress().getHostAddress(), addr.getPort()).println();
            try {
                p2PClient.connect(addr);
            } catch (Exception e) {
                System.err.format("连接节点异常, %s", e.getMessage()).println();
            }
        }
        LockSupport.park();
    }
}
