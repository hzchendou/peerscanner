package com.hzchendou.model.packet.message;

import java.util.ArrayList;
import java.util.List;

import com.hzchendou.enums.CommandTypeEnums;
import com.hzchendou.model.VarInt;
import com.hzchendou.model.packet.MessagePacket;
import com.hzchendou.utils.TypeUtils;

import io.netty.buffer.ByteBuf;

/**
 * inv消息.
 *
 * @author hzchendou
 * @date 18-11-15
 * @since 1.0
 */
public class InventoryMessage extends MessagePacket {
    public static final long MAX_INVENTORY_ITEMS = 50000;

    private long arrayLen;

    List<InventoryItem> items;


    public InventoryMessage() {
        this.command = CommandTypeEnums.INV.getName();
    }

    /**
     * 反序列化
     */
    @Override
    protected void deserialize() {
        arrayLen = readVarInt();
        if (arrayLen > MAX_INVENTORY_ITEMS) {
            throw new RuntimeException("Too many items in INV message: " + arrayLen);
        }

        // An inv is vector<CInv> where CInv is int+hash. The int is either 1 or 2 for tx or block.
        items = new ArrayList<>((int) arrayLen);
        for (int i = 0; i < arrayLen; i++) {
            if (cursor + InventoryItem.MESSAGE_LENGTH > body.length) {
                throw new RuntimeException("Ran off the end of the INV");
            }
            int typeCode = (int) readUint32();
            InventoryItem.Type type;
            // See ppszTypeName in net.h
            switch (typeCode) {
                case 0:
                    type = InventoryItem.Type.Error;
                    break;
                case 1:
                    type = InventoryItem.Type.Transaction;
                    break;
                case 2:
                    type = InventoryItem.Type.Block;
                    break;
                case 3:
                    type = InventoryItem.Type.FilteredBlock;
                    break;
                default:
                    throw new RuntimeException("Unknown CInv type: " + typeCode);
            }
            InventoryItem item = new InventoryItem(type, readHash());
            items.add(item);
        }
        body = null;
    }

    /**
     * 序列化
     *
     * @param buf
     */
    @Override
    public void serializePacket(ByteBuf buf) {
        buf.writeBytes(new VarInt(items.size()).encode());
        for (InventoryItem item : items) {
            TypeUtils.uint32ToByteBufLE(item.type.ordinal(), buf);
            buf.writeBytes(TypeUtils.reverseBytes(item.hash));
        }
    }
}
