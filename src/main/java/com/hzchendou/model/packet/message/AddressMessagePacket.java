package com.hzchendou.model.packet.message;

import java.util.ArrayList;
import java.util.List;

import com.hzchendou.enums.CommandTypeEnums;
import com.hzchendou.model.VarInt;
import com.hzchendou.model.packet.MessagePacket;

import io.netty.buffer.ByteBuf;

/**
 * addr命令消息数据,该消息包含其它对等节点IP地址信息,
 * 使用这种方法,在P2P网络环境下各个节点之间可以相互发现，而不用过度依赖于DNS或者时IRC发现机制
 *
 * @author hzchendou
 * @date 18-11-16
 * @since 1.0
 */
public class AddressMessagePacket extends MessagePacket {

    /**
     * 最大地址数
     */
    private static final long MAX_ADDRESSED = 1024;

    /**
     * 地址
     */
    private List<PeerAddress> addresses;

    public AddressMessagePacket() {
        this.command = CommandTypeEnums.ADDR.getName();
    }

    /**
     * 反序列化
     */
    @Override
    protected void deserialize() {
        long numAddresses = readVarInt();
        //进行消息体积校验，防止巨量消息导致程序崩溃
        if (numAddresses > MAX_ADDRESSED) {
            throw new RuntimeException("Address message too large." + numAddresses);
        }
        addresses = new ArrayList<>((int) numAddresses);
        for (int i = 0; i < numAddresses; i++) {
            PeerAddress addr = PeerAddress.defaultPeerAddr(0).setNeedTime(Boolean.TRUE);
            byte[] addrBytes = new byte[PeerAddress.MESSAGE_SIZE];
            System.arraycopy(body, cursor, addrBytes, 0, addrBytes.length);
            addr.deserialize(addrBytes);
            addresses.add(addr);
            cursor += PeerAddress.MESSAGE_SIZE;
        }
    }

    /**
     * 序列化
     *
     * @param buf
     */
    @Override
    public void serializePacket(ByteBuf buf) {
        if (addresses != null) {
            buf.writeBytes(new VarInt(addresses.size()).encode());
            for (PeerAddress addr : addresses) {
                addr.serializePacket(buf);
            }
        }
    }

    /**
     * 地址数量
     *
     * @return
     */
    public int addrSize() {
        return addresses == null ? 0 : addresses.size();
    }

    public List<PeerAddress> getAddresses() {
        return addresses;
    }
}
