
package com.hzchendou.handler.codec;


import static com.hzchendou.utils.TypeUtils.readUint32;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.hzchendou.model.packet.Packet;
import com.hzchendou.model.packet.PacketCommand;
import com.hzchendou.model.packet.PacketHeader;
import com.hzchendou.model.packet.PacketPayload;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 解码器,比特币官方P2P通信协议由请求头以及请求体组成
 * header(packetMagic(4) + command(12) + payload长度(4) + 校验码(4) )
 * packetMagic-(int)魔数表示链id,测试链以及正式链的id不相同
 * command-(string)命令，当前支持20种不同的命令（version,inv,block,getdata,tx,addr,ping,pong,verack,getblocks,getheaders,getaddr,headers,filterload,merkleblock,notfound,mempool,reject,getutxos,utxos,sendheaders）
 * payload-(object)消息体
 * 检验码-sha256(sha256(payload))
 * payload(n)
 */
public final class PacketDecoder extends ByteToMessageDecoder {

    /**
     * 包数据解码
     *
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        decodeFrames(in, out);
    }

    /**
     * 解码帧数据
     *
     * @param in
     * @param out
     */
    private void decodeFrames(ByteBuf in, List<Object> out) {
        if (in.readableBytes() >= PacketHeader.HEADER_LENGTH) {
            //1.记录当前读取位置位置.如果读取到非完整的frame,要恢复到该位置,便于下次读取
            in.markReaderIndex();
            PacketPayload packet = decodeFrame(in);
            if (packet != null) {
                out.add(packet);
            } else {
                //2.读取到不完整的frame,恢复到最近一次正常读取的位置,便于下次读取
                in.resetReaderIndex();
            }
        }
    }

    /**
     * @param in
     * @return
     */
    private PacketPayload decodeFrame(ByteBuf in) {
        PacketPayload packet = decodePacketHeader(in);
        int bodyLength = packet.size;
        //没有请求体数据时，直接返回（例如verack消息）
        if (bodyLength == 0) {
            return packet;
        }
        if (bodyLength > Packet.MAX_SIZE) {
            throw new RuntimeException("packet body length over limit:" + bodyLength);
        }
        int readableBytes = in.readableBytes();
        if (readableBytes < bodyLength) {
            return null;
        }
        packet.body = new byte[bodyLength];
        in.readBytes(packet.body, 0, bodyLength);
        return packet;
    }

    /**
     * 解码包头数据
     *
     * @param in
     * @return
     */
    private PacketPayload decodePacketHeader(ByteBuf in) {
        PacketPayload packet = new PacketPayload();
        byte[] header = new byte[PacketHeader.HEADER_LENGTH];
        in.readBytes(header, 0, header.length);
        int offset = 0;
        //读取magic数据
        packet.magic = readUint32(header, offset);
        offset += 4;
        //读取command数据
        int cursor = offset;
        // The command is a NULL terminated string, unless the command fills all twelve bytes
        // in which case the termination is implicit.
        for (; header[cursor] != 0 && cursor < PacketCommand.COMMAND_LENGTH; cursor++)
            ;
        byte[] commandBytes = new byte[cursor - offset];
        System.arraycopy(header, offset, commandBytes, 0, cursor - offset);
        //此处需要注意,bitcoin中的command编码为ASCII
        packet.command = new String(commandBytes, StandardCharsets.US_ASCII);
        offset += PacketCommand.COMMAND_LENGTH;
        packet.size = (int) readUint32(header, offset);
        offset += 4;
        packet.checksum = new byte[4];
        // Note that the size read above includes the checksum bytes.
        System.arraycopy(header, offset, packet.checksum, 0, 4);
        return packet;
    }
}
