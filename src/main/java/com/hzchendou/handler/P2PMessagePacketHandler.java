package com.hzchendou.handler;

import java.util.List;

import com.hzchendou.model.packet.MessagePacket;
import com.hzchendou.model.packet.message.AddressMessagePacket;
import com.hzchendou.model.packet.message.GetAddrMessage;
import com.hzchendou.model.packet.message.PeerAddress;
import com.hzchendou.model.packet.message.PingMessagePacket;
import com.hzchendou.model.packet.message.PongMessagePacket;
import com.hzchendou.model.packet.message.SendHeadersMessagePacket;
import com.hzchendou.model.packet.message.VersionAckMessagePacket;
import com.hzchendou.model.packet.message.VersionMessagePacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * p2p协议消息处理器.
 *
 * @author hzchendou
 * @date 18-11-13
 * @since 1.0
 */
public class P2PMessagePacketHandler extends ChannelHandlerAdapter {

    /**
     * 连接建立完成，发送version message
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("连接建立");
        sendMessagePacket(ctx, new VersionMessagePacket());
    }

    /**
     * 读取消息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof MessagePacket) {
            dealPacket(ctx, (MessagePacket) msg);
        } else {
            System.out.println("接收到未知消息");
        }
    }


    /**
     * 处理异常消息
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    /**
     * 处理包消息
     *
     * @param packet
     */
    public void dealPacket(ChannelHandlerContext ctx, MessagePacket packet) {
        //发送verack消息
        if (packet instanceof VersionMessagePacket) {
            VersionMessagePacket versionMessage = (VersionMessagePacket) packet;
            System.out.format("收到数据:%s, 客户端版本号:%s", versionMessage.command, versionMessage.subVer).println();
            sendMessagePacket(ctx, new VersionAckMessagePacket());
        } else if (packet instanceof VersionAckMessagePacket) {
            System.out.println("收到version应答消息");
            //发送获取地址消息
            sendMessagePacket(ctx, new GetAddrMessage());
        } else if (packet instanceof SendHeadersMessagePacket) {
            System.out.println("收到sendHeaders消息");
        } else if (packet instanceof PingMessagePacket) {
            System.out.format("收到ping消息:%s", ((PingMessagePacket) packet).getNonce()).println();
            if (((PingMessagePacket) packet).isHasNonce()) {
                sendMessagePacket(ctx, new PongMessagePacket(((PingMessagePacket) packet).getNonce()));
            }
        } else if (packet instanceof PongMessagePacket) {
            System.out.format("收到pong消息:%s", ((PongMessagePacket) packet).getNonce()).println();
        } else if (packet instanceof AddressMessagePacket) {
            AddressMessagePacket addrMessage = ((AddressMessagePacket) packet);
            List<PeerAddress> peerAddrs = addrMessage.getAddresses();
            System.out.format("收到addr消息, 地址数量%s， 地址：", addrMessage.addrSize());
            for (PeerAddress address : peerAddrs) {
                System.out.format("%s:%s", address.getAddr().getHostAddress(), address.getPort());
            }
            System.out.println();
        } else if (packet instanceof GetAddrMessage) {
            System.out.println("收到getAddr消息");
        } else {
            System.out.format("收到不支持数据:%s", packet.command).println();
        }
    }

    /**
     * 发送消息
     *
     * @param ctx
     * @param packet
     */
    private void sendMessagePacket(ChannelHandlerContext ctx, MessagePacket packet) {
        ByteBuf resp = Unpooled.buffer();
        packet.serializePacket(resp);
        ctx.write(resp);
        ctx.flush();
        System.out.format("完成%s消息发送", packet.command).println();
    }

}
