package com.cylan.jiafeigou.ads;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
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
            Intent intent = new Intent(this, NewHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
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
                startActivity(intent);
                finishExt();
            }
            break;
            case R.id.imv_ads_timer:
                //跳转主页
                Intent intent = new Intent(this, NewHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finishExt();
                break;
        }
    }
}
