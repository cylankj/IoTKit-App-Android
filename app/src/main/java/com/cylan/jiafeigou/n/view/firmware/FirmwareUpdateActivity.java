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
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.google.gson.Gson;

import java.io.File;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FirmwareUpdateActivity extends BaseFullScreenFragmentActivity<FirmwareUpdateContract.Presenter>
        implements FirmwareUpdateContract.View, ClientUpdateManager.DownloadListener,
        ClientUpdateManager.FUpgradingListener {
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
        RxEvent.CheckVersionRsp description = null;
        ClientUpdateManager.PackageDownloadTask packageDownloadAction = ClientUpdateManager.getInstance().getUpdateAction(getUuid());
        if (packageDownloadAction == null) {
        } else {
            //下载中
            packageDownloadAction.setDownloadListener(new FirmwareUpdateActivity.Download(this));
            description = packageDownloadAction.getCheckDevVersionRsp();
        }
        try {
            //没下载,下载失败,或者下载成功.
            if (description == null) {
                String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + getUuid());
                if (!TextUtils.isEmpty(content)) {
                    description = new Gson().fromJson(content, RxEvent.CheckVersionRsp.class);
                    if (TextUtils.equals(description.version, currentVersion)) {
                        description = null;
                        PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CONTENT + getUuid());
                        //已经是最新的了.
                        basePresenter.cleanFile();
                    }
                }
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
            basePresenter.cleanFile();
        }
        packageDownloadAction = ClientUpdateManager.getInstance().getUpdateAction(getUuid());
        if (packageDownloadAction != null && packageDownloadAction.getCheckDevVersionRsp().downloadState == JConstant.U.UPDATING)
            return;
        ClientUpdateManager.FirmWareUpdatingTask updatingTask = ClientUpdateManager.getInstance().getUpdatingTask(getUuid());
        if (updatingTask != null && updatingTask.getUpdateState() == JConstant.U.UPDATING) {
            ClientUpdateManager.getInstance().enqueue(getUuid(), new Updating(this));
        } else {
            ClientUpdateManager.getInstance().removeTask(getUuid());
        }
    }

    private boolean validateFile(RxEvent.CheckVersionRsp description) {
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

    @Override
    public void upgradeStart() {
        customToolbar.post(() -> {
            llDownloadPgContainer.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void upgradeProgress(int percent) {
        customToolbar.post(() -> {
            llDownloadPgContainer.setVisibility(View.VISIBLE);
            tvLoadingShow.setText(percent + "%");
        });
    }

    //    public static final class U {
//        public static int FAILED_DEVICE_FAILED = -4;//设备返回非0
//        public static int FAILED_FPING_ERR = -3;
//        public static int FAILED_30S = -2;
//        public static int FAILED_60S = -1;
//        public static int IDLE = 0;
//        public static int UPDATING = 1;
//        public static int SUCCESS = 2;
//    }
    @Override
    public void upgradeErr(final int errCode) {
        customToolbar.post(() -> {
            tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
            llDownloadPgContainer.setVisibility(View.VISIBLE);
            switch (errCode) {
                case -2:
                case -3:
                    ToastUtil.showToast(getString(R.string.UPDATE_DISCONNECT));
                    break;
                case -1:
                case -4:
                    ToastUtil.showToast(getString(R.string.Tap1_FirmwareUpdateFai));
                    break;
            }
        });
    }

    @Override
    public void upgradeSuccess() {
        customToolbar.post(() -> {
            llDownloadPgContainer.setVisibility(View.VISIBLE);
            tvLoadingShow.setText("100%");
            ToastUtil.showToast(getString(R.string.Tap1_FirmwareUpdateSuc));
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
        if (TextUtils.equals(tvCurrentVersion.getText(), tvHardwareNewVersion.getText())) {
            //相同版本
            ToastUtil.showToast(getString(R.string.NEW_VERSION));
            return false;
        }
        int net = NetUtils.getJfgNetType();
        if (net == 0) {
            //2.客户端无网络
            ToastUtil.showToast(getString(R.string.NoNetworkTips));
            return false;
        }
        String deviceMac = device.$(202, "");
        String routMac = NetUtils.getRouterMacAddress();
        if (TextUtils.equals(deviceMac, routMac)) return true;
        if (JFGRules.isDeviceOnline(device.$(201, new DpMsgDefine.DPNet()))) {
            //3.局域网
            return true;
        }
        DpMsgDefine.DPNet dpNet = device.$(201, new DpMsgDefine.DPNet());
        String localSSid = NetUtils.getNetName(ContextUtils.getContext());
        String remoteSSid = dpNet.ssid;
        AppLogger.d("" + localSSid + "," + remoteSSid);
        //4.以上条件都不满足的话,就是在线了
        if (!TextUtils.equals(localSSid, remoteSSid) || dpNet.net != 1) {
            AlertDialogManager.getInstance().showDialog(this, getString(R.string.setwifi_check, remoteSSid),
                    getString(R.string.setwifi_check, remoteSSid), getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }, getString(R.string.CANCEL), null);
            return false;
        }
        //简单地认为是同一个局域网
        return true;
    }


    @OnClick(R.id.tv_download_soft_file)
    public void downloadOrUpdate(View v) {
        ViewUtils.deBounceClick(v, 1500);
        String txt = tvDownloadSoftFile.getText().toString();
        if (TextUtils.equals(txt, getString(R.string.Tap1_Update))) {
            //升级
            //1.网络环境{是否同一局域网}
            if (!checkNet()) return;
            //开始升级
            ClientUpdateManager.getInstance().enqueue(getUuid(), new Updating(this));
        } else if (txt.contains(getString(R.string.Tap1_FirmwareDownloading).substring(0, 2))) {
            //Tap1_FirmwareDownloading:正在下载(%s),
        } else {
            //1.下载失败
            //2.Tap1a_DownloadInstall 下载并安装(%s)
            try {
                String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + getUuid());
                final RxEvent.CheckVersionRsp description = new Gson().fromJson(content, RxEvent.CheckVersionRsp.class);
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


    private static class Updating implements ClientUpdateManager.FUpgradingListener {

        private WeakReference<ClientUpdateManager.FUpgradingListener> listenerRef;

        public Updating(ClientUpdateManager.FUpgradingListener listener) {
            this.listenerRef = new WeakReference<>(listener);
        }

        @Override
        public void upgradeStart() {
            if (listenerRef == null || listenerRef.get() == null) return;
            listenerRef.get().upgradeStart();
        }

        @Override
        public void upgradeProgress(int percent) {
            if (listenerRef == null || listenerRef.get() == null) return;
            listenerRef.get().upgradeProgress(percent);
        }

        @Override
        public void upgradeErr(int errCode) {
            if (listenerRef == null || listenerRef.get() == null) return;
            listenerRef.get().upgradeErr(errCode);
        }

        @Override
        public void upgradeSuccess() {
            if (listenerRef == null || listenerRef.get() == null) return;
            listenerRef.get().upgradeSuccess();
        }

    }
}
