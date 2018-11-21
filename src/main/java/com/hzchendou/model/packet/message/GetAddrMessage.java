package com.hzchendou.model.packet.message;

import com.hzchendou.enums.CommandTypeEnums;
import com.hzchendou.model.packet.MessagePacket;

/**
 * 获取地址消息.
 *
 * @author hzchendou
 * @date 18-11-16
 * @since 1.0
 */
public class GetAddrMessage extends MessagePacket {

    public GetAddrMessage() {
        this.command = CommandTypeEnums.GETADDR.getName();
    }
}
