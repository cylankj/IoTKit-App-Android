package com.cylan.jiafeigou.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.support.DswLog;
import com.cylan.jiafeigou.activity.main.MyVideos;
import com.cylan.jiafeigou.engine.MyService;
import com.cylan.jiafeigou.utils.BitmapUtil;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.Utils;
import support.uil.core.ImageLoader;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-21
 * Time: 16:30
 */

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initCache();
        loadSplashView();
        startService();
    }

    private void initCache() {
        if (StringUtils.isEmptyOrNull(PreferenceUtil.getVersionName(this))
                || !Utils.getAppVersionName(this).equals(PreferenceUtil.getVersionName(this))) {
            ImageLoader.getInstance().getMemoryCache().clear();
            ImageLoader.getInstance().getDiskCache().clear();
            CacheUtil.clear();
        }
        PreferenceUtil.setVersionName(this, Utils.getAppVersionName(this));
    }

    private void loadSplashView() {
        final ImageView mSplashView = new ImageView(this);
        mSplashView.setScaleType(ImageView.ScaleType.FIT_XY);
        mSplashView.setImageBitmap(BitmapUtil.readBitMap(this, R.drawable.splash, DensityUtil.getScreenWidth(this), DensityUtil.getScreenHeight(this)));
        setContentView(mSplashView);
        mSplashView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity();
            }
        }, 3000);
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

}
