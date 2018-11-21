package com.hzchendou.model.packet.message;

import com.hzchendou.enums.CommandTypeEnums;
import com.hzchendou.model.packet.MessagePacket;
import com.hzchendou.utils.TypeUtils;

import io.netty.buffer.ByteBuf;

/**
 * Pong消息.
 *
 * @author hzchendou
 * @date 18-11-16
 * @since 1.0
 */
public class PongMessagePacket extends MessagePacket {

    private long nonce;

    public PongMessagePacket() {
        this.command = CommandTypeEnums.PONG.getName();
    }

    public PongMessagePacket(long nonce) {
        this.command = CommandTypeEnums.PONG.getName();
        this.nonce = nonce;
    }

    /**
     * 反序列化
     */
    @Override
    protected void deserialize() {
        nonce = readInt64();
    }


    /**
     * 序列化
     *
     * @param buf
     */
    @Override
    public void serializePacket(ByteBuf buf) {
        TypeUtils.int64ToByteBufLE(nonce, buf);
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}
