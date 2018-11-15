package com.hzchendou.model.packet;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import com.hzchendou.model.VarInt;
import com.hzchendou.utils.TypeUtils;

/**
 * 包请求体.
 *
 * @author hzchendou
 * @date 18-11-14
 * @since 1.0
 */
public interface PacketPayload extends Packet {
    /**
     * 读取4个字节转化为无符号int类型
     *
     * @return
     */
    default long readUint32(byte[] body, int cursor) {
        try {
            long u = TypeUtils.readUint32(body, cursor);
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
    default long readInt64(byte[] body, int cursor) {
        try {
            long u = TypeUtils.readInt64(body, cursor);
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
    default BigInteger readUint64(byte[] body, int cursor) {
        // Java does not have an unsigned 64 bit type. So scrape it off the wire then flip.
        return new BigInteger(TypeUtils.reverseBytes(readBytes(body,cursor,8)));
    }

    /**
     * @return
     */
    default long readVarInt(byte[] body, int cursor) {
        return readVarInt(body, cursor, 0);
    }

    default long readVarInt(byte[] body, int cursor, int offset) {
        try {
            VarInt varint = new VarInt(body, cursor + offset);
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
    default byte[] readBytes(byte[] body, int cursor, int length) {
        if ((length > MAX_SIZE) || (cursor + length > body.length)) {
            throw new RuntimeException("Claimed value length too large: " + length);
        }
        try {
            byte[] b = new byte[length];
            System.arraycopy(body, cursor, b, 0, length);
            return b;
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    default byte[] readByteArray(byte[] body, int cursor) {
        long len = readVarInt(body, cursor);
        return readBytes(body, cursor, (int) len);
    }

    default String readStr(byte[] body, int cursor) {
        long length = readVarInt(body, cursor);
        return length == 0 ?
                "" :
                new String(readBytes(body, cursor, (int) length),
                        StandardCharsets.UTF_8);// optimization for empty strings
    }

    default boolean hasMoreBytes(byte[] body, int cursor) {
        return cursor < body.length;
    }
}
