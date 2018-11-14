package com.hzchendou.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import com.hzchendou.model.packet.PacketPayload;
import com.hzchendou.model.packet.message.VersionAckMessagePacket;
import com.hzchendou.model.packet.message.VersionMessagePacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 连接Version消息处理.
 *
 * @author hzchendou
 * @date 18-11-13
 * @since 1.0
 */
public class ConnectionVersionHandler extends ChannelHandlerAdapter {

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
        ByteBuf resp = Unpooled.buffer();
        VersionMessagePacket packet = new VersionMessagePacket();
        packet.serializePacket(resp);
        ctx.write(resp);
        ctx.flush();
        System.out.println("数据发送完成");
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
        if (msg instanceof PacketPayload) {
            dealPacket(ctx, (PacketPayload) msg);
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
    public void dealPacket(ChannelHandlerContext ctx, PacketPayload packet) {
        System.out.println("收到数据:" + packet.command);
        //发送verack消息
        if (Objects.equals("version", packet.command)) {
            sendVerAckPacket(ctx);
        } else {
            ByteBuf buf = Unpooled.buffer();
            packet.serializePacket(buf);
            ctx.write(buf);
            ctx.flush();
            System.out.format("发送数据: %s", packet.command).println();
        }
    }

    /**
     * 发送消息应答
     *
     * @param ctx
     * @throws IOException
     */
    public void sendVerAckPacket(ChannelHandlerContext ctx) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        VersionAckMessagePacket message = new VersionAckMessagePacket();
        ByteBuf resp = Unpooled.buffer();
        message.serializePacket(resp);
        ctx.write(resp);
        ctx.flush();
        System.out.println("完成verack消息发送:");
    }
}
