package com.hzchendou.model.packet.message;

import com.hzchendou.enums.CommandTypeEnums;
import com.hzchendou.model.packet.MessagePacket;
import com.hzchendou.utils.TypeUtils;

import io.netty.buffer.ByteBuf;

/**
 * Ping消息.
 *
 * @author hzchendou
 * @date 18-11-15
 * @since 1.0
 */
public class PingMessagePacket extends MessagePacket {

    private long nonce;

    private boolean hasNonce;

    /**
     * Create a Ping with a nonce value.
     * Only use this if the remote node has a protocol version greater than 60000
     */
    public PingMessagePacket(long nonce) {
        this.command = CommandTypeEnums.PING.getName();
        this.nonce = nonce;
        this.hasNonce = true;
    }

    /**
     * Create a Ping without a nonce value.
     * Only use this if the remote node has a protocol version lower than or equal 60000
     */
    public PingMessagePacket() {
        this.command = CommandTypeEnums.PING.getName();
        this.hasNonce = false;
    }


    /**
     * 反序列化
     */
    @Override
    protected void deserialize() {
        try {
            nonce = readInt64();
            hasNonce = true;
        } catch(Exception e) {
            hasNonce = false;
        }
    }

    @Override
    public void serializePacket(ByteBuf buf) {
        if (hasNonce) {
            TypeUtils.int64ToByteBufLE(nonce, buf);
        }
    }

    public boolean isHasNonce() {
        return hasNonce;
    }

    public void setHasNonce(boolean hasNonce) {
        this.hasNonce = hasNonce;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}
