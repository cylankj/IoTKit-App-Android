package com.cylan.jiafeigou.worker;

import android.graphics.Bitmap;
import android.os.Handler;

import com.cylan.jiafeigou.utils.BitmapUtil;


/**
 * Created by hebin on 2015/10/16.
 */
public class SaveShotPhotoRunnable implements Runnable {

    private String mPath;
    private Bitmap mBitmap;
    private Handler mHandler;
    private int mWhat;
    private boolean isShot;

    //Bitmap bitmap, String path, Handler handler, int what,
    public SaveShotPhotoRunnable(Object... obj) {
        this.mBitmap = (Bitmap) obj[0];
        this.mPath = (String) obj[1];
        this.mHandler = (Handler) obj[2];
        this.mWhat = (int) obj[3];
        if (obj.length > 4)
            isShot = (boolean) obj[4];
    }


    @Override
    public void run() {
        Boolean isSave = BitmapUtil.saveBitmap2file(mBitmap, mPath);
        if (isSave) {
            SaveShotPhoto ssp = new SaveShotPhoto();
            ssp.mPath = mPath;
            ssp.isShot = isShot;
            mHandler.obtainMessage(mWhat, ssp).sendToTarget();
        }
    }

    public static class SaveShotPhoto {
        public String mPath;
        public boolean isShot;
    }

}
