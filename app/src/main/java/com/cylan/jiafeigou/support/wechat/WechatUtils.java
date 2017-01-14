package com.cylan.jiafeigou.support.wechat;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by cylan-hunt on 16-10-28.
 */

public class WechatUtils {
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
