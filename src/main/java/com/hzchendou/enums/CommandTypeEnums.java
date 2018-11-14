package com.hzchendou.enums;

/**
 * 命令类型枚举类.
 *
 * @author hzchendou
 * @date 18-11-14
 * @since 1.0
 */
public enum CommandTypeEnums {
    VERSION("version"),
    INV("inv"),
    BLOCK("block"),
    GETDATA("getdata"),
    TX("tx"),
    ADDR("addr"),
    PING("ping"),
    PONG("pong"),
    VERACK("verack"),
    GETBLOCKS("getblocks"),
    GETHEADERS("getheaders"),
    GETADDR("getaddr"),
    HEADERS("headers"),
    FILTERLOAD("filterload"),
    MERKLEBLOCK("merkleblock"),
    NOTFOUND("notfound"),
    MEMPOOL("mempool"),
    REJECT("reject"),
    GETUTXOS("getutxos"),
    UTXOS("utxos"),
    SENDHEADERS("sendheaders");

    String name;

    CommandTypeEnums(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
