package com.cylan.jiafeigou.support.photoselect;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineClipImageContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineClipImagePresenterImp;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


/**
 * 头像裁剪Activity
 */
public class ClipImageActivity extends AppCompatActivity implements MineClipImageContract.View, View.OnClickListener {
    private ClipViewLayout clipViewLayout1;
    private ClipViewLayout clipViewLayout2;
    private TextView btnCancel;
    private TextView btnOk;
    private int type;
    private Handler handler;
    private MineClipImageContract.Presenter presenter;
    private Uri mSaveUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_image);
        type = getIntent().getIntExtra("type", 1);
        initView();
        initPresenter();
    }

    private void initPresenter() {
        handler = new Handler();
        presenter = new MineClipImagePresenterImp(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
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
        if (type == 1) {
            clipViewLayout1.setVisibility(View.VISIBLE);
            clipViewLayout2.setVisibility(View.GONE);
            //设置图片资源
            clipViewLayout1.setImageSrc(getIntent().getData());
        } else {
            clipViewLayout2.setVisibility(View.VISIBLE);
            clipViewLayout1.setVisibility(View.GONE);
            //设置图片资源
            clipViewLayout2.setImageSrc(getIntent().getData());
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
        if (type == 1) {
            zoomedCropBitmap = clipViewLayout1.clip();
        } else {
            zoomedCropBitmap = clipViewLayout2.clip();
        }
        if (zoomedCropBitmap == null) {
            Log.e("android", "zoomedCropBitmap == null");
            return;
        }
        mSaveUri = Uri.fromFile(new File(getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg"));
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

            if (presenter != null) {
                showUpLoadPro();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        presenter.upLoadUserHeadImag(mSaveUri.getPath());
                    }
                }, 2000);
            }
        }
    }

    @Override
    public void showUpLoadPro() {
        LoadingDialog.showLoading(getSupportFragmentManager(), getString(R.string.Tap3_Uploading));
    }

    @Override
    public void hideUpLoadPro() {
        LoadingDialog.dismissLoading(getSupportFragmentManager());
    }

    @Override
    public void upLoadResultView(int code) {
        if (code == 200) {
            Intent intent = new Intent();
            intent.setData(mSaveUri);
            setResult(RESULT_OK, intent);
        } else {
            ToastUtil.showNegativeToast(getString(R.string.Tap3_UploadingFailed));
        }
        finish();
    }

    @Override
    public void setPresenter(MineClipImageContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return null;
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideUpLoadPro();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
    }
}
