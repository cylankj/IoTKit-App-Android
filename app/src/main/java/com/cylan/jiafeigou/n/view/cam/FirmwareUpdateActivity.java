package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cam.HardwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.HardwareUpdatePresenterImpl;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ContextUtils;

import butterknife.BindView;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;

public class FirmwareUpdateActivity extends BaseFullScreenFragmentActivity<HardwareUpdateContract.Presenter>
        implements HardwareUpdateContract.View {
    @BindView(R.id.tv_hardware_now_version)
    TextView tvHardwareNowVersion;
    @BindView(R.id.tv_hardware_new_version)
    TextView tvHardwareNewVersion;
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

    private String uuid;
    private RxEvent.CheckDevVersionRsp checkDevVersion;
    private String fileSize;

    private FirmwareFragment.OnUpdateListener listener;

    public interface OnUpdateListener {
        void onUpdateResult(boolean hasNew);
    }

    public void setOnUpdateListener(FirmwareFragment.OnUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_hardware_update);
        this.uuid = getIntent().getStringExtra(KEY_DEVICE_ITEM_UUID);
        this.checkDevVersion = getIntent().getParcelableExtra("version_content");
        basePresenter = new HardwareUpdatePresenterImpl(this, uuid, checkDevVersion);
    }

    @Override
    public void setPresenter(HardwareUpdateContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return ContextUtils.getContext();
    }

    @Override
    public void handlerResult(int code) {

    }

    @Override
    public void onDownloadStart() {

    }

    @Override
    public void onDownloadFinish() {

    }

    @Override
    public void onDownloading(double percent, long downloadedLength) {

    }

    @Override
    public void onDownloadErr(int reason) {

    }

    @Override
    public void beginUpdate() {

    }

    @Override
    public void onUpdateing(int percent) {

    }

    @Override
    public void initFileSize(String size) {

    }

    @Override
    public void showPingLoading() {

    }

    @Override
    public void hidePingLoading() {

    }

    @Override
    public void deviceNoRsp() {

    }
}
