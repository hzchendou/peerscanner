package com.hzchendou.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;

import com.hzchendou.model.AbstractBitcoinMessage;
import com.hzchendou.model.VersionMessage;
import com.hzchendou.model.packet.PacketPayload;
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
        //        super.channelRead(ctx, msg);
        if (msg instanceof ByteBuf) {
            dealByteBuff(ctx, (ByteBuf) msg);
        } else if (msg instanceof PacketPayload) {
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
    public void dealPacket(ChannelHandlerContext ctx, PacketPayload packet) throws IOException {
        System.out.println("收到数据:" + packet.command);
        //发送verack消息
        if (Objects.equals("version", packet.command)) {
            sendVerAckPacket(ctx);
        }
    }

    /**
     * 处理消息
     *
     * @param ctx
     * @param msg
     * @throws IOException
     */
    public void dealByteBuff(ChannelHandlerContext ctx, ByteBuf msg) throws IOException {
        seekPastMagicBytes(msg);
        AbstractBitcoinMessage.BitcoinPacketHeader header =
                new AbstractBitcoinMessage.BitcoinPacketHeader((ByteBuf) msg);
        System.out.println("收到数据:" + header.command);
        //发送verack消息
        if (Objects.equals("version", header.command)) {
            sendVerAckPacket(ctx);
        }
    }

    /**
     * 跳过魔数
     *
     * @param in
     * @throws BufferUnderflowException
     */
    public void seekPastMagicBytes(ByteBuf in) throws BufferUnderflowException {
        int magicCursor = 3;  // Which byte of the magic we're looking for currently.
        while (true) {
            byte b = in.readByte();
            // We're looking for a run of bytes that is the same as the packet magic but we want to ignore partial
            // magics that aren't complete. So we keep track of where we're up to with magicCursor.
            byte expectedByte = (byte) (0xFF & AbstractBitcoinMessage.packetMagic >>> (magicCursor * 8));
            if (b == expectedByte) {
                magicCursor--;
                if (magicCursor < 0) {
                    // We found the magic sequence.
                    return;
                } else {
                    // We still have further to go to find the next message.
                }
            } else {
                magicCursor = 3;
            }
        }
    }

    public void sendVerAckPacket(ChannelHandlerContext ctx) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        VersionMessage message = new VersionMessage();
        message.serialize("verack", new byte[0], out);
        ByteBuf resp = Unpooled.copiedBuffer(out.toByteArray());
        ctx.write(resp);
        ctx.flush();
        System.out.println("完成verack消息发送:");
    }
}
