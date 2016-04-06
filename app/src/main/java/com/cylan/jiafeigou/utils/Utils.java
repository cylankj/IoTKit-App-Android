package com.cylan.jiafeigou.utils;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.cylan.jiafeigou.base.MyApp;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.ProcessUtils;
import com.cylan.utils.entity.ZipLog;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

public class Utils {

    private static final long INITIALCRC = 0xFFFFFFFFFFFFFFFFL;
    private static long[] sCrcTable = new long[256];

    public static void closeIO(Closeable io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                DswLog.ex(e.toString());
            }
        }
    }

    public final static String[] SIZE_TAG = new String[]{"Bytes", "KB", "MB",
            "GB", "TB", "PB", "EB", "ZB", "YB"};

    public static final Format SIZEFORMAT = new DecimalFormat("#.##");
    public static final Format DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static String date2String(Format format, long date) {
        if (date >= 0) {
            return format.format(new Date(date));
        }
        return null;
    }


    public static String date2String(long date) {
        if (date >= 0) {
            return DATEFORMAT.format(new Date(date));
        }
        return null;
    }

    public static String size2String(long size) {

        if (size >= 0) {
            if (size == 0) {
                return SIZEFORMAT.format(size) + SIZE_TAG[0];
            }
            int index = (int) Math.floor(Math.log(size) / Math.log(1024));
            double result = size / Math.pow(1024, index);
            if (index < 0 || index > SIZE_TAG.length - 1) {
                return null;
            }
            return SIZEFORMAT.format(result) + SIZE_TAG[index];
        }
        return null;
    }

    public static final FilenameFilter SFILE_FILTER = new FilenameFilter() {

        public boolean accept(File dir, String filename) {
            return !(dir.isHidden() || filename.startsWith("."));
        }
    };

    private static String hardware;

    public static String getHardware(Context context) {
        if (hardware != null)
            return hardware;

        String id = android.provider.Settings.Secure.getString(
                context.getContentResolver(), "android_id");
        if (id == null)
            id = android.provider.Settings.System.getString(
                    context.getContentResolver(), "android_id");
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();

        String source = "";
        if (id != null) {
            source += id;
        }
        if (imei != null) {
            source += imei;
        }

        hardware = getMD5(source.getBytes());
        return hardware;
    }

    private static final char HEXDIGITS[] = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static MessageDigest sMESSAGEDIGEST;

    static {
        try {
            sMESSAGEDIGEST = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            DswLog.ex(e.toString());
        }
    }

    public static String getMD5(byte[] source) {
        if (sMESSAGEDIGEST == null) {
            return null;
        }
        sMESSAGEDIGEST.update(source);
        byte tmp[] = sMESSAGEDIGEST.digest();
        char str[] = new char[16 * 2];
        int k = 0;
        for (int i = 0; i < 16; i++) {
            byte byte0 = tmp[i];
            str[k++] = HEXDIGITS[byte0 >>> 4 & 0xf];
            str[k++] = HEXDIGITS[byte0 & 0xf];
        }
        return new String(str);
    }

    private static final String SEED = "jesus";
    private final static String HEX = "0123456789ABCDEFGHJKLMNOPQRSTUVWXYZzyxwvutsrqponmlkjhgfedcba";

    private final static String CHARSET = "UTF-8";

    public static String encrypt(String text) throws Exception {
        byte[] rawKey = getRawKey(SEED.getBytes(CHARSET));
        byte[] result = encrypt(rawKey, text.getBytes(CHARSET));
        return new String(Base64.encode(result, Base64.NO_PADDING), CHARSET);
    }

    public static String decrypt(String encrypted) throws Exception {
        byte[] rawKey = getRawKey(SEED.getBytes(CHARSET));
        byte[] enc = Base64.decode(encrypted.getBytes(CHARSET),
                Base64.NO_PADDING);
        byte[] result = decrypt(rawKey, enc);
        return new String(result, CHARSET);
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(seed);
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    private static Cipher initCipher(byte[] raw, int mode) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
        cipher.init(mode, skeySpec,
                new IvParameterSpec(new byte[cipher.getBlockSize()]));
        return cipher;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        return initCipher(raw, Cipher.ENCRYPT_MODE).doFinal(clear);
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted)
            throws Exception {
        return initCipher(raw, Cipher.DECRYPT_MODE).doFinal(encrypted);
    }

    protected static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    protected static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    protected static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
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

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

    public static String getVersion(Context context) {
        final PackageManager pm = context.getPackageManager();
        final String packageName = context.getPackageName();
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            DswLog.ex(e.toString());
        }
        return "";
    }

    // 15602997666=>156****7666
    public static String phoneNumchange(String phonenum) {
        try {
            String result;
            String start = (String) phonenum.subSequence(0, 3);
            int len = phonenum.length();
            String end = (String) phonenum.subSequence((len - 4), len);
            result = start + "****" + end;
            return result;
        } catch (Exception e) {
            DswLog.ex(e.toString());
            return phonenum;
        }
    }


    public static boolean isInner(Context mContext) {

        X500Principal DEBUG_DN = new X500Principal("CN=Android Debug,O=Android,C=US");

        boolean debuggable = false;
        try {
            PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature signatures[] = pinfo.signatures;
            for (int i = 0; i < signatures.length; i++) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                ByteArrayInputStream stream = new ByteArrayInputStream(signatures[i].toByteArray());
                X509Certificate cert = (X509Certificate) cf
                        .generateCertificate(stream);
                debuggable = cert.getSubjectX500Principal().equals(DEBUG_DN);
                if (debuggable)
                    break;
            }

        } catch (CertificateException e) {
        } catch (PackageManager.NameNotFoundException e) {
            DswLog.ex(e.toString());
        }
        return debuggable;
    }


    public static String getBundleId(Context context) {
        PackageInfo pis = null;
        try {
            pis = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            return isInner(context) ? "" : getMD5(pis.signatures[0].toByteArray());
        } catch (NameNotFoundException e) {
            DswLog.ex(e.toString());
        }
        return null;
    }


    public static String getCameraDefaultPath() {
        String Extpath = Environment.getExternalStorageDirectory().toString();
        String path = null;
        if (new File(Extpath).exists()) {
            path = Extpath + "/DCIM/Camera/jiafeigou/";
            if (!new File(path).exists()) {
                File file = new File(path);
                file.mkdirs();
            }
        }
        return path;
    }

    /**
     * 杩斿洖褰撳墠绋嬪簭鐗堟湰鍚�
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package cidData---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    /**
     * 妫�鏌TML 涓灏剧┖琛屾垨绌烘牸
     *
     * @param value
     * @return
     */
    public boolean checkBlank(String value) {
        return value.matches("^\\s*|\\s*$");
    }

    /**
     * 妫�鏌ヨ緭鍏ョ殑鏁版嵁涓槸鍚︽湁鐗规畩瀛楃
     *
     * @param qString 瑕佹鏌ョ殑鏁版嵁
     * @param regx    鐗规畩瀛楃姝ｅ垯琛ㄨ揪寮�
     * @return boolean 濡傛灉鍖呭惈姝ｅ垯琛ㄨ揪寮� <code> regx </code> 涓畾涔夌殑鐗规畩瀛楃锛岃繑鍥瀟rue锛�
     * 鍚﹀垯杩斿洖false
     */
    public static boolean hasCrossScriptRisk(String qString, String regx) {
        if (qString != null) {
            qString = qString.trim();
            Pattern p = Pattern.compile(regx, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(qString);
            return m.find();
        }
        return false;
    }

    public static String getAppPackageName(Context context) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            DswLog.ex(e.toString());
        }
        return info.packageName;
    }

    public static String executeHttpGet(String add) throws Throwable {
        String result = null;
        URL url = null;
        HttpURLConnection connection = null;
        InputStreamReader in = null;
        try {
            url = new URL(add);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            in = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(in);
            StringBuffer strBuffer = new StringBuffer();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strBuffer.append(line);
            }
            result = strBuffer.toString();
        } catch (Exception e) {
            throw e.fillInStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    DswLog.ex(e.toString());
                }
            }

        }
        return result;
    }

    /**
     * 鑾峰彇褰撳墠瀹㈡埛绔増鏈俊鎭�
     */
    public static int getCurrentVersion(Context mContext) {
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0);
            // curVersionName = cidData.versionName;
            return info.versionCode;
        } catch (NameNotFoundException e) {
            DswLog.ex(e.toString());
        }
        return 0;
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean isValid(String s) {
        return s != null && !s.equals("");
    }


    /**
     * @return
     */
    public static boolean isStorageLow() {
        return checkAvailableBlocks(0.1f);
    }

    public static long getMUnit() {
        return (1024 * 1024);
    }

    /**
     * get giga byte
     *
     * @return
     */
    public static long getGBUnit() {
        return (1024 * 1024 * 1024);
    }

    public static double getStorageAvailableSize() {
        File sdcardDir = new File(PathGetter.getUpgradePath());
        StatFs sf = new StatFs(sdcardDir.getAbsolutePath());
        return ((double) sf.getAvailableBlocks() * sf.getBlockSize())
                / getMUnit();
    }

    public static boolean isStorageFull() {
        File sdcardDir = new File(PathGetter.getUpgradePath());
        if (!sdcardDir.canWrite())
            return true;
        return checkAvailableBlocks(0.05f);
    }

    public static boolean checkAvailableBlocks(float offset) {
        File sdcardDir = new File(PathGetter.getUpgradePath());
        StatFs sf = new StatFs(sdcardDir.getAbsolutePath());
        return (float) sf.getAvailableBlocks() / sf.getBlockCount() < offset;
    }

    public static byte[] makeKey(String httpUrl) {
        return getBytes(httpUrl);
    }

    public static byte[] getBytes(String in) {
        byte[] result = new byte[in.length() * 2];
        int output = 0;
        for (char ch : in.toCharArray()) {
            result[output++] = (byte) (ch & 0xFF);
            result[output++] = (byte) (ch >> 8);
        }
        return result;
    }

    public static final long crc64Long(byte[] buffer) {
        long crc = INITIALCRC;
        for (int k = 0, n = buffer.length; k < n; ++k) {
            crc = sCrcTable[(((int) crc) ^ buffer[k]) & 0xff] ^ (crc >> 8);
        }
        return crc;
    }

    public static int getLanguageType(Context ctx) {
        Locale locale = ctx.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry().toLowerCase();
        if (language.endsWith("zh")) {
            if ("cn".equals(country)) {
                return Constants.LANGUAGE_TYPE_CHINESE;
            }
        } else if (language.endsWith("ru")) {
            return Constants.LANGUAGE_TYPE_RU;
        }
        return Constants.LANGUAGE_TYPE_ENGLISH;
    }


    // net type
    private static final int NET_OFFLINE = 0;
    private static final int NET_WIFI = 1;
    private static final int NET_3G = 2;

    public static int getNetType(Context ctx) {
        int type = NET_WIFI;
        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                type = NET_WIFI;
            } else {
                type = NET_3G;
            }
        }

        return type;
    }

    public static String getNetName(Context ctx) {
        WifiManager wm = (WifiManager) ctx.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo mConnWif = wm.getConnectionInfo();
        if (!StringUtils.isEmptyOrNull(mConnWif.getSSID())) {
            return mConnWif.getSSID().replaceAll("\"", "");
        }
        return "";
    }

    public static String getApplicationName(Context ctx) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = ctx.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(
                    ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName = (String) packageManager
                .getApplicationLabel(applicationInfo);
        return applicationName;
    }

    /**
     * @param path: whether path is valid and mkdir
     * @return
     */
    public static boolean isPathValid(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }


    public static String getSignature(String packagename, Context context) {
        PackageInfo pis = null;
        try {
            pis = context.getPackageManager().getPackageInfo(packagename, PackageManager.GET_SIGNATURES);
            return getMD5(pis.signatures[0].toByteArray());
        } catch (NameNotFoundException e) {
            DswLog.ex(e.toString());
        }
        return null;
    }


    public static void copyAssetFile(Context ctx, String filename) throws IOException {
        AssetManager assetManager = ctx.getAssets();
        InputStream in = null;
        OutputStream out = null;

        String newFileName = PathGetter.getUpgradePath() + "/" + filename;
        File uFile = new File(newFileName);
        if (uFile.exists())
            uFile.delete();
        in = assetManager.open(filename);
        out = new FileOutputStream(newFileName);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        in = null;
        out.flush();
        out.close();
        out = null;


    }

    /**
     * 比较版本号
     *
     * @param compareVersion
     * @param currentVersion
     * @return currentVersion>=compareVersion false : true
     */
    public static boolean isNoSurrortVersion(String compareVersion, String currentVersion) {
        if (compareVersion.equals(currentVersion))
            return false;
        String[] compare = compareVersion.split("\\.");
        String[] current = currentVersion.split("\\.");
        if (compare.length == 4 && current.length == 4) {
            for (int i = 0; i < current.length; i++) {
                if (Integer.parseInt(compare[i]) > Integer.parseInt(current[i])) {
                    return true;
                } else if (Integer.parseInt(compare[i]) < Integer.parseInt(current[i])) {
                    return false;
                } else {
                    continue;
                }
            }
        }
        return false;
    }

    public static String getlocalip(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if (ipAddress == 0) return null;
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }


    public static void sendBroad2System(Context ctx, String path) {
        File f = new File(path);
        try {
            MediaStore.Images.Media.insertImage(ctx.getContentResolver(), path, f.getName(), null);
        } catch (Exception e) {

            DswLog.ex(e.toString());
        }
        ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + f.getPath())));
    }

    public static boolean is5G(int freq) {
        return (freq > 4900 && freq < 5900);
    }

    public static boolean isServerRunning(Context mContext, String serviceName) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningServiceInfos
                = (ArrayList<ActivityManager.RunningServiceInfo>) manager.getRunningServices(100);
        for (ActivityManager.RunningServiceInfo info : runningServiceInfos) {
            String name = info.service.getClassName();
            if (serviceName.equals(name)) {
                return true;
            }
        }
        return false;
    }


    public static String getShortCountryName(Context ctx) {
        Locale locale = ctx.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry().toLowerCase();
        if (language.endsWith("zh")) {
            if ("cn".equals(country)) {
                return "zh-rCN";
            }
        } else if (language.endsWith("de")
                || language.endsWith("es")
                || language.endsWith("fr")
                || language.endsWith("ja")
                || language.endsWith("pt")
                || language.endsWith("ru")
                || language.endsWith("it")
                ) {
            return language;
        }
        return Locale.ENGLISH.getLanguage();
    }

    /**
     * get random int between min and max
     *
     * @param min
     * @param max
     * @return <ul>
     * <li>if min > max, return 0</li>
     * <li>if min == max, return min</li>
     * <li>else return random int between min and max</li>
     * </ul>
     */
    public static int getRandom(int min, int max) {
        if (min > max) {
            return 0;
        }
        if (min == max) {
            return min;
        }
        return min + new Random().nextInt(max - min);
    }

    public static boolean isRunOnBackground(Context cxt) {
        KeyguardManager mKeyguardManager = (KeyguardManager) cxt.getSystemService(Context.KEYGUARD_SERVICE);
        boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
        if (flag) {
            return true;
        } else {
            return isRunOnTask(cxt);
        }
    }

    public static boolean isRunOnTask(Context cxt) {
        final ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        return !cxt.getPackageName().equals(am.getRunningTasks(1).get(0).topActivity.getPackageName());
    }

    public static String getIMEI(Context ctx) {
        try {
            final TelephonyManager tm = (TelephonyManager) ctx.getSystemService(ctx.TELEPHONY_SERVICE);
            return tm.getDeviceId() == null ? "" : tm.getDeviceId();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isRecentBellCall(Context ctx, long callTime) {
        if (callTime == 0)
            return true;
        final long time = System.currentTimeMillis() / 1000 + PreferenceUtil.getKeyNtpTimeDiff(ctx);
        DswLog.e("MyService:current time: " + time + " callTime: " + callTime);
        return Math.abs(callTime - time) <= 15;
    }

    public static Vibrator getVibrator(Context context) {
        return (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
    }

    private static final String GET_LOG_TAG = "START_TO_GET_LOG:";
    private static final String GET_LOG_PRE = "START_TO_GET_LOG:";

    /**
     * 发送日志
     */
    public static void sendLog(String url) {
        DswLog.e(GET_LOG_TAG);
        DswLog.e(GET_LOG_PRE + ProcessUtils.myProcessName(MyApp.getContext()));
        DswLog.e(GET_LOG_PRE + Build.DISPLAY);
        DswLog.e(GET_LOG_PRE + Build.MODEL);
        DswLog.e(GET_LOG_PRE + Build.VERSION.SDK_INT + " " + Build.VERSION.RELEASE);
        ZipLog zipLog = new ZipLog(PathGetter.getRootPath());
        ArrayList<File> list = new ArrayList<>();
        zipLog.addFile(PathGetter.getWslogPath(), list); //DWSLog
        zipLog.addFile(PathGetter.getSmartCallPath(), list); //SmartCall
        zipLog.addFile(PathGetter.getBreakPadPath(), list);//breakpad
        zipLog.addFile(PathGetter.getCrashPath(), list);//crash
        zipLog.packZip(list);
        JniPlay.HttpPostFile(Constants.WEB_ADDR, Constants.WEB_PORT,
                url, zipLog.getZipDir());
        DswLog.e("Post log to server !");
    }

    /**
     * get the url of feedback
     *
     * @param content
     * @param sessid
     * @return
     */
    public static String getFeedbackUrl(String content, String sessid) {
        try {
            return "http://" + Constants.WEB_ADDR + ":" + Constants.WEB_PORT + "/index.php?mod=client&act=feedback&content=" + URLEncoder.encode(content, "UTF-8")
                    + "&sessid=" + sessid + "&sys_version=" + URLEncoder.encode(android.os.Build.VERSION.RELEASE, "UTF-8")
                    + "&model=" + URLEncoder.encode(android.os.Build.MODEL, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            DswLog.ex(e.toString());
            return "";
        }
    }

    /**
     * 上传日志地址
     *
     * @param sessid
     * @return
     */
    public static String getPostLogUrl(String sessid) {
        return "http://index.php?mod=client&act=log&sessid=" + sessid;
    }

    /**
     * 删除某个目录下的文件
     *
     * @param directory
     */
    public static void clearDirectoryFile(String directory) {
        File file = new File(directory);
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            for (int i = 0; i < childFiles.length; i++) {
                File f = childFiles[i];
                if (f.isFile()) {
                    f.delete();
                }
            }
        }
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String TruncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;
        strURL = strURL.trim().toLowerCase();
        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }
        return strAllParam;
    }

    /**
     * 解析出url参数中的键值对 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> URLRequest(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();
        String[] arrSplit = null;
        String strUrlParam = TruncateUrlPage(URL);
        if (strUrlParam == null) {
            return mapRequest;
        }
        // 每个键值为一组
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");
            // 解析出键值
            if (arrSplitEqual.length > 1) {
                // 正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            } else {
                if (arrSplitEqual[0] != "") {
                    // 只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

    /**
     * 把秒数转化成00:00
     *
     * @param sec
     * @return
     */
    public static String parse2Time(int sec) {
        return String.format("%02d", sec / 60) + String.format(":%02d", sec % 60);
    }


    public static String getAccountHeadPicUrl() {
        return "http://" + Constants.WEB_ADDR + ":" + Constants.WEB_PORT + "/index.php?mod=client&act=photo&sessid="
                + PreferenceUtil.getSessionId(MyApp.getContext());
    }

    /**
     * 防止控件被重复多次点击
     *
     * @param view
     */
    public static void disableView(final View view) {
        view.setClickable(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setClickable(true);
            }
        }, 1000);
    }
}
