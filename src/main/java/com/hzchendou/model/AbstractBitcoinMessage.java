package com.hzchendou.model;

/**
 * 比特币消息.
 *
 * @author hzchendou
 * @date 18-11-13
 * @since 1.0
 */
public abstract class AbstractBitcoinMessage implements Message {

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

    public long packetMagic = 0xf9beb4d9L;

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
}
