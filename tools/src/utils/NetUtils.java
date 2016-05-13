package utils;

import java.net.InetAddress;

public class NetUtils {
    /*
     * IP字符串转长整形,实现c语言中的inet_aton功能.
     *
     * @param ipStr: IP地址字符串
     *
     * @return: IP长整形
     */
    public static long inetAton(String ipStr) throws Exception {
        byte[] bAddr = InetAddress.getByName(strIP).getAddress();
        long netIP = (bAddr[0] & 0xff) +
                     ((bAddr[1] & 0xff) << 8) +
                     ((bAddr[2] & 0xff) << 16) +
                     ((bAddr[3] & 0xff) << 24);
        return netIP;
    }

}
