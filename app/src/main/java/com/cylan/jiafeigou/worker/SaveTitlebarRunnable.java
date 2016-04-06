package com.cylan.jiafeigou.worker;

import android.content.Context;
import android.graphics.Bitmap;

import com.cylan.jiafeigou.utils.BitmapUtil;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.PathGetter;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

public class SaveTitlebarRunnable implements Runnable {

    private Context mContext;
    private Bitmap bitmap;
    private String mTitlebarUrl;

    public SaveTitlebarRunnable(Context mContext, Bitmap bit) {
        this.mContext = mContext;
        this.bitmap = bit;
        mTitlebarUrl = PathGetter.getBgTitleBarPath(mContext);
        String imageUrl = ImageDownloader.Scheme.FILE.wrap(mTitlebarUrl);
        MyImageLoader.removeFromCache(imageUrl);
    }

    @Override
    public void run() {
        if (bitmap == null)
            return;
        Bitmap mTitlebarBm = BitmapUtil.cutBitmap(bitmap, bitmap.getWidth(), DensityUtil.dip2px(mContext, 48));

        if (mTitlebarBm == null)
            return;
        Bitmap mTitlebar = BitmapUtil.BoxBlurFilter(mTitlebarBm);
        if (!mTitlebarBm.isRecycled()) {
            mTitlebarBm.recycle();
        }

        BitmapUtil.saveBitmap2file(mTitlebar, mTitlebarUrl);
    }

}