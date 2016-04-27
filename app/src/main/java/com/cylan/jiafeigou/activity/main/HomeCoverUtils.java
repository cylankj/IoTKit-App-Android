package com.cylan.jiafeigou.activity.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.cylan.publicApi.Constants;
import com.cylan.jiafeigou.utils.BitmapUtil;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import support.uil.core.DisplayImageOptions;
import support.uil.core.ImageLoader;
import support.uil.core.assist.ImageSize;
import support.uil.core.listener.SimpleImageLoadingListener;

public class HomeCoverUtils {

    private int mSceneid;
    private Handler mHandler;
    private ImageView mImageView;
    private Context mContext;
    private final static String TAG = "HomeCoverUtils";

    public HomeCoverUtils(Context context, Handler mHandler, int sceneid, ImageView mImageView) {
        this.mContext = context;
        this.mHandler = mHandler;
        this.mSceneid = sceneid;
        this.mImageView = mImageView;
        LoadImg();
    }

    //接下来需要做的工作:
    private void LoadImg() {
        final ImageSize mImageSize = new ImageSize(mImageView.getWidth(), mImageView.getHeight());

        //显示图片的配置
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        String url = "http://" + Constants.WEB_ADDR + ":" + Constants.WEB_PORT + "/index.php?sessid=" + PreferenceUtil.getSessionId(mContext)
                + "&mod=client&act=get_scene_image&scene_id=" + mSceneid;

        ImageLoader.getInstance().loadImage(url, mImageSize, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                if (TextUtils.equals(mImageView.getTag().toString(), mSceneid + "")) {
                    Bitmap bitmap = loadedImage;
                    if (bitmap.getWidth() < mImageView.getWidth()) {
                        bitmap = BitmapUtil.zoomBitmap(bitmap, mImageView.getWidth(), mImageView.getHeight());
                    }
//                    if (BitmapUtil.equals(mImageView.getDrawable(), new BitmapDrawable(mImageView.getResources(), bitmap))) {
//                        SLog.d(TAG, "is the same bitmap");
//                    }
                    mImageView.setImageDrawable(new BitmapDrawable(mImageView.getResources(), bitmap));
                    Message msg = mHandler.obtainMessage();
                    msg.what = MyVideos.HANDLER_HOMECOVER_COMPLETE;
                    msg.obj = bitmap;
                    mHandler.sendMessage(msg);
                }
            }
        });

    }

}