package com.hzchendou.model.packet;

import com.hzchendou.utils.TypeUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 包请求体.
 *
 * @author hzchendou
 * @date 18-11-14
 * @since 1.0
 */
public class PacketPayload extends PacketHeader {

    /**
     * 请求体数据
     */
    public byte[] body;

    // The cursor keeps track of where we are in the byte array as we parse it.
    // Note that it's relative to the start of the message payload NOT the start of the array.
    protected int cursor;

    /**
     * 获取请求体
     *
     * @return
     */
    @Override
    public byte[] getBody() {
        if (body == null) {
            ByteBuf byteBuf = Unpooled.buffer();
            serializeToByteBuf(byteBuf);
            if (byteBuf.readableBytes() > 0) {
                body = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(body, 0, body.length);
                byteBuf.clear();
            } else {
                body = new byte[0];
            }
            return body;
        }
        return body;
    }


    public void setBody(byte[] body) {
        this.body = body;
    }

    /**
     * 序列化
     *
     * @param buf
     */
    public void serializeToByteBuf(ByteBuf buf) {

    }

    /**
     * 反序列化
     */
    public void deserialize() {

    }


    protected long readUint32() {
        try {
            long u = TypeUtils.readUint32(body, cursor);
            cursor += 4;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
    }
}
