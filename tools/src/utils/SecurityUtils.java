import utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

    private static final String ENCTYPE = "SHA-256";

    /*
     * 摘要加密函数.
     *
     * @param data: 加密数据
     *
     * @return: 密文数据
　　 */
    public static byte[] Encrypt(byte[] data) {
        MessageDigest md = null;
        try {
                md = MessageDigest.getInstance(ENCTYPE);
                md.update(data);
                return md.digest();
        } catch (NoSuchAlgorithmException e) {
                return null;
        }
    }
}
