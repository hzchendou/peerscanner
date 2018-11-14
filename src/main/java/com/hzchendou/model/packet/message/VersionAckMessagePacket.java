package com.hzchendou.model.packet.message;

import com.hzchendou.enums.CommandTypeEnums;
import com.hzchendou.model.packet.PacketPayload;

/**
 * 版本消息应答.
 *
 * @author hzchendou
 * @date 18-11-14
 * @since 1.0
 */
public class VersionAckMessagePacket extends PacketPayload {

    public VersionAckMessagePacket() {
        command = CommandTypeEnums.VERACK.getName();
        setBody(new byte[0]);
    }
}
