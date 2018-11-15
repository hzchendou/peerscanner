package com.hzchendou.model.packet;

import io.netty.buffer.ByteBuf;

/**
 * 网络包.
 *
 * @author hzchendou
 * @date 18-11-14
 * @since 1.0
 */
public interface Packet {

    int MAX_SIZE = 0x02000000;

    /**
     * 获取请求体数据
     *
     * @return
     */
    default byte[] getBody() {
        return new byte[0];
    }

    /**
     * 序列化
     *
     * @param buf
     */
    void serializePacket(ByteBuf buf);

    /**
     * 反序列化(主要针对请求体进行反序列化操作)
     *
     * @param body
     */
    void deserialize(byte[] body);
}
