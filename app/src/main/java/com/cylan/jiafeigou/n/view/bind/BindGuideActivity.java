package com.cylan.jiafeigou.n.view.bind;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.ApFilter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.view.activity.ConfigWifiActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_BIND_DEVICE;

public class BindGuideActivity extends BaseFullScreenFragmentActivity {
    @BindView(R.id.imv_bind_guide)
    ImageView imvBindGuide;
    @BindView(R.id.tv_bind_guide_next)
    TextView tvBindGuideNext;
    @BindView(R.id.tv_guide_sub_content)
    TextView tvGuideSubContent;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.tv_guide_main_content)
    TextView tvGuideMainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_guide);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final String defaultAlias = getIntent().getStringExtra(KEY_BIND_DEVICE);
        int bind_guide_res = R.raw.bind_guide;
        if (TextUtils.equals(defaultAlias, getString(R.string.RuiShi_Name))) {
            //rs cam,在cylan包中,bind_guide_rs是一个空文件.这算是一个渠道包,只有doby才有改入口.
            bind_guide_res = R.raw.bind_guide_rs;
            tvGuideMainContent.setText(getString(R.string.WIFI_SET_RS));
        } else if (TextUtils.equals(defaultAlias, getString(R.string.DOG_CAMERA_NAME))) {
            //is cam
            tvGuideMainContent.setText(getString(R.string.WIFI_SET_3));
        } else {
            //default bell
            tvGuideMainContent.setText(getString(R.string.WIFI_SET_3_1));
        }
        tvGuideSubContent.setText(getString(R.string.WIFI_SET_4, getString(R.string.app_name)));
        GlideDrawableImageViewTarget imageViewTarget =
                new GlideDrawableImageViewTarget(imvBindGuide);
        Glide.with(this).load(bind_guide_res).into(imageViewTarget);
        customToolbar.setBackAction((View v) -> {
            finishExt();
        });
    }

    @Override
    public void onBackPressed() {
        finishExt();
    }

    @Override
    public void onResume() {
        super.onResume();
        tryLoadConfigApFragment();
    }

    @OnClick(R.id.tv_bind_guide_next)
    public void onClick() {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    private void tryLoadConfigApFragment() {
        final WifiInfo info = NetUtils.getWifiManager(ContextUtils.getContext()).getConnectionInfo();
        if (info == null || !ApFilter.accept(info.getSSID()) || NetUtils.getNetType(ContextUtils.getContext()) != ConnectivityManager.TYPE_WIFI) {
            AppLogger.i("bind: " + info);
            return;
        }
        if (NetUtils.getNetType(ContextUtils.getContext()) == ConnectivityManager.TYPE_WIFI && ApFilter.isAPMode(info.getSSID(), getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID))) {
            String panoramaConfigure = getIntent().getStringExtra("PanoramaConfigure");
            Bundle bundle = new Bundle();
            bundle.putString("PanoramaConfigure", panoramaConfigure);
            bundle.putBoolean("Success", true);
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID));
            ConfigPanoramaWiFiSuccessFragment newInstance = ConfigPanoramaWiFiSuccessFragment.newInstance(bundle);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    newInstance, android.R.id.content);
        } else {
            Intent intent = getIntent();
            intent.setClass(this, ConfigWifiActivity.class);
            intent.putExtra(JConstant.KEY_BIND_DEVICE, getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE));
            startActivity(intent);
            finish();
        }
    }
}
