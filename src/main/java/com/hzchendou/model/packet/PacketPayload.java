package com.hzchendou.model.packet;

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

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
