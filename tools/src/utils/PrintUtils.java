package utils;

public class PrintUtils {

    /*
     * 字节数组转打印格式.
     *
     * @param bts: 要打印的字节数组
     *
     * @return: 十六进制打印格式
     */
    public static String bytesToHex(byte[] bts) {
        String tmp = null;
        StringBuffer sbf = new StringBuffer();

        for (int i = 0; i < bts.length; i++) {
            if (i > 0 && i % 8 == 0)
                sbf.append(" ");

            if (i > 0 && i % 16 == 0) {
                sbf.append("\n");
            }
            if (i % 16 == 0)
                sbf.append(String.format("  0x%04x:  ", i));

            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1)
                sbf.append("0");

            sbf.append(tmp + " ");
        }

        return sbf.toString();
    }
}
