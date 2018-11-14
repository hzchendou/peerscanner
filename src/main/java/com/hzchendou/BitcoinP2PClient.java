package com.hzchendou;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.hzchendou.handler.ConnectionVersionHandler;
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

    public void connect(InetSocketAddress address) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).option(
                    ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("decoder", new PacketDecoder());
                    ch.pipeline().addLast(new ConnectionVersionHandler());
                }
            });

            ChannelFuture f = b.connect(address).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        InetSocketAddress[] address = DNSDiscovery.defaultDnsDiscovery().getPeers(0, 5000, TimeUnit.MILLISECONDS);
        System.out.format("connect to %s:%s", address[0].getAddress().getHostAddress(), address[0].getPort()).println();
        new BitcoinP2PClient().connect(address[0]);
    }
}
