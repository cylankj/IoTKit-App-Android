package com.cylan.jiafeigou.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.SmartCall;
import com.cylan.jiafeigou.activity.main.MyVideos;
import com.cylan.jiafeigou.engine.MyService;
import com.cylan.jiafeigou.presenter.SplashPresenter;
import com.cylan.jiafeigou.presenter.compl.SplashPresenterCompl;
import com.cylan.jiafeigou.view.SplashViewRequiredOps;
import com.cylan.jiafeigou.utils.BitmapUtil;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.support.DswLog;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-21
 * Time: 16:30
 */

public class SplashActivity extends Activity implements SplashViewRequiredOps {

    SplashPresenter.Ops mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mPresenter = new SplashPresenterCompl(this);
        initCache();
    }

    private void initCache() {
        if (StringUtils.isEmptyOrNull(PreferenceUtil.getVersionName(this))
                || !Utils.getAppVersionName(this).equals(PreferenceUtil.getVersionName(this))) {
            mPresenter.initCache();
        }else {
            setVersionName();
            loadSplashView();
        }

    }

    private void setVersionName() {
        PreferenceUtil.setVersionName(this, Utils.getAppVersionName(this));
    }

    private void loadSplashView() {
        final ImageView mSplashView = new ImageView(this);
        mSplashView.setScaleType(ImageView.ScaleType.FIT_XY);
        mSplashView.setImageBitmap(BitmapUtil.readBitMap(this, R.drawable.splash, DensityUtil.getScreenWidth(this), DensityUtil.getScreenHeight(this)));
        setContentView(mSplashView);
        mSplashView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        mPresenter.showTime();
    }


    private void startService() {
        Intent server = new Intent(this, MyService.class);
        startService(server);
        DswLog.i("MyService in SplashActivity");
    }

    private void startActivity() {
        if (!PreferenceUtil.getIsLogout(this) && !TextUtils.isEmpty(PreferenceUtil.getSessionId(this))) {
            startActivity(new Intent(this, MyVideos.class));
        } else {
            startActivity(new Intent(this, SmartCall.class));
        }
        finish();
    }


    @Override
    public void timeShowed() {
        startService();
        startActivity();
    }

    @Override
    public void cacheInited() {
        setVersionName();
        loadSplashView();
    }
}
