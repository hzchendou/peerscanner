package com.hzchendou.model.packet;

import static com.hzchendou.model.packet.PacketCommand.COMMAND_LENGTH;

import java.util.Arrays;

import com.hzchendou.utils.TypeUtils;
import com.hzchendou.utils.Sha256HashUtils;

import io.netty.buffer.ByteBuf;

/**
 * 网络包头部信息.
 * header(packetMagic(4) + command(12) + payload长度(4) + 校验码(4) )
 * packetMagic-(int)魔数表示链id,测试链以及正式链的id不相同
 * command-(string)命令，当前支持20种不同的命令（version,inv,block,getdata,tx,addr,ping,pong,verack,getblocks,getheaders,getaddr,headers,filterload,merkleblock,notfound,mempool,reject,getutxos,utxos,sendheaders）
 * payload-(object)消息体
 * 检验码-sha256(sha256(payload))
 *
 * @author hzchendou
 * @date 18-11-14
 * @since 1.0
 */
public class PacketHeader implements Packet {

    public static final long PACKET_MAGIC = 0xf9beb4d9L;


    public static final int HEADER_LENGTH = 4 + COMMAND_LENGTH + 4 + 4;

    /**
     * 4字节魔数
     */
    public long magic = 0xf9beb4d9L;

    /**
     * 命令符（最大为12字节，转为byte数组时,不足部分用\0补充）
     */
    public String command;

    /**
     * 请求体大小
     */
    public int size;

    /**
     * 请求体校验码
     */
    public byte[] checksum;

    public long getMagic() {
        return magic;
    }

    public void setMagic(long magic) {
        this.magic = magic;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }

    /**
     * 序列化
     *
     * @param buf
     */
    public void serializePacket(ByteBuf buf) {
        byte[] message = getBody();
        byte[] header = new byte[HEADER_LENGTH];
        TypeUtils.uint32ToByteArrayBE(magic, header, 0);
        // The header array is initialized to zero by Java so we don't have to worry about
        // NULL terminating the string here.
        for (int i = 0; i < command.length() && i < COMMAND_LENGTH; i++) {
            header[4 + i] = (byte) (command.codePointAt(i) & 0xFF);
        }
        //command
        TypeUtils.uint32ToByteArrayLE(message.length, header, 4 + COMMAND_LENGTH);
        //checksum
        byte[] hash = Sha256HashUtils.hashTwice(message);
        System.arraycopy(hash, 0, header, 4 + COMMAND_LENGTH + 4, 4);
        buf.writeBytes(header);
        buf.writeBytes(message);
    }

    /**
     * 获取请求体数据
     *
     * @return
     */
    public byte[] getBody() {
        return new byte[0];
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PacketHeader{");
        sb.append("magic=").append(magic);
        sb.append(", command='").append(command).append('\'');
        sb.append(", size=").append(size);
        sb.append(", checksum=").append(Arrays.toString(checksum));
        sb.append('}');
        return sb.toString();
    }
}
