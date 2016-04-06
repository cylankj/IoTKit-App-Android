package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

/**
 * Created by HeBin on 2015/6/18.
 */
public class MyImageLoader {

    public static void loadTitlebarImage(Context ctx, ImageView img) {
        String mTitlebarUrl = PathGetter.getBgTitleBarPath(ctx);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        String imageUrl = ImageDownloader.Scheme.FILE.wrap(mTitlebarUrl);
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(imageUrl, img, options);
    }

    public static void removeFromCache(String url) {
        MemoryCacheUtils.removeFromCache(url, com.nostra13.universalimageloader.core.ImageLoader.getInstance().getMemoryCache());
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().getDiskCache().remove(url);
    }

    public static void loadImageFromNet(String url, ImageView img) {
        //显示图片的配置
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(url, img, options);

    }

    public static void loadMsgImageFromNet(String url, ImageView img) {
        //显示图片的配置
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnFail(R.drawable.image_load_failed)
                .build();
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(url, img, options);

    }

    public static void loadImageFromDrawable(String drawable, ImageView img) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(drawable, img, options);
    }

    public static void loadWelcomeBitmap(int res, ImageView img) {
        String imageUrl = ImageDownloader.Scheme.DRAWABLE.wrap(res + "");
        loadImageFromDrawable(imageUrl, img);
    }

}
