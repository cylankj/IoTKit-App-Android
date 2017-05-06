package com.cylan.jiafeigou.n.view.cam;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.ClientUpdateManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cam.FirmwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.FirmwareUpdatePresenterImpl;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.google.gson.Gson;

import java.io.File;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

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
    private Subscription subscription;

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
            hardwareUpdatePoint.setVisibility(description.hasNew ? View.VISIBLE : View.INVISIBLE);
            tvVersionDescribe.setText(description.tip);
            if (validateFile(description)) {
                tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
            } else if (description.downloadState == JConstant.D.DOWNLOADING && System.currentTimeMillis() - description.lastUpdateTime < 3 * 1000) {
                tvDownloadSoftFile.setText(getString(R.string.Tap1_FirmwareDownloading, "0/" + MiscUtils.FormatSdCardSize(description.fileSize)));
                ClientUpdateManager.getInstance().downLoadFile(description, new Download(this));
            } else {
                tvDownloadSoftFile.setText(getString(R.string.Tap1_Album_DownloadFailed));
            }
        } catch (Exception e) {
            AppLogger.e("err :" + MiscUtils.getErr(e));
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
            downloadProgress.setProgress(100);
        });
    }

    @Override
    public void process(long currentByte, long totalByte) {
        tvDownloadSoftFile.post(() -> {
            tvDownloadSoftFile.setEnabled(false);
            tvDownloadSoftFile.setText(getString(R.string.Tap1_FirmwareDownloading, MiscUtils.FormatSdCardSize(currentByte) + "/" + MiscUtils.FormatSdCardSize(totalByte)));
            tvLoadingShow.setText(MiscUtils.FormatSdCardSize(currentByte / 8) + "/" + MiscUtils.FormatSdCardSize(totalByte));
            if (totalByte == 0) return;
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


    @OnClick(R.id.tv_download_soft_file)
    public void downloadOrUpdate() {
        String txt = tvDownloadSoftFile.getText().toString();
        if (TextUtils.equals(txt, getString(R.string.Tap1_Update))) {
            //升级
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
        if (subscription != null) subscription.unsubscribe();
    }

    /**
     * 24小时的规则,只有下载成功的前提下生效
     *
     * @param description
     * @return
     */
    private boolean validateContent(RxEvent.CheckDevVersionRsp description) {
        if (description == null) return false;//没有下载过
//        if (description.downloadState == JConstant.D.FAILED) return false;//下载失败
        if (TextUtils.isEmpty(description.fileDir)) return false;//文件路径出错
        File file = new File(description.fileDir, description.fileName);
        if (!file.exists()) return false;//文件不存在
        //下载中,App异常了.这个条件优先级比较低,如果很就之前就下载好了呢.
//        if (System.currentTimeMillis() - description.downloadUpdateTime > 2 * 1000 * 60)
//            return false;
        //
        //        String localFileMd5 = FileUtils.getFileMd5(description.filePath);
//        AppLogger.d("localFileMd5:" + localFileMd5);
//        if (!TextUtils.equals(localFileMd5, description.md5)) {
//            FileUtils.deleteFile(description.filePath);
//            return false;
//        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finishExt();
    }

    @Override
    public void setPresenter(FirmwareUpdateContract.Presenter presenter) {

    }

}
