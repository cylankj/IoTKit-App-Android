package com.cylan.jiafeigou.support.galleryfinal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.galleryfinal.model.PhotoInfo;
import com.cylan.jiafeigou.support.galleryfinal.widget.crop.CropImageActivity;
import com.cylan.jiafeigou.support.galleryfinal.widget.zoonview.ClipViewLayout;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;


/**
 * 头像裁剪Activity
 */
public class ClipImageActivity extends CropImageActivity implements View.OnClickListener {
    private static final int RESULT_CAMERA =2;
    private ClipViewLayout clipViewLayout1;
    private ClipViewLayout clipViewLayout2;
    private TextView btnCancel;
    private TextView btnOk;
    private int type;
    private Uri mCameraSaveUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_clip_image);
        type = getIntent().getIntExtra("type", 1);
        mTakePhotoAction = getIntent().getBooleanExtra("cameraAction", false);
        initView();
        if (mTakePhotoAction){
            takePhotoAction();
        }
    }

    /**
     * 初始化组件
     */
    public void initView() {
        clipViewLayout1 = (ClipViewLayout) findViewById(R.id.clipViewLayout1);
        clipViewLayout2 = (ClipViewLayout) findViewById(R.id.clipViewLayout2);
        btnCancel = (TextView) findViewById(R.id.btn_cancel);
        btnOk = (TextView) findViewById(R.id.bt_ok);
        //设置点击事件监听器
        btnCancel.setOnClickListener(this);
        btnOk.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        clipViewLayout1.setVisibility(View.VISIBLE);
        clipViewLayout2.setVisibility(View.GONE);
        //设置图片资源
        if (getIntent().getData() != null){
            clipViewLayout1.setImageSrc(getIntent().getData());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.bt_ok:
                generateUriAndReturn();
                break;
        }
    }

    /**
     * 生成Uri并且通过setResult返回给打开的activity
     */
    private void generateUriAndReturn() {
        //调用返回剪切图
        Bitmap zoomedCropBitmap;

        zoomedCropBitmap = clipViewLayout1.clip();

        if (zoomedCropBitmap == null) {
            Log.e("android", "zoomedCropBitmap == null");
            return;
        }
        Uri mSaveUri = Uri.fromFile(new File(getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg"));
        if (mSaveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = getContentResolver().openOutputStream(mSaveUri);
                if (outputStream != null) {
                    zoomedCropBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                }
            } catch (IOException ex) {
                Log.e("android", "Cannot open file: " + mSaveUri, ex);
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (mTakePhotoAction){
                Intent intent = new Intent();
                intent.setData(mCameraSaveUri);
                setResult(RESULT_CAMERA, intent);
                //TODO 保存URi地址
            }else {
                Intent intent = new Intent();
                intent.setData(mSaveUri);
                setResult(RESULT_OK, intent);
                //TODO 保存URi地址
            }
            finish();
        }
    }

    @Override
    protected void takeResult(PhotoInfo info) {
        File file = new File(info.getPhotoPath());
        clipViewLayout1.setImageSrc(Uri.fromFile(file));
        mCameraSaveUri = Uri.fromFile(file);
    }

    @Override
    public void setCropSaveSuccess(File file) {

    }

    @Override
    public void setCropSaveException(Throwable throwable) {

    }
}
