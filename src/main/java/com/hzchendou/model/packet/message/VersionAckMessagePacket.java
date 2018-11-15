package com.hzchendou.model.packet.message;

import com.hzchendou.enums.CommandTypeEnums;
import com.hzchendou.model.packet.MessagePacket;

/**
 * 版本消息应答.
 *
 * @author hzchendou
 * @date 18-11-14
 * @since 1.0
 */
public class VersionAckMessagePacket extends MessagePacket {

    public VersionAckMessagePacket() {
        command = CommandTypeEnums.VERACK.getName();
    }
}
