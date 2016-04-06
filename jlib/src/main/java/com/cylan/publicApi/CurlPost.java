package com.cylan.publicApi;

import java.io.File;
import java.io.FileOutputStream;

public class CurlPost {
    public static final String DIR_NAME = "exception_picture";

    public static void post(String url, String path) {
        //Jni.UploadPath(url, path);
        // Jni.UploadPath(url, path + "test.jpg");
    }

    /**
     * 保存异常图片
     */
    public static void saveBitmap(File f, byte[] data) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(f);
            fos.write(data);
            fos.flush();
            fos.close();
            DswLog.w("save file " + f.getPath() + " succeed! size is " + data.length / 1024 + "kb");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String mkdirs(String dir) {
        File f = android.os.Environment.getExternalStorageDirectory();
        String path = f.getAbsolutePath() + "/" + Constants.ROOT_DIR + "/" + dir + "/";
        File cache = new File(path);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return path;
    }

    public static void RecursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }
}
