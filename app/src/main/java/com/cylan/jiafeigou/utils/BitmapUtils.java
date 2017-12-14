package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class BitmapUtils {
    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable != null && drawable.getIntrinsicWidth() > 0 && drawable.getIntrinsicHeight() > 0) {
            Bitmap bitmap = Bitmap.createBitmap(

                    drawable.getIntrinsicWidth(),

                    drawable.getIntrinsicHeight(),

                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

                            : Bitmap.Config.RGB_565);

            Canvas canvas = new Canvas(bitmap);

            // canvas.setBitmap(bitmap);

            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

            drawable.draw(canvas);

            return bitmap;
        }
        return null;

    }

    public static Bitmap cutBitmap(Bitmap bitmap, int nwidth, int nHeight) {
        if (null == bitmap || nwidth > bitmap.getWidth() || nHeight > bitmap.getHeight()) {
            return null;
        }
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, nwidth, nHeight);
        return result;
    }

    /**
     * 高斯模糊
     */
    public static Bitmap BoxBlurFilter(Bitmap bmp) {
        /** 水平方向模糊度 */
        float hRadius = 10;
        /** 竖直方向模糊度 */
        float vRadius = 10;
        /** 模糊迭代度 */
        int iterations = 45;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < iterations; i++) {
            blur(inPixels, outPixels, width, height, hRadius);
            blur(outPixels, inPixels, height, width, vRadius);
        }
        blurFractional(inPixels, outPixels, width, height, hRadius);
        blurFractional(outPixels, inPixels, height, width, vRadius);
        bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


    public static void blur(int[] in, int[] out, int width, int height, float radius) {
        int widthMinus1 = width - 1;
        int r = (int) radius;
        int tableSize = 2 * r + 1;
        int divide[] = new int[256 * tableSize];

        for (int i = 0; i < 256 * tableSize; i++) {
            divide[i] = i / tableSize;
        }

        int inIndex = 0;

        for (int y = 0; y < height; y++) {
            int outIndex = y;
            int ta = 0, tr = 0, tg = 0, tb = 0;

            for (int i = -r; i <= r; i++) {
                int rgb = in[inIndex + clamp(i, 0, width - 1)];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for (int x = 0; x < width; x++) {
                out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb];

                int i1 = x + r + 1;
                if (i1 > widthMinus1) {
                    i1 = widthMinus1;
                }
                int i2 = x - r;
                if (i2 < 0) {
                    i2 = 0;
                }
                int rgb1 = in[inIndex + i1];
                int rgb2 = in[inIndex + i2];

                ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
                outIndex += height;
            }
            inIndex += width;
        }
    }

    public static void blurFractional(int[] in, int[] out, int width, int height, float radius) {
        radius -= (int) radius;
        float f = 1.0f / (1 + 2 * radius);
        int inIndex = 0;

        for (int y = 0; y < height; y++) {
            int outIndex = y;

            out[outIndex] = in[0];
            outIndex += height;
            for (int x = 1; x < width - 1; x++) {
                int i = inIndex + x;
                int rgb1 = in[i - 1];
                int rgb2 = in[i];
                int rgb3 = in[i + 1];

                int a1 = (rgb1 >> 24) & 0xff;
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = rgb1 & 0xff;
                int a2 = (rgb2 >> 24) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = rgb2 & 0xff;
                int a3 = (rgb3 >> 24) & 0xff;
                int r3 = (rgb3 >> 16) & 0xff;
                int g3 = (rgb3 >> 8) & 0xff;
                int b3 = rgb3 & 0xff;
                a1 = a2 + (int) ((a1 + a3) * radius);
                r1 = r2 + (int) ((r1 + r3) * radius);
                g1 = g2 + (int) ((g1 + g3) * radius);
                b1 = b2 + (int) ((b1 + b3) * radius);
                a1 *= f;
                r1 *= f;
                g1 *= f;
                b1 *= f;
                out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
                outIndex += height;
            }
            out[outIndex] = in[width - 1];
            inIndex += width;
        }
    }

    public static int clamp(int x, int a, int b) {
        return (x < a) ? a : (x > b) ? b : x;
    }


    /**
     * 放大图片，避免传入和生成的bitmap分辨率一致，否则容易导致RuntimeException
     *
     * @param bitmap original bitmap
     * @param w
     * @param h
     * @return bitma
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidht = ((float) w / width);
        float scaleHeight = ((float) h / height);
        if (scaleHeight == height && scaleWidht == width) {
            height += 1;
            width += 1;
        }
        matrix.postScale(scaleWidht, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            System.gc();
        }
        return newbmp;
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     *
     * @param context
     * @param resId
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Bitmap readBitMap(Context context, int resId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = false;
        options.inPurgeable = true;
        return BitmapFactory.decodeStream(context.getResources().openRawResource(resId), null, options);
    }


    public static Bitmap byte2bitmap(int w, int h, byte[] cursor) {
        int cursorPixcel[] = new int[w * h];
        int nLen = 0;
        for (int i = 0; i < w * h; i++) {
            int a, r, g, b;
            a = cursor[nLen + 3] & 0x000000ff;
            r = cursor[nLen + 2] & 0x000000ff;
            g = cursor[nLen + 1] & 0x000000ff;
            b = cursor[nLen + 0] & 0x000000ff;

            cursorPixcel[i] = (a << 24 | r << 16 | g << 8 | b);
            nLen += 4;
        }

        Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        newBitmap.setPixels(cursorPixcel, 0, w, 0, 0, w, h);
        return newBitmap;
    }

    public static boolean saveBitmap2file(Bitmap bmp, String filename) {
        PerformanceUtils.startTrace("saveBitmap2file:" + filename);
        CompressFormat format = CompressFormat.PNG;
        int quality = 50;
        boolean isSave;
        try {
            File file = new File(filename);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream stream = new FileOutputStream(filename);
            isSave = bmp.compress(format, quality, stream);
            stream.flush();
            stream.close();
            PerformanceUtils.stopTrace("saveBitmap2file:" + filename);
        } catch (Exception e) {
            PerformanceUtils.stopTrace("saveBitmap2file:" + filename);
            AppLogger.e(e);
            return false;
        }
        return isSave;
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static boolean equals(Drawable drawable0, Drawable drawable1) {
        if (drawable0 == null || drawable1 == null) {
            return false;
        }
        Drawable.ConstantState constantState0 = drawable0.getConstantState();
        Drawable.ConstantState constantState1 = drawable1.getConstantState();
        if (constantState0 == null || constantState1 == null) {
            return false;
        }
        return constantState0.equals(constantState1);
    }
}
