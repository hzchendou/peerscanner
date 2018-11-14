package com.hzchendou.utils;

import java.io.IOException;
import java.math.BigInteger;

import io.netty.buffer.ByteBuf;

/**
 * 类型转换工具.
 *
 * @author hzchendou
 * @date 18-11-13
 * @since 1.0
 */
public class TypeUtils {

    /**
     * Parse 2 bytes from the byte array (starting at the offset) as unsigned 16-bit integer in little endian format.
     *
     * @param bytes
     * @param offset
     * @return
     */
    public static int readUint16(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
    }

    /**
     * Parse 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in little endian format.
     *
     * @param bytes
     * @param offset
     * @return
     */
    public static long readUint32(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffl) | ((bytes[offset + 1] & 0xffl) << 8) | ((bytes[offset + 2] & 0xffl) << 16) | (
                (bytes[offset + 3] & 0xffl) << 24);
    }

    /**
     * Parse 8 bytes from the byte array (starting at the offset) as signed 64-bit integer in little endian format.
     *
     * @param bytes
     * @param offset
     * @return
     */
    public static long readInt64(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffl) | ((bytes[offset + 1] & 0xffl) << 8) | ((bytes[offset + 2] & 0xffl) << 16) | (
                (bytes[offset + 3] & 0xffl) << 24) | ((bytes[offset + 4] & 0xffl) << 32) | ((bytes[offset + 5] & 0xffl)
                << 40) | ((bytes[offset + 6] & 0xffl) << 48) | ((bytes[offset + 7] & 0xffl) << 56);
    }

    /**
     * Write 4 bytes to the byte array (starting at the offset) as unsigned 32-bit integer in little endian format.
     *
     * @param val
     * @param out
     * @param offset
     */
    public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
    }

    /**
     * Write 4 bytes to the byte array (starting at the offset) as unsigned 32-bit integer in big endian format.
     *
     * @param val
     * @param out
     * @param offset
     */
    public static void uint32ToByteArrayBE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & (val >> 24));
        out[offset + 1] = (byte) (0xFF & (val >> 16));
        out[offset + 2] = (byte) (0xFF & (val >> 8));
        out[offset + 3] = (byte) (0xFF & val);
    }

    /**
     * Write 4 bytes to the output stream as unsigned 32-bit integer in little endian format.
     * (逆序写入)
     *
     * @param val
     * @param stream
     * @throws IOException
     */
    public static void uint32ToByteStreamLE(long val, ByteBuf stream) {
        stream.writeInt((int) (0xFF & val));
        stream.writeInt((int) (0xFF & (val >> 8)));
        stream.writeInt((int) (0xFF & (val >> 16)));
        stream.writeInt((int) (0xFF & (val >> 24)));
    }

    /**
     * 将64位无符号整数转为二进制，同时用0进行补全
     *
     * @param val
     * @param stream
     * @throws IOException
     */
    public static void uint64ToByteBufLE(BigInteger val, ByteBuf stream) {
        byte[] bytes = val.toByteArray();
        if (bytes.length > 8) {
            throw new RuntimeException("Input too large to encode into a uint64");
        }
        bytes = reverseBytes(bytes);
        stream.writeBytes(bytes);
        if (bytes.length < 8) {
            for (int i = 0; i < 8 - bytes.length; i++)
                stream.writeByte(0);
        }
    }

    /**
     * Write 2 bytes to the output stream as unsigned 16-bit integer in big endian format.
     *
     * @param val
     * @param stream
     * @throws IOException
     */
    public static void uint16ToByteStreamBE(int val, ByteBuf stream) {
        stream.writeInt((int) (0xFF & (val >> 8)));
        stream.writeByte((int) (0xFF & val));
    }

    /**
     * 对输入R数组进行逆反操作,eturns a copy of the given byte array in reverse order.
     *
     * @param bytes
     * @return
     */
    public static byte[] reverseBytes(byte[] bytes) {
        // We could use the XOR trick here but it's easier to understand if we don't. If we find this is really a
        // performance issue the matter can be revisited.
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            buf[i] = bytes[bytes.length - 1 - i];
        return buf;
    }

    /**
     * Write 2 bytes to the byte array (starting at the offset) as unsigned 16-bit integer in little endian format.
     *
     * @param val
     * @param out
     * @param offset
     */
    public static void uint16ToByteArrayLE(int val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
    }

    /**
     * Write 8 bytes to the byte array (starting at the offset) as signed 64-bit integer in little endian format.
     *
     * @param val
     * @param out
     * @param offset
     */
    public static void int64ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
        out[offset + 4] = (byte) (0xFF & (val >> 32));
        out[offset + 5] = (byte) (0xFF & (val >> 40));
        out[offset + 6] = (byte) (0xFF & (val >> 48));
        out[offset + 7] = (byte) (0xFF & (val >> 56));
    }

}
