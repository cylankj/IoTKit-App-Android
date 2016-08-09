package com.cylan.jiafeigou.n.view.bell;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BellLiveActivity extends ProcessActivity {

    @BindView(R.id.fLayout_bell_live_holder)
    FrameLayout fLayoutBellLiveHolder;
    @BindView(R.id.tv_bell_live_flow)
    TextView tvBellLiveFlow;
    @BindView(R.id.imgv_bell_live_switch_to_land)
    ImageView imgvBellLiveSwitchToLand;

    /**
     * 水平方向的view
     */
    private WeakReference<View> fLayoutLandHolderRef;


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

    @OnClick({R.id.imgv_bell_live_switch_to_land})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgv_bell_live_switch_to_land:
                initLandView();
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
                handleScreenUpdate(false);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                // 加入竖屏要处理的代码
                handleScreenUpdate(true);
                break;
        }
        final boolean isLandScape = this.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                handleSystemBar(!isLandScape, 100);
            }
        });
    }

    @Override
    public void onBackPressed() {
        final boolean isLandScape = this.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        if (isLandScape) {
            ViewUtils.setRequestedOrientation(this, Configuration.ORIENTATION_PORTRAIT);
            return;
        }
        finishExt();
    }

    private void handleScreenUpdate(final boolean port) {
        if (port) {
            ViewUtils.updateViewHeight(fLayoutBellLiveHolder, 0.75f);
            ViewUtils.setViewMarginStatusBar(tvBellLiveFlow);
            imgvBellLiveSwitchToLand.setVisibility(View.VISIBLE);
        } else {
            ViewUtils.updateViewMatchScreenHeight(fLayoutBellLiveHolder);
            ViewUtils.clearViewMarginStatusBar(tvBellLiveFlow);
            tvBellLiveFlow.bringToFront();
            imgvBellLiveSwitchToLand.setVisibility(View.GONE);
        }
        initLandView();
        fLayoutLandHolderRef.get()
                .setVisibility(port ? View.GONE : View.VISIBLE);
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

    /**
     * 初始化 Layer层view，横屏全屏时候，需要在上层
     */
    private void initLandView() {
        if (fLayoutLandHolderRef == null || fLayoutLandHolderRef.get() == null) {
            View view = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.layout_bell_live_land_layer, null);
            if (view != null) {
                fLayoutLandHolderRef = new WeakReference<>(view);
            }
        }
        View v = fLayoutBellLiveHolder.findViewById(R.id.fLayout_bell_live_land_layer);
        if (v == null) {
            fLayoutBellLiveHolder.addView(fLayoutLandHolderRef.get());
        }
    }
}
