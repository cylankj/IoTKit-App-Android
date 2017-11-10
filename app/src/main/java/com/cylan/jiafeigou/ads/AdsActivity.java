package com.cylan.jiafeigou.ads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.widget.AdsTimerView;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AdsActivity extends BaseFullScreenFragmentActivity {

    @BindView(R.id.imv_ads_pic)
    ImageView imvAdsPic;
    @BindView(R.id.imv_ads_timer)
    AdsTimerView imvAdsTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads);
        ButterKnife.bind(this);
        imvAdsTimer.setBoomer(() -> {//跳转主页
            Intent intent = null;
            if (BaseApplication.getAppComponent().getSourceManager().getLoginState() == LogState.STATE_ACCOUNT_ON) {
                intent = new Intent(this, NewHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                //没有登录,跳转到登录页面.
                setResult(JConstant.CODE_AD_FINISH);
            }
            finishExt();
        });
        imvAdsTimer.startTimer();
        AdsStrategy.AdsDescription description = getIntent().getParcelableExtra(JConstant.KEY_ADD_DESC + JFGRules.getLanguageType());

        if (description != null)//加载广告图片
        {
            description.showCount++;
            PreferencesUtils.putString(JConstant.KEY_ADD_DESC + JFGRules.getLanguageType(), new Gson().toJson(description));
            //遇到网络不好的情况会出现白屏,因为图片还没下载,所以先下载图片.
            // TODO: 2017/11/10 GLIDE
//            Glide.with(this)
//                    .load(description.url)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .listener(new RequestListener<String, GlideDrawable>() {
//                        @Override
//                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                            AppLogger.e(MiscUtils.getErr(e));
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                            return false;
//                        }
//
//                    })
//                    .into(imvAdsPic);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        showSystemUI();
    }

    @OnClick({R.id.imv_ads_pic, R.id.imv_ads_timer})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imv_ads_pic: {
                Intent intent = getIntent();
                intent.setClass(this, AdsDetailActivity.class);
                startActivityForResult(intent, JConstant.CODE_AD_FINISH);
//                finishExt();
            }
            break;
            case R.id.imv_ads_timer:
                //跳转主页
                Intent intent = new Intent(this, NewHomeActivity.class);
                finishExt();
                if (BaseApplication.getAppComponent().getSourceManager().getLoginState() == LogState.STATE_ACCOUNT_ON) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    //没有登录,跳转到登录页面.
                    setResult(JConstant.CODE_AD_FINISH);
                }
                finishExt();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case JConstant.CODE_AD_FINISH:
                setResult(JConstant.CODE_AD_FINISH);
                finishExt();
                break;
        }
    }
}
