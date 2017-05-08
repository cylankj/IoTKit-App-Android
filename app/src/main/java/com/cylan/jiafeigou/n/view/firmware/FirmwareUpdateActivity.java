package com.cylan.jiafeigou.n.view.firmware;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.ClientUpdateManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cam.FirmwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.FirmwareUpdatePresenterImpl;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.google.gson.Gson;

import java.io.File;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FirmwareUpdateActivity extends BaseFullScreenFragmentActivity<FirmwareUpdateContract.Presenter>
        implements FirmwareUpdateContract.View, ClientUpdateManager.DownloadListener {
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
    @BindView(R.id.tv_percent)
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
            ClientUpdateManager.PackageDownloadTask packageDownloadAction = ClientUpdateManager.getInstance().getUpdateAction(getUuid());
            RxEvent.CheckDevVersionRsp description = null;
            if (packageDownloadAction == null) {
                //没下载,下载失败,或者下载成功.
                String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + getUuid());
                description = new Gson().fromJson(content, RxEvent.CheckDevVersionRsp.class);
                if (TextUtils.equals(description.version, currentVersion)) {
                    description = null;
                    PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CONTENT + getUuid());
                    PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CHECK_TIME + getUuid());
                    //已经是最新的了.
                }
            } else {
                //下载中
                packageDownloadAction.setDownloadListener(new FirmwareUpdateActivity.Download(this));
                description = packageDownloadAction.getCheckDevVersionRsp();
            }
            tvHardwareNewVersion.setText(description.version);
            hardwareUpdatePoint.setVisibility(description.hasNew ? View.VISIBLE : View.INVISIBLE);
            tvVersionDescribe.setText(description.tip);
            if (validateFile(description)) {
                //下载成功
                tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
            } else if (description.downloadState == JConstant.D.DOWNLOADING && System.currentTimeMillis() - description.lastUpdateTime < 3 * 1000) {
                //下载中
                tvDownloadSoftFile.setText(getString(R.string.Tap1_FirmwareDownloading, "0/" + MiscUtils.FormatSdCardSize(description.fileSize)));
                ClientUpdateManager.getInstance().downLoadFile(description, new Download(this));
            } else if (description.downloadState == JConstant.D.FAILED) {
                tvDownloadSoftFile.setText(getString(R.string.Tap1_Album_DownloadFailed));
            } else {
                //异常.下载中App挂了.
                //失败
                tvDownloadSoftFile.setText(getString(R.string.Tap1a_DownloadInstall, MiscUtils.FormatSdCardSize(description.fileSize)));
            }
        } catch (Exception e) {
            AppLogger.e("err :" + MiscUtils.getErr(e));
            tvCurrentVersion.setText(currentVersion);
            tvHardwareNewVersion.setText(currentVersion);
            hardwareUpdatePoint.setVisibility(View.INVISIBLE);
            tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
        }
        ClientUpdateManager.FirmWareUpdatingTask updatingTask = ClientUpdateManager.getInstance().getUpdatingTask(getUuid());
        if (updatingTask != null) {
            Bundle bundle = new Bundle();
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, getUuid());
            ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                    FirmwareUpdatingFragment.newInstance(bundle), android.R.id.content);
        }
    }

    private boolean validateFile(RxEvent.CheckDevVersionRsp description) {
        File file = new File(description.fileDir, description.fileName);
        return description.downloadState == JConstant.D.SUCCESS
                && file.exists() && file.length() == description.fileSize;
    }

    @Override
    public void start(long totalByte) {
        llDownloadPgContainer.post(() -> {
            tvDownloadSoftFile.setText(getString(R.string.Tap1_FirmwareDownloading, "0/" + MiscUtils.FormatSdCardSize(totalByte)));
            llDownloadPgContainer.setVisibility(View.VISIBLE);
            tvLoadingShow.setText("0/" + MiscUtils.FormatSdCardSize(totalByte));
        });
    }

    @Override
    public void failed(Throwable throwable) {
        tvDownloadSoftFile.post(() -> {
            tvDownloadSoftFile.setEnabled(true);
            tvDownloadSoftFile.setText(getString(R.string.Tap1_Album_DownloadFailed));
        });
    }

    @Override
    public void finished(File file) {
        tvDownloadSoftFile.post(() -> {
            tvDownloadSoftFile.setEnabled(true);
            tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
            llDownloadPgContainer.setVisibility(View.GONE);
        });
    }

    @Override
    public void process(long currentByte, long totalByte) {
        tvDownloadSoftFile.post(() -> {
            tvDownloadSoftFile.setEnabled(false);
            tvDownloadSoftFile.setText(getString(R.string.Tap1_FirmwareDownloading, MiscUtils.FormatSdCardSize(currentByte) + "/" + MiscUtils.FormatSdCardSize(totalByte)));
            tvLoadingShow.setText(MiscUtils.FormatSdCardSize(currentByte) + "/" + MiscUtils.FormatSdCardSize(totalByte));
            if (totalByte == 0) return;
            llDownloadPgContainer.setVisibility(View.VISIBLE);
            downloadProgress.setProgress((int) ((float) currentByte / totalByte * 100));
        });
    }


    private static class Download implements ClientUpdateManager.DownloadListener {

        private WeakReference<FirmwareUpdateActivity> updateActivityWeakReference;

        public Download(FirmwareUpdateActivity activity) {
            updateActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void start(long totalByte) {
            if (updateActivityWeakReference == null || updateActivityWeakReference.get() == null)
                return;
            updateActivityWeakReference.get().start(totalByte);
        }

        @Override
        public void failed(Throwable throwable) {
            if (updateActivityWeakReference == null || updateActivityWeakReference.get() == null)
                return;
            updateActivityWeakReference.get().failed(throwable);
        }

        @Override
        public void finished(File file) {
            if (updateActivityWeakReference == null || updateActivityWeakReference.get() == null)
                return;
            updateActivityWeakReference.get().finished(file);
        }

        @Override
        public void process(long currentByte, long totalByte) {
            if (updateActivityWeakReference == null || updateActivityWeakReference.get() == null)
                return;
            updateActivityWeakReference.get().process(currentByte, totalByte);
        }
    }

    private boolean checkNet() {
        Device device = basePresenter.getDevice();
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        String localSSid = NetUtils.getNetName(ContextUtils.getContext());
        String remoteSSid = net.ssid;
        AppLogger.d("" + localSSid + "," + remoteSSid);
        if (!TextUtils.equals(localSSid, remoteSSid) || net.net != 1) {
            AlertDialogManager.getInstance().showDialog(this, getString(R.string.setwifi_check, remoteSSid),
                    getString(R.string.setwifi_check, remoteSSid), getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }, getString(R.string.CANCEL), null);
            return false;
        }
        //简单地认为是同一个局域网
        return true;
    }

    private boolean canNext() {
        Device device = basePresenter.getDevice();

        if (TextUtils.equals(tvCurrentVersion.getText(), tvHardwareNewVersion.getText())) {
            ToastUtil.showToast(getString(R.string.NEW_VERSION));
            return false;
        }

        int net = NetUtils.getJfgNetType();
        if (net == 0) {
            ToastUtil.showToast(getString(R.string.NoNetworkTips));
            return false;
        }

        String deviceMac = device.$(202, "");
        String routMac = NetUtils.getRouterMacAddress();
        if (TextUtils.equals(deviceMac, routMac)) return true;
        if (!JFGRules.isDeviceOnline(device.$(201, new DpMsgDefine.DPNet()))) {
            ToastUtil.showToast(getString(R.string.NOT_ONLINE));
            return false;
        }
        return false;
    }

    @OnClick(R.id.tv_download_soft_file)
    public void downloadOrUpdate() {
        if (!canNext()) return;
        String txt = tvDownloadSoftFile.getText().toString();
        if (TextUtils.equals(txt, getString(R.string.Tap1_Update))) {
            //升级
            //1.网络环境{是否同一局域网}
            if (!checkNet()) return;
            Bundle bundle = new Bundle();
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, getUuid());
            ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                    FirmwareUpdatingFragment.newInstance(bundle), android.R.id.content);
        } else if (txt.contains(getString(R.string.Tap1_FirmwareDownloading).substring(0, 2))) {
            //Tap1_FirmwareDownloading:正在下载(%s),
        } else {
            //1.下载失败
            //2.Tap1a_DownloadInstall 下载并安装(%s)
            try {
                String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + getUuid());
                final RxEvent.CheckDevVersionRsp description = new Gson().fromJson(content, RxEvent.CheckDevVersionRsp.class);
                ClientUpdateManager.getInstance().downLoadFile(description, new Download(this));
            } catch (Exception e) {
                AppLogger.e("err:" + MiscUtils.getErr(e));
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        finishExt();
    }

}
