package com.hzchendou.enums;

/**
 * 协议版本号.
 *
 * @author hzchendou
 * @date 18-11-14
 * @since 1.0
 */
public enum ProtocolVersion {

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
