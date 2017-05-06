package com.cylan.jiafeigou.n.view.cam;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
            invalidateFile();
        } catch (Exception e) {
        }
    }

    private void invalidateFile() {
        subscription = Observable.just("check")
                .subscribeOn(Schedulers.newThread())
                .flatMap(ret -> {
                    RxEvent.CheckDevVersionRsp description = null;
                    try {
                        String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + getUuid());
                        description = new Gson().fromJson(content, RxEvent.CheckDevVersionRsp.class);
                    } catch (Exception e) {
                        description = null;
                    }
                    if (validateContent(description))
                        return Observable.just(new Pair<>(description, 0L));
                    return Observable.just(new Pair<>(description, description.fileSize));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (ret == null) {
                        tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
                    } else {
                        tvDownloadSoftFile.setText(getString(R.string.Tap1a_DownloadInstall, MiscUtils.getByteFromBitRate(ret.second)));
                    }
                }, AppLogger::e);
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
