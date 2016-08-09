package com.cylan.jiafeigou.n.view.bell;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BellLiveActivity extends ProcessActivity {

    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.fLayout_bell_live_holder)
    FrameLayout fLayoutBellLiveHolder;
    @BindView(R.id.tv_bell_live_flow)
    TextView tvBellLiveFlow;
    @BindView(R.id.imgv_bell_live_switch_to_land)
    ImageView imgvBellLiveSwitchToLand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bell_live);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        ButterKnife.bind(this);
        initView();
        initTextData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.btn_back, R.id.imgv_bell_live_switch_to_land})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finishExt();
                break;
            case R.id.imgv_bell_live_switch_to_land:
                ViewUtils.setRequestedOrientation(this,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final int orientation = this.getResources().getConfiguration().orientation;
        switch (orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                // 加入横屏要处理的代码
                ViewUtils.updateViewMatchScreenHeight(fLayoutBellLiveHolder);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                // 加入竖屏要处理的代码
                ViewUtils.updateViewHeight(fLayoutBellLiveHolder, 0.75f);
                break;
        }
        final boolean isLandScape = orientation == Configuration.ORIENTATION_LANDSCAPE;
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                handleSystemBar(!isLandScape, 1000);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishExt();
    }

    private void initView() {
        ViewUtils.updateViewHeight(fLayoutBellLiveHolder, 0.75f);
        ViewUtils.setViewMarginStatusBar(tvBellLiveFlow);
    }

    private void initTextData() {
        Intent intent = getIntent();
        final String content = intent.getStringExtra("text");
        AppLogger.d("content: " + content);
    }

}
