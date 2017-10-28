package com.cylan.jiafeigou.n.view.firmware;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
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
import com.cylan.jiafeigou.misc.ver.AbstractVersion;
import com.cylan.jiafeigou.misc.ver.BaseFUUpdate;
import com.cylan.jiafeigou.misc.ver.IFUUpdate;
import com.cylan.jiafeigou.misc.ver.NormalFUUpdate;
import com.cylan.jiafeigou.misc.ver.PanFUUpdate;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.FirmwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.FirmwareUpdatePresenterImpl;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.BaseRequest;
import com.lzy.okserver.download.DownloadInfo;
import com.lzy.okserver.download.DownloadManager;
import com.lzy.okserver.download.db.DownloadDBManager;
import com.lzy.okserver.listener.DownloadListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Badge(parentTag = "DeviceInfoDetailFragment")
public class FirmwareUpdateActivity extends BaseFullScreenFragmentActivity<FirmwareUpdateContract.Presenter>
        implements FirmwareUpdateContract.View,
        IFUUpdate.FUpgradingListener {
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
    @BindView(R.id.tv_new_version_name)
    TextView tvNewVersionName;
    @BindView(R.id.tv_version_desc)
    TextView tvVersionDesc;

    private AbstractVersion.BinVersion binVersion;
    //升级过程中,可能有网络切换,
    private String currentVersion;

    private String newVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_hardware_update);
        ButterKnife.bind(this);
        presenter = new FirmwareUpdatePresenterImpl(this);
        customToolbar.setBackAction(v -> {
            if (isCurrentUpdating()) {
                //升级中,返回直播页面.
                if (!TextUtils.isEmpty(JConstant.KEY_CURRENT_PLAY_VIEW)) {
                    ComponentName name = new ComponentName(this, JConstant.KEY_CURRENT_PLAY_VIEW);
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setComponent(name);
                    ResolveInfo info = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (info != null) {
                        startActivity(intent);
                    } else {
                        onBackPressed();//下载中返回上一级
                    }
                }
            } else {
                onBackPressed();//下载中返回上一级
            }
        });
    }

    private AbstractVersion.BinVersion getVersion() {
        final String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + uuid());
        if (TextUtils.isEmpty(content)) {
            return AbstractVersion.BinVersion.NULL;
        }
        try {
            return new Gson().fromJson(content, AbstractVersion.BinVersion.class);
        } catch (Exception e) {
            return AbstractVersion.BinVersion.NULL;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Device device = presenter.getDevice();
        currentVersion = device.$(207, "");
        boolean result = isDownloading();
        if (!result) {
            dealUpdate();
        }
    }

    private boolean isDownloading() {
        DownloadManager.getInstance().setTargetFolder(JConstant.UPDATE_FILE_PATH);
        binVersion = getVersion();
        if (binVersion.isNULL()) {
            newVersion = currentVersion;
            tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
            hardwareUpdatePoint.setVisibility(View.INVISIBLE);
            tvCurrentVersion.setText(currentVersion);
            tvNewVersionName.setText(newVersion);
            return false;
        } else {
            newVersion = binVersion.getTagVersion();
            tvVersionDesc.setText(binVersion.getContent());
        }
        tvCurrentVersion.setText(currentVersion);
        tvNewVersionName.setText(newVersion);
        if (TextUtils.equals(currentVersion, newVersion)) {
            PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CONTENT + uuid());
            AppLogger.d("相同版本:" + newVersion);
            return false;
        }
        if (BindUtils.versionCompare(currentVersion, newVersion) < 0) {
            hardwareUpdatePoint.setVisibility(View.VISIBLE);
        }
        final int size = binVersion.getList() == null ? 0 : binVersion.getList().size();
        boolean downloading = false;
        int finished = 0;
        for (int i = 0; i < size; i++) {
            DownloadInfo info = DownloadManager.getInstance().getDownloadInfo(binVersion.getList().get(i).url);
            if (info != null && info.getState() == DownloadManager.DOWNLOADING) {
                downloading = true;
            }
            if (info != null && info.getState() == DownloadManager.FINISH) {
                finished++;
            }
            if (info != null && info.getState() == DownloadManager.NONE) {
                finished++;
            }
        }
        if (finished == size) {
            //下载完成
            boolean fileCheck = validateFile(binVersion);
            if (fileCheck) {
                tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
                AppLogger.d("竟然完成了?");
            } else {
                //文件不存在了
                tvDownloadSoftFile.setText(getString(R.string.Tap1a_DownloadInstall, MiscUtils.FormatSdCardSize(binVersion.getTotalSize())));
            }
        } else if (downloading) {
            //下载中
            tvDownloadSoftFile.setText(getString(R.string.Tap1_FirmwareDownloading, "0/" + MiscUtils.FormatSdCardSize(binVersion.getTotalSize())));
            toDownload();
        } else {//异常,或者未开始
            tvDownloadSoftFile.setText(getString(R.string.Tap1a_DownloadInstall, MiscUtils.FormatSdCardSize(binVersion.getTotalSize())));
        }
        return true;
    }

    private boolean isCurrentUpdating() {
        BaseFUUpdate update = ClientUpdateManager.getInstance().getUpdatingTask(uuid());
        return update != null && update.getUpdateState() == JConstant.U.UPDATING;
    }

    /**
     * 下载结束,检查
     *
     * @param binVersion
     * @return
     */
    private boolean validateFile(AbstractVersion.BinVersion binVersion) {
        final int count = ListUtils.getSize(binVersion.getList());
        for (int i = 0; i < count; i++) {
            final String key = binVersion.getList().get(i).url;
            DownloadInfo info = DownloadManager.getInstance().getDownloadInfo(key);
            if (info != null) {
                File file = new File(info.getTargetPath());
                if (file.length() != binVersion.getList().get(i).fileSize) {
                    //下载错误了.
                    FileUtils.deleteFile(file.getAbsolutePath());
                    DownloadManager.getInstance().removeTask(key);
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 可能是在升级
     */
    private void dealUpdate() {
        BaseFUUpdate baseFUUpdate = ClientUpdateManager.getInstance().getUpdatingTask(uuid());
        if (baseFUUpdate != null && baseFUUpdate.getUpdateState() == JConstant.U.UPDATING) {
            baseFUUpdate.setListener(new FUUpdating(this));
        } else if (baseFUUpdate != null && baseFUUpdate.getUpdateState() == JConstant.U.SUCCESS) {
            //成功

        }
    }

    @Override
    public void upgradeStart() {
        customToolbar.post(() -> {
            tvDownloadSoftFile.setEnabled(false);
            llDownloadPgContainer.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void upgradeProgress(int percent) {
        customToolbar.post(() -> {
            tvDownloadSoftFile.setEnabled(false);
            llDownloadPgContainer.setVisibility(View.VISIBLE);
            tvLoadingShow.setText(percent + "%");
            downloadProgress.setProgress(percent);
        });
    }

    @Override
    public void upgradeErr(final int errCode) {
        customToolbar.post(() -> {
            tvDownloadSoftFile.setEnabled(true);
            tvLoadingShow.setText("0%");
            downloadProgress.setProgress(0);
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
            tvDownloadSoftFile.setEnabled(true);
            llDownloadPgContainer.setVisibility(View.INVISIBLE);
            tvLoadingShow.setText("100%");
            ToastUtil.showToast(getString(R.string.Tap1_FirmwareUpdateSuc));
            hardwareUpdatePoint.setVisibility(View.INVISIBLE);
            tvCurrentVersion.setText(newVersion);
            tvNewVersionName.setText(newVersion);
        });
    }


    private boolean checkEnv() {
        Device device = presenter.getDevice();
        //相同版本
        if (TextUtils.equals(tvCurrentVersion.getText(), tvNewVersionName.getText())) {
            //相同版本
            ToastUtil.showToast(getString(R.string.NEW_VERSION));
            return false;
//            return true;//mock¬
        }
        String deviceMac = device.$(202, "");
        String routMac = NetUtils.getRouterMacAddress();
        //1.直连AP
        if (TextUtils.equals(deviceMac, routMac)) {
            return true;
        }
        DpMsgDefine.DPNet dpNet = device.$(201, new DpMsgDefine.DPNet());
        String localSSid = NetUtils.getNetName(ContextUtils.getContext());
        //2.不在线
        if (!JFGRules.isDeviceOnline(dpNet) || TextUtils.isEmpty(dpNet.ssid)) {
            ToastUtil.showToast(getString(R.string.NOT_ONLINE));
            return false;
        }
        String remoteSSid = dpNet.ssid;
        AppLogger.d("check ???" + localSSid + "," + remoteSSid);
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
        int net = NetUtils.getJfgNetType();
        if (net == 0) {
            //2.客户端无网络
            ToastUtil.showToast(getString(R.string.NoNetworkTips));
            return;
        }
        String txt = tvDownloadSoftFile.getText().toString();
        if (TextUtils.equals(txt, getString(R.string.Tap1_Update))) {
            //升级
            //1.网络环境{是否同一局域网}
            if (!checkEnv()) {
                return;
            }
            //2.电量
            int battery = presenter.getDevice().$(206, 0);
            if (battery <= 30) {
                AlertDialogManager.getInstance().showDialog(this, FirmwareUpdateActivity.class.getSimpleName(),
                        getString(R.string.Tap1_Update_Electricity),
                        getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        }, false);
                return;
            }
            //开始升级
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid());
            BaseFUUpdate update = ClientUpdateManager.getInstance().getUpdatingTask(uuid());
            if (JFGRules.isPan720(device.pid)) {
                update = new PanFUUpdate(uuid(), getFileNameList());
            } else {
                update = new NormalFUUpdate(uuid(), getFileNameList().get(0));
            }
            update.setListener(this);
            ClientUpdateManager.getInstance().enqueue(uuid(), update);
        } else if (txt.contains(getString(R.string.Tap1_FirmwareDownloading).substring(0, 2))) {
            //Tap1_FirmwareDownloading:正在下载(%s),
        } else {
            //1.下载失败
            //2.Tap1a_DownloadInstall 下载并安装(%s)
            if (NetUtils.getJfgNetType() == 2) {
                AlertDialogManager.getInstance().showDialog(this, getString(R.string.Tap1_Firmware_DataTips),
                        getString(R.string.Tap1_Firmware_DataTips),
                        getString(R.string.OK), (DialogInterface dialog, int which) -> {
                            toDownload();
                        }, getString(R.string.CANCEL), null);
                return;
            }
            final int size = binVersion == null ? 0 : ListUtils.getSize(binVersion.getList());
            for (int i = 0; i < size; i++) {
                final String key = binVersion.getList().get(i).url;
                DownloadManager.getInstance().removeTask(key);
            }
            toDownload();
        }
    }

    /**
     * 很大可能出错
     *
     * @return
     */
    private List<String> getFileNameList() {
        List<String> fileNameList = new ArrayList<>();
        final int count = ListUtils.getSize(binVersion.getList());
        for (int i = 0; i < count; i++) {
            DownloadInfo info = DownloadManager.getInstance().getDownloadInfo(binVersion.getList().get(i).url);
            if (info != null) {
                fileNameList.add(info.getFileName());
            }
        }
        return fileNameList;
    }

    private void toDownload() {
        final int size = binVersion == null ? 0 : ListUtils.getSize(binVersion.getList());
        for (int i = 0; i < size; i++) {
            final String key = binVersion.getList().get(i).url;
            DownloadInfo info = DownloadManager.getInstance().getDownloadInfo(key);
            if (info != null) {
                info.setListener(new DListener(this));
                DownloadDBManager.INSTANCE.replace(info);
            }
            BaseRequest baseRequest = OkGo.get(key);
            DownloadManager.getInstance().addTask(binVersion.getList().get(i).url, baseRequest, new DListener(this));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean performBackIntercept() {
        finishExt();
        return true;
    }

    /**
     * 固件升级的
     */
    private static class FUUpdating implements IFUUpdate.FUpgradingListener {

        private WeakReference<IFUUpdate.FUpgradingListener> listenerRef;

        public FUUpdating(IFUUpdate.FUpgradingListener listener) {
            this.listenerRef = new WeakReference<>(listener);
        }

        @Override
        public void upgradeStart() {
            if (listenerRef == null || listenerRef.get() == null) {
                return;
            }
            listenerRef.get().upgradeStart();
        }

        @Override
        public void upgradeProgress(int percent) {
            if (listenerRef == null || listenerRef.get() == null) {
                return;
            }
            listenerRef.get().upgradeProgress(percent);
        }

        @Override
        public void upgradeErr(int errCode) {
            if (listenerRef == null || listenerRef.get() == null) {
                return;
            }
            listenerRef.get().upgradeErr(errCode);
        }

        @Override
        public void upgradeSuccess() {
            if (listenerRef == null || listenerRef.get() == null) {
                return;
            }
            listenerRef.get().upgradeSuccess();
        }

    }

    private void dealErrDownload() {
        if (binVersion != null) {
            final int size = ListUtils.getSize(binVersion.getList());
            for (int i = 0; i < size; i++) {
                final String key = binVersion.getList().get(i).url;
                DownloadInfo info = DownloadManager.getInstance().getDownloadInfo(key);
                if (info != null) {
                    File file = new File(info.getTargetPath());
                    if (file.exists() && info.getDownloadLength() != file.length()) {
                        DownloadManager.getInstance().removeTask(key);
                        FileUtils.deleteFile(file.getAbsolutePath());
                        AppLogger.d("删除失败的");
                    }
                }
            }
        }
    }

    private static final class DListener extends DownloadListener {
        private WeakReference<FirmwareUpdateActivity> activityWeakReference;
        private static Map<String, Float> progressMap = new HashMap<>();
        private final Object lock = new Object();
        private boolean failed;

        public DListener(FirmwareUpdateActivity activity) {
            progressMap.clear();
            this.activityWeakReference = new WeakReference<>(activity);
        }

        private float getPercent(DownloadInfo downloadInfo) {
            synchronized (lock) {
                progressMap.put(downloadInfo.getFileName(), downloadInfo.getProgress());
                float percent = 0;
                Iterator<String> iterator = progressMap.keySet().iterator();
                while (iterator.hasNext()) {
                    final String name = iterator.next();
                    percent += progressMap.get(name);
                }
                return percent;
            }
        }

        @Override
        public void onProgress(DownloadInfo downloadInfo) {
            if (activityWeakReference.get() == null) {
                return;
            }
            AbstractVersion.BinVersion version = activityWeakReference.get().binVersion;
            if (version == null) {
                throw new IllegalArgumentException("错了");
            }
            float percent = getPercent(downloadInfo);
            long totalByte = version.getTotalSize();
            if (percent > 1.0 || failed) {
                DownloadManager.getInstance().pauseAllTask();
                failed = true;
                activityWeakReference.get().dealErrDownload();
                AppLogger.d("下载失败啊?percent huge");
                activityWeakReference.get().tvDownloadSoftFile.setEnabled(true);
                activityWeakReference.get().tvDownloadSoftFile.setText(activityWeakReference.get().getString(R.string.Tap1_Album_DownloadFailed));
                Log.e("downloading", "downloading: 下载出错.");
                return;
            }
            Log.d("downloading", "downloading: " + downloadInfo.getProgress());
            Log.d("downloading", "downloading: " + downloadInfo.getFileName());
            Log.d("downloading", "downloading: " + percent);
            if (totalByte == 0) {
                return;
            }
            if (activityWeakReference.get() == null) {
                return;
            }
            activityWeakReference.get().tvDownloadSoftFile.setEnabled(false);
            activityWeakReference.get().tvDownloadSoftFile.setText(activityWeakReference.get().getString(R.string.Tap1_FirmwareDownloading, MiscUtils.FormatSdCardSize((long) (percent * totalByte)) + "/" + MiscUtils.FormatSdCardSize(totalByte)));
            activityWeakReference.get().tvLoadingShow.setText(MiscUtils.FormatSdCardSize((long) (percent * totalByte)) + "/" + MiscUtils.FormatSdCardSize(totalByte));
            activityWeakReference.get().llDownloadPgContainer.setVisibility(View.VISIBLE);
            activityWeakReference.get().downloadProgress.setProgress((int) (percent * 100));
        }

        @Override
        public void onFinish(DownloadInfo downloadInfo) {
            if (activityWeakReference.get() == null) {
                return;
            }
            Log.d("downloading", "downloading onFinish");
            activityWeakReference.get().tvDownloadSoftFile.setEnabled(true);
            activityWeakReference.get().tvDownloadSoftFile.setText(activityWeakReference.get().getString(R.string.Tap1_Update));
            activityWeakReference.get().llDownloadPgContainer.setVisibility(View.GONE);
        }

        @Override
        public void onError(DownloadInfo downloadInfo, String s, Exception e) {
            if (activityWeakReference.get() == null) {
                return;
            }
            Log.d("downloading", "downloading onError");
            activityWeakReference.get().tvDownloadSoftFile.setEnabled(true);
            activityWeakReference.get().tvDownloadSoftFile.setText(activityWeakReference.get().getString(R.string.Tap1_Album_DownloadFailed));
            AppLogger.d("下载失败啊?" + MiscUtils.getErr(e) + "," + s);
            if (downloadInfo != null) {
                handleErr(downloadInfo.getUrl(), downloadInfo.getTargetPath());
            }
        }
    }

    private static void handleErr(final String url, final String filePath) {
        try {
            FileUtils.deleteAbsoluteFile(filePath);
            DownloadDBManager.INSTANCE.delete(url);
            DownloadManager.getInstance().removeTask(url);
        } catch (Exception e) {

        }
    }
}
