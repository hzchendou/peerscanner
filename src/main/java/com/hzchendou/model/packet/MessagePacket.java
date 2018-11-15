package com.hzchendou.model.packet;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import com.hzchendou.model.VarInt;
import com.hzchendou.utils.TypeUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 消息包.
 *
 * @author hzchendou
 * @date 18-11-15
 * @since 1.0
 */
public class MessagePacket extends MessagePacketHeader implements PacketPayload {

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

    /**
     * 反序列化
     *
     * @param body
     */
    @Override
    public void deserialize(byte[] body) {
        this.body = body;
        cursor = 0;
        deserialize();
    }

    /**
     * 反序列化
     */
    protected void deserialize() {

    }

    /**
     * 序列化
     *
     * @param buf
     */
    public void serializeToByteBuf(ByteBuf buf) {
    }

    /**
     * 信息拷贝
     *
     * @param packet
     */
    public void copyFrom(MessagePacket packet) {
        this.magic = packet.magic;
        this.command = packet.command;
        this.size = packet.size;
        this.checksum = packet.checksum;
    }

    /**
     * 读取4个字节转化为无符号int类型
     *
     * @return
     */
    protected long readUint32() {
        try {
            long u = readUint32(body, cursor);
            cursor += 4;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取8个字节，转化为长整形long
     *
     * @return
     */
    protected long readInt64() {
        try {
            long u = readInt64(body, cursor);
            cursor += 8;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取8个字节，转化为无符号整形
     *
     * @return
     */
    protected BigInteger readUint64() {
        // Java does not have an unsigned 64 bit type. So scrape it off the wire then flip.
        return new BigInteger(TypeUtils.reverseBytes(readBytes(8)));
    }

    /**
     * @return
     */
    protected long readVarInt() {
        return readVarInt(0);
    }

    protected long readVarInt(int offset) {
        try {
            VarInt varint = new VarInt(body, cursor + offset);
            cursor += offset + varint.getOriginalSizeInBytes();
            return varint.value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取指定长度字节数
     *
     * @param length
     * @return
     */
    protected byte[] readBytes(int length) {
        if ((length > MAX_SIZE) || (cursor + length > body.length)) {
            throw new RuntimeException("Claimed value length too large: " + length);
        }
        try {
            byte[] b = new byte[length];
            System.arraycopy(body, cursor, b, 0, length);
            cursor += length;
            return b;
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    protected byte[] readByteArray() {
        long len = readVarInt();
        return readBytes((int) len);
    }

    protected String readStr() {
        long length = readVarInt();
        return length == 0 ?
                "" :
                new String(readBytes((int) length), StandardCharsets.UTF_8); // optimization for empty strings
    }

    /**
     * 读取Hash值
     *
     * @return
     */
    protected byte[] readHash() {
        // We have to flip it around, as it's been read off the wire in little endian.
        // Not the most efficient way to do this but the clearest.
        return TypeUtils.reverseBytes(readBytes(32));
    }

    protected boolean hasMoreBytes() {
        return cursor < body.length;
    }
}
