package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.engine.FirmwareCheckerService;
import com.cylan.jiafeigou.n.mvp.contract.cam.FirmwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.FirmwareUpdatePresenterImpl;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FirmwareUpdateActivity extends BaseFullScreenFragmentActivity<FirmwareUpdateContract.Presenter>
        implements FirmwareUpdateContract.View {
    @BindView(R.id.tv_hardware_now_version)
    TextView tvCurrentVersion;
    @BindView(R.id.hardware_update_point)
    View hardwareUpdatePoint;
    @BindView(R.id.tv_download_soft_file)
    TextView tvDownloadSoftFile;
    @BindView(R.id.download_progress)
    ProgressBar downloadProgress;
    @BindView(R.id.ll_download_pg_container)
    LinearLayout llDownloadPgContainer;
    @BindView(R.id.tv_version_describe)
    TextView tvVersionDescribe;
    @BindView(R.id.tv_loading_show)
    TextView tvLoadingShow;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.tv_hardware_new_version)
    TextView tvHardwareNewVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_hardware_update);
        ButterKnife.bind(this);
        basePresenter = new FirmwareUpdatePresenterImpl(this);
        customToolbar.setBackAction(v -> onBackPressed());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Device device = basePresenter.getDevice();
        String currentVersion = device.$(207, "");
        tvCurrentVersion.setText(currentVersion);
        try {
            String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + getUuid());
            RxEvent.CheckDevVersionRsp description = new Gson().fromJson(content, RxEvent.CheckDevVersionRsp.class);
            tvHardwareNewVersion.setText(description.version);
        } catch (Exception e) {
        }
    }


    @Override
    public void onBackPressed() {
        finishExt();
    }

    @Override
    public void setPresenter(FirmwareUpdateContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return ContextUtils.getContext();
    }


}
