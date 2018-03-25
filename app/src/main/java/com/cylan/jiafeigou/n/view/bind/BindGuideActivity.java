package com.cylan.jiafeigou.n.view.bind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.ApFilter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.view.activity.Config4GActivity;
import com.cylan.jiafeigou.n.view.activity.ConfigWifiActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    @BindView(R.id.iv_explain_gray)
    ImageView ivExplainGray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_guide);
        ButterKnife.bind(this);
        if (getIntent().hasExtra(JConstant.KEY_BIND_DEVICE_ALIAS)
                && TextUtils.equals(getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE_ALIAS),
                getString(R.string._720PanoramicCamera))) {
            ivExplainGray.setVisibility(View.VISIBLE);
            ViewUtils.setViewMarginStatusBar(ivExplainGray);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        int bind_guide_res = getIntent().getIntExtra(JConstant.KEY_CONNECT_AP_GIF, -1);
        String content = getIntent().getStringExtra(JConstant.KEY_SSID_PREFIX);
        tvGuideMainContent.setText(getString(R.string.WIFI_SET_VER, content));
        tvGuideSubContent.setText(getString(R.string.WIFI_SET_4, getString(R.string.app_name)));
        GlideApp.with(this).load(bind_guide_res).into(imvBindGuide);
        customToolbar.setBackAction((View v) -> finishExt());
    }

    @Override
    public boolean performBackIntercept(boolean willExit) {
        finishExt();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        tryLoadConfigApFragment();
    }

    @OnClick(R.id.tv_bind_guide_next)
    public void onClick() {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        if (autoBack == null) {
            autoBack = new AutoBack(this, getIntent());
        }
        autoBack.run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoBack != null) {
            autoBack.cancel();
        }
    }

    private AutoBack autoBack;

    private static class AutoBack {
        private Br br;
        private WifiManager wifiManager;
        private WeakReference<BindGuideActivity> weakReference;
        private Intent activityIntent;

        public AutoBack(BindGuideActivity activity, Intent intent) {
            weakReference = new WeakReference<>(activity);
            this.activityIntent = intent;
            wifiManager = NetUtils.getWifiManager(ContextUtils.getContext());
        }

        private class Br extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiInfo info = wifiManager.getConnectionInfo();
                if (info != null && info.getSupplicantState() == SupplicantState.COMPLETED) {
                    final String ssidName = NetUtils.getNetName(context);
                    AppLogger.d("能够跳转？" + ApFilter.accept(ssidName));
                    if (ApFilter.accept(ssidName)) {
                        if (weakReference.get() != null) {
                            Intent toIntent = null;
                            if (activityIntent == null) {
                                toIntent = new Intent(weakReference.get(),
                                        BindGuideActivity.class);
                            } else {
                                toIntent = activityIntent;
                                toIntent.setClass(weakReference.get(),
                                        BindGuideActivity.class);
                            }
                            AppLogger.d("可以跳转：" + toIntent.getComponent().getClassName());
                            toIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            weakReference.get().startActivity(toIntent);
                        } else {
                            //start with context
                            AppLogger.d("空了?");
                        }
                    }
                }
            }
        }

        private void run() {
            try {
                if (br != null) {
                    ContextUtils.getContext().unregisterReceiver(br);
                }
                br = new Br();
                ContextUtils.getContext().registerReceiver(br, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            } catch (Exception e) {

            }
        }

        private void cancel() {
            weakReference = null;
            try {
                if (br != null) {
                    ContextUtils.getContext().unregisterReceiver(br);
                }
            } catch (Exception e) {

            }

        }
    }

    private void tryLoadConfigApFragment() {
        try {
            final WifiInfo info = NetUtils.getWifiManager(ContextUtils.getContext()).getConnectionInfo();
            if (info == null || !ApFilter.accept(info.getSSID()) || NetUtils.getNetType(ContextUtils.getContext()) != ConnectivityManager.TYPE_WIFI) {
                AppLogger.i("bind: " + info);
                return;
            }
            if (getIntent().getBooleanExtra(JConstant.KEY_720_CONFIG_HOT_SPOT, false)) {
                finishExt();//720
                return;
            }

            String panoramaConfigure = getIntent().getStringExtra("PanoramaConfigure");
            JConstant.DEVICE_PRODUCT_SERIES productSeries = (JConstant.DEVICE_PRODUCT_SERIES) getIntent().getSerializableExtra(JConstant.KEY_BIND_DEVICE_PRODUCT_SERIES);
            if (TextUtils.equals(panoramaConfigure, "OutDoor") && ApFilter.isAPMode(info.getSSID(), uuid())
                    && NetUtils.getNetType(ContextUtils.getContext()) == ConnectivityManager.TYPE_WIFI) {
                Bundle bundle = new Bundle();
                bundle.putString("PanoramaConfigure", panoramaConfigure);
                bundle.putBoolean("Success", true);
                bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID));
                ConfigPanoramaWiFiSuccessFragment newInstance = ConfigPanoramaWiFiSuccessFragment.newInstance(bundle);
                ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), newInstance, android.R.id.content);
            } else if (productSeries == JConstant.DEVICE_PRODUCT_SERIES.SERIES_SMART_CAMERA_OUTDOOR_4G) {
                Intent intent = getIntent();
                intent.setClass(this, Config4GActivity.class);
                intent.putExtra(JConstant.KEY_BIND_DEVICE, getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE));
                startActivity(intent);
                finish();
            } else {
                Intent intent = getIntent();
                intent.setClass(this, ConfigWifiActivity.class);
                intent.putExtra(JConstant.KEY_BIND_DEVICE, getIntent().getStringExtra(JConstant.KEY_BIND_DEVICE));
                startActivity(intent);
                finish();
            }
        } finally {
            if (autoBack != null) {
                autoBack.cancel();
            }
        }
    }

    @OnClick(R.id.iv_explain_gray)
    public void onExplain() {
        PanoramaExplainFragment fragment = PanoramaExplainFragment.newInstance(null);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                fragment, android.R.id.content);
    }
}
