package com.cylan.jiafeigou.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by yzd on 16-12-9.
 */

public class FileUtils {

    public static void copyFile(File in, File out) {
        if (in == null || out == null || !in.canRead()) {
            return;
        }

        if (!out.getParentFile().exists()) out.getParentFile().mkdirs();

        FileInputStream fin = null;
        FileOutputStream fos = null;
        try {
            fin = new FileInputStream(in);
            fos = new FileOutputStream(out);
            byte[] buffer = new byte[1024];
            int len;
            while (((len = fin.read(buffer)) != -1)) {
                fos.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
