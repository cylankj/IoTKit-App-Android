package com.cylan.jiafeigou.ads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.widget.AdsTimerView;

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
        AdsStrategy.AdsDescription description = getIntent().getParcelableExtra(JConstant.KEY_ADD_DESC);
        if (description != null)//加载广告图片
            Glide.with(this)
                    .load(description.url)
                    .into(imvAdsPic);
    }

    @Override
    public void onBackPressed() {

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case JConstant.CODE_AD_FINISH:
                setResult(JConstant.CODE_AD_FINISH);
                finishExt();
                break;
        }
    }
}
