package com.cylan.jiafeigou.utils;

import android.annotation.SuppressLint;

import com.google.api.client.util.Base64;

import java.net.URLEncoder;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 作者：zsl
 * 创建时间：2017/2/8
 * 描述：
 */
public class AESUtil {
    public static String key = "abcdefg";

    public static String encrypt(String src) throws Exception {
        byte[] rawKey = getRawKey(key.getBytes());
        byte[] result = encrypt(rawKey, src.getBytes());
        return toHex(result);
    }

    public static String decrypt(String encrypted) throws Exception {
        byte[] rawKey = getRawKey(key.getBytes());
        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(rawKey, enc);
        return new String(result);
    }

    @SuppressLint("TrulyRandom")
    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        // SHA1PRNG 强随机种子算法, 要区别4.2以上版本的调用方法
        SecureRandom sr = null;
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN) {
            sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        } else {
            sr = SecureRandom.getInstance("SHA1PRNG");
        }
        sr.setSeed(seed);
        kgen.init(256, sr);
        return kgen.generateKey().getEncoded();
    }

    private static byte[] encrypt(byte[] key, byte[] src) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(src);
    }

    private static byte[] decrypt(byte[] key, byte[] encrypted)
            throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(encrypted);
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(
                    hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }


    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";
     /*
     * 展示了一个生成指定算法密钥的过程 初始化HMAC密钥
     * @return
     * @throws Exception
     *
      public static String initMacKey() throws Exception {
      //得到一个 指定算法密钥的密钥生成器
      KeyGenerator KeyGenerator keyGenerator =KeyGenerator.getInstance(MAC_NAME);
      //生成一个密钥
      SecretKey secretKey =keyGenerator.generateKey();
      return null;
      }
     */

    /**
     * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
     *
     * @param encryptText 被签名的字符串
     * @param encryptKey  密钥
     * @return
     * @throws Exception
     */
    public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
        byte[] data = encryptKey.getBytes(ENCODING);
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        //生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);

        byte[] text = encryptText.getBytes(ENCODING);
        //完成 Mac 操作
        return mac.doFinal(text);
    }

    public static String sign(String reqPath, String ServiceKeySecret, String timestamp) throws Exception {
        String signature = "";
        try {
            byte[] data = ServiceKeySecret.getBytes("utf-8");
            SecretKey secretKey = new SecretKeySpec(data, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);
            String encryptText = reqPath + "\n" + timestamp;
            byte[] hmac = mac.doFinal(encryptText.getBytes("utf-8"));
            int hn, ln, cx;
            String hexDigitChars = "0123456789abcdef";
            StringBuffer buf = new StringBuffer(hmac.length * 2);
            for (cx = 0; cx < hmac.length; cx++) {
                hn = ((int) (hmac[cx]) & 0x00ff) / 16;
                ln = ((int) (hmac[cx]) & 0x000f);
                buf.append(hexDigitChars.charAt(hn));
                buf.append(hexDigitChars.charAt(ln));
            }
            hmac = buf.toString().getBytes();
            // Base64编码

            String base64 =  Base64.encodeBase64String(hmac);;
            System.out.println(base64);
            signature = URLEncoder.encode(base64, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signature;
    }
}
