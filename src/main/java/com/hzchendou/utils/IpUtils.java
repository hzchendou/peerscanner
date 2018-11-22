package com.hzchendou.utils;

/**
 * IP地址工具.
 *
 * @author hzchendou
 * @date 18-11-22
 * @since 1.0
 */
public class IpUtils {

    /**
     * 判断地址是否未IPv4格式
     *
     * @param data
     * @return
     */
    public static boolean isIpv4(byte[] data) {
        //如果地址长度是4个字节，或者前10个字节时00,11-12个字节是ff则为ipv4
        if (data.length == 4) {
            return true;
        }
        if (data.length == 16) {
            for (int i = 0; i < 12; i++) {
                if (i < 10) {
                    if (data[i] != (byte) 0x00) {
                        return false;
                    }
                } else {
                    if (data[i] != (byte) 0xff) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
