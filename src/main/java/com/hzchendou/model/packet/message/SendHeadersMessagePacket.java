package com.hzchendou.model.packet.message;

import com.hzchendou.enums.CommandTypeEnums;
import com.hzchendou.model.packet.MessagePacket;

/**
 * sendHeaers消息.
 *
 * @author hzchendou
 * @date 18-11-15
 * @since 1.0
 */
public class SendHeadersMessagePacket extends MessagePacket {
    public SendHeadersMessagePacket() {
        command = CommandTypeEnums.VERACK.getName();
    }
}
