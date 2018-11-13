package com.hzchendou.constants;

/**
 * 比特币消息类型.
 *
 * @author hzchendou
 * @date 18-11-13
 * @since 1.0
 */
public interface BitcoinMessageType extends MessageType {

    String VERSION_MESSAGE = "version";

    String VERSION_ACK_MESSAGE = "verack";
}
