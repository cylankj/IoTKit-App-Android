package com.cylan.jiafeigou.utils;

/**
 * Created by hunt on 15-9-25.
 */

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 读取app本身的签名,读取外部apk的签名.可以校验下载下来的apk是否被窜改过,再加上MD5校验.就不会安装上被恶意修改的app.
 */
public class SignatureUtils {
    /**
     * @param context
     * @return
     */
    public static String getSignature(Context context) {
        String sig = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature sign = info.signatures[0];
            sig = sign == null ? null : sign.toCharsString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return sig;
    }

    /**
     * 从APK中读取签名
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String getSignaturesFromApk(File file) {
        List<String> signatures = new ArrayList<String>();
        try {
            JarFile jarFile = new JarFile(file);
            JarEntry je = jarFile.getJarEntry("AndroidManifest.xml");
            byte[] readBuffer = new byte[8192];
            Certificate[] certs = loadCertificates(jarFile, je, readBuffer);
            if (certs != null) {
                for (Certificate c : certs) {
                    String sig = toCharsString(c.getEncoded());
                    signatures.add(sig);
                }
            }
        } catch (IOException ex) {
            Log.e(SignatureUtils.class.getSimpleName(), "io exception : " + ex.toString());
        } catch (CertificateEncodingException e) {
            Log.e(SignatureUtils.class.getSimpleName(), "CertificateEncodingException : " + e.toString());
        }
        return signatures.size() == 0 ? null : signatures.get(0);
    }

    /**
     * 加载签名
     *
     * @param jarFile
     * @param je
     * @param readBuffer
     * @return
     */
    private static Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer) {
        try {
            InputStream is = jarFile.getInputStream(je);
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
            }
            is.close();
            return je != null ? je.getCertificates() : null;
        } catch (IOException e) {
        }
        return null;
    }


    /**
     * 将签名转成转成可见字符串
     *
     * @param sigBytes
     * @return
     */
    private static String toCharsString(final byte[] sigBytes) {
        final int N = sigBytes.length;
        final int N2 = N * 2;
        char[] text = new char[N2];
        for (int j = 0; j < N; j++) {
            byte v = sigBytes[j];
            int d = (v >> 4) & 0xf;
            text[j * 2] = (char) (d >= 10 ? ('a' + d - 10) : ('0' + d));
            d = v & 0xf;
            text[j * 2 + 1] = (char) (d >= 10 ? ('a' + d - 10) : ('0' + d));
        }
        return new String(text);
    }

    public static void readMD5Key() {
        PackageInfo info;
        try {
            Context context = ContextUtils.getContext();
            info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                Log.d("hash_key", "hash_key:" + MD5(signature.toByteArray()));
            }
        } catch (Exception e) {
        }
    }

    public static String MD5(byte[] data) {
        final char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            e.update(data);
            byte[] md = e.digest();
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; ++i) {
                byte b = md[i];
                str[k++] = hexDigits[b >>> 4 & 15];
                str[k++] = hexDigits[b & 15];
            }
            return new String(str);
        } catch (Exception var9) {
            var9.printStackTrace();
            return null;
        }
    }

}
