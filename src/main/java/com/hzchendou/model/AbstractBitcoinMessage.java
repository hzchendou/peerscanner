package com.hzchendou.model;

import static com.hzchendou.utils.TypeUtils.readUint32;

import java.nio.BufferUnderflowException;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;

/**
 * 比特币消息.
 *
 * @author hzchendou
 * @date 18-11-13
 * @since 1.0
 */
public abstract class AbstractBitcoinMessage implements Message {

    protected static final int COMMAND_LEN = 12;

    /**
     * 最大为32MB
     */
    public static final int MAX_SIZE = 0x02000000; // 32MB

    /**
     * The version number of the protocol spoken.
     */
    public int clientVersion = ProtocolVersion.CURRENT.bitcoinProtocol;
    /**
     * Flags defining what optional services are supported.
     */
    public long localServices = 0;
    /**
     * What the other side believes the current time to be, in seconds.
     */
    public long time = System.currentTimeMillis() / 1000;

    public static long packetMagic = 0xf9beb4d9L;

    public static enum ProtocolVersion {
        MINIMUM(70000),
        PONG(60001),
        BLOOM_FILTER(70000),
        WITNESS_VERSION(70012),
        CURRENT(70012);

        private final int bitcoinProtocol;

        ProtocolVersion(final int bitcoinProtocol) {
            this.bitcoinProtocol = bitcoinProtocol;
        }

        public int getBitcoinProtocolVersion() {
            return bitcoinProtocol;
        }
    }


    public static class BitcoinPacketHeader {
        /** The largest number of bytes that a header can represent */
        public static final int HEADER_LENGTH = COMMAND_LEN + 4 + 4;

        public final byte[] header;
        public final String command;
        public final int size;
        public final byte[] checksum;

        public BitcoinPacketHeader(ByteBuf in) throws BufferUnderflowException {
            header = new byte[HEADER_LENGTH];
            in.readBytes(header, 0, header.length);

            int cursor = 0;

            // The command is a NULL terminated string, unless the command fills all twelve bytes
            // in which case the termination is implicit.
            for (; header[cursor] != 0 && cursor < COMMAND_LEN; cursor++) ;
            byte[] commandBytes = new byte[cursor];
            System.arraycopy(header, 0, commandBytes, 0, cursor);
            command = new String(commandBytes, StandardCharsets.US_ASCII);
            cursor = COMMAND_LEN;

            size = (int) readUint32(header, cursor);
            cursor += 4;

            if (size > MAX_SIZE || size < 0)
                throw new RuntimeException("Message size too large: " + size);

            // Old clients don't send the checksum.
            checksum = new byte[4];
            // Note that the size read above includes the checksum bytes.
            System.arraycopy(header, cursor, checksum, 0, 4);
            cursor += 4;
        }
    }
}
