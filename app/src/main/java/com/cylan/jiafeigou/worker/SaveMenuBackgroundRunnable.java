package com.cylan.jiafeigou.worker;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.cylan.jiafeigou.activity.main.MyVideos;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.utils.BitmapUtil;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.PathGetter;


public class SaveMenuBackgroundRunnable implements Runnable {

    private Context mContext;
    private Handler mHandler;
    private Bitmap bitmap;

    public SaveMenuBackgroundRunnable(Context mContext, Handler mHandler, Bitmap bit) {
        this.mContext = mContext;
        this.mHandler = mHandler;
        this.bitmap = bit;
    }

    @Override
    public void run() {
        // 保存侧边栏背景图片
        final String mThemeUrl = PathGetter.getThemePicPath(mContext);
        if (bitmap == null)
            return;
        Bitmap mBigerbtm = BitmapUtil.cutBitmap(bitmap, (bitmap.getHeight() * DensityUtil.dip2px(mContext, ClientConstants.MENU_LAYOUT_WIDTH)) / DensityUtil.getScreenHeight(mContext),
                bitmap.getHeight());
        if (mBigerbtm == null)
            return;
        Bitmap mThemeBm = BitmapUtil.BoxBlurFilter(mBigerbtm);
        if (!mBigerbtm.isRecycled()) {
            mBigerbtm.recycle();
        }

        Boolean isSave = BitmapUtil.saveBitmap2file(mThemeBm, mThemeUrl);
        if (isSave) {
            mHandler.obtainMessage(MyVideos.HANDLER_TO_SET_THEME, mThemeBm).sendToTarget();
        }

    }

}
