package com.cylan.jiafeigou.support.galleryfinal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineClipImageContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineClipImagePresenterImp;
import com.cylan.jiafeigou.support.galleryfinal.model.PhotoInfo;
import com.cylan.jiafeigou.support.galleryfinal.widget.crop.CropImageActivity;
import com.cylan.jiafeigou.support.galleryfinal.widget.zoonview.ClipViewLayout;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 头像裁剪Activity
 */
public class ClipImageActivity extends CropImageActivity implements View.OnClickListener,MineClipImageContract.View {

    private ClipViewLayout clipViewLayout1;
    private ClipViewLayout clipViewLayout2;
    private TextView btnCancel;
    private TextView btnOk;
    private Uri mCameraSaveUri;
    private Bitmap zoomedCropBitmap;
    private RelativeLayout rl_upload_hint;

    private MineClipImageContract.Presenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_clip_image);
        mTakePhotoAction = getIntent().getBooleanExtra("cameraAction", false);
        initView();
        initPresenter();
        if (mTakePhotoAction){
            takePhotoAction();
        }
    }

    private void initPresenter() {
        presenter = new MineClipImagePresenterImp(this);
    }

    /**
     * 初始化组件
     */
    public void initView() {
        clipViewLayout1 = (ClipViewLayout) findViewById(R.id.clipViewLayout1);
        clipViewLayout2 = (ClipViewLayout) findViewById(R.id.clipViewLayout2);
        btnCancel = (TextView) findViewById(R.id.btn_cancel);
        btnOk = (TextView) findViewById(R.id.bt_ok);
        rl_upload_hint = (RelativeLayout) findViewById(R.id.rl_upload_pro_hint);
        //设置点击事件监听器
        btnCancel.setOnClickListener(this);
        btnOk.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //if (presenter != null)presenter.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        clipViewLayout1.setVisibility(android.view.View.VISIBLE);
        clipViewLayout2.setVisibility(View.GONE);
        //设置图片资源
        if (getIntent().getData() != null){
            clipViewLayout1.setImageSrc(getIntent().getData());
        }
    }

    /**
     * 生成Uri并且通过setResult返回给打开的activity
     */
    private void generateUriAndReturn() {
        //调用返回剪切图
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
                PreferencesUtils.putString(JConstant.USER_IMAGE_HEAD_URL, mCameraSaveUri.getPath());
                //presenter.upLoadUserHeadImag(mCameraSaveUri.getPath());
            }else {
                PreferencesUtils.putString(JConstant.USER_IMAGE_HEAD_URL, mSaveUri.getPath());
                //presenter.upLoadUserHeadImag(mSaveUri.getPath());
            }
            finish();
        }
    }

    @Override
    protected void takeResult(PhotoInfo info) {
        if (info == null){
            finish();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (zoomedCropBitmap != null){
            zoomedCropBitmap.recycle();
        }
/*
        if (presenter != null){
            presenter.stop();
        }*/
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

    @Override
    public void showUpLoadPro() {
        rl_upload_hint.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideUpLoadPro() {
        rl_upload_hint.setVisibility(View.INVISIBLE);
    }

    @Override
    public void upLoadResultView(int code) {
        if (code == JError.ErrorOK){
            ToastUtil.showPositiveToast("上传成功");
        }else {
            ToastUtil.showPositiveToast("上传失败");
        }
    }

    @Override
    public void setPresenter(MineClipImageContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return null;
    }
}
