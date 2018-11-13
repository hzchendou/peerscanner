package com.hzchendou.handler;

import java.io.ByteArrayOutputStream;

import com.hzchendou.model.VersionMessage;
import com.hzchendou.utils.UnsafeByteArrayOutputStream;

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
        //发送version 消息
        VersionMessage message = new VersionMessage();
        ByteArrayOutputStream stream = new UnsafeByteArrayOutputStream(150);
        message.bitcoinSerializeToStream(stream);
        byte[] buf = stream.toByteArray();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.serialize("version", buf, out);
        ByteBuf resp = Unpooled.copiedBuffer(out.toByteArray());
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        System.out.println("收到数据");
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
    }
}
