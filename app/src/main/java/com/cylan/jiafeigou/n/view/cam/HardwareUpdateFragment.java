package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.HardwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.HardwareUpdatePresenterImpl;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class HardwareUpdateFragment extends IBaseFragment<HardwareUpdateContract.Presenter> implements HardwareUpdateContract.View {

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
    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.tv_version_describe)
    TextView tvVersionDescribe;
    @BindView(R.id.tv_loading_show)
    TextView tvLoadingShow;

    private String uuid;
    private RxEvent.CheckDevVersionRsp checkDevVersion;
    private String fileSize;

    public static HardwareUpdateFragment newInstance(Bundle bundle) {
        HardwareUpdateFragment fragment = new HardwareUpdateFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
        this.checkDevVersion = (RxEvent.CheckDevVersionRsp) getArguments().getSerializable("version_content");
        basePresenter = new HardwareUpdatePresenterImpl(this, checkDevVersion);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hardware_update, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewPaddingStatusBar(view.findViewById(R.id.fLayout_top_bar_container));
    }

    @Override
    public void onStart() {
        super.onStart();
        initView();
        if (basePresenter != null)basePresenter.getFileSize();
    }

    private void initView() {
        tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
        DpMsgDefine.DPPrimary<String> sVersion = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_207_DEVICE_VERSION);
        String s = MiscUtils.safeGet(sVersion, "");
        tvHardwareNowVersion.setText(s);
        tvHardwareNewVersion.setText(s);

        // 有新版本
        if (checkDevVersion != null && checkDevVersion.hasNew) {
            tvHardwareNewVersion.setText(checkDevVersion.version);
//            tvDownloadSoftFile.setText(String.format(getString(R.string.Tap1a_DownloadInstall), basePresenter.getFileSize()));
            hardwareUpdatePoint.setVisibility(View.VISIBLE);
            tvVersionDescribe.setVisibility(View.VISIBLE);
            tvVersionDescribe.setText(checkDevVersion.tip);
        }
    }

    @Override
    public void setPresenter(HardwareUpdateContract.Presenter presenter) {

    }

    @OnClick({R.id.tv_download_soft_file, R.id.imgV_top_bar_center})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_top_bar_center:
                getFragmentManager().popBackStack();
                break;

            case R.id.tv_download_soft_file:
                //TEST
//                handlerDownLoad();

                if (NetUtils.getJfgNetType(getContext()) == -1) {
                    ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }

                DpMsgDefine.DPNet net = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_201_NET);
                boolean show = net != null && JFGRules.isDeviceOnline(net);
                if (!show) {
                    ToastUtil.showNegativeToast(getString(R.string.NOT_ONLINE));
                    return;
                }

                if (!checkDevVersion.hasNew) {
                    ToastUtil.showPositiveToast(getString(R.string.NEW_VERSION));
                    return;
                }

                if (!tvDownloadSoftFile.getText().equals(getString(R.string.Tap1_Update))) {
                    handlerDownLoad();
                } else {
                    handlerUpdate();
                }
                break;
        }

    }

    /**
     * 处理升级
     */
    private void handlerUpdate() {
        if (tvHardwareNewVersion.getText().equals(tvHardwareNowVersion.getText())) {
            ToastUtil.showPositiveToast(getString(R.string.NEW_VERSION));
            return;
        }

        //设备在线
        DpMsgDefine.DPNet net = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_201_NET);
        String localSSid = NetUtils.getNetName(ContextUtils.getContext());
        String remoteSSid = net.ssid;
        if (!TextUtils.equals(localSSid, remoteSSid)) {
            new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.setwifi_check, remoteSSid))
                    .setPositiveButton(getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    })
                    .show();
        } else {
            new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.Tap1_UpdateFirmwareTips))
                    .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        //开始升级
                        basePresenter.startUpdate();
                        basePresenter.startCounting();
                    })
                    .show();
        }
    }

    /**
     * 处理下载
     */
    private void handlerDownLoad() {

        //wifi 网络
        int netType = NetUtils.getNetType(getContext());
        if (netType == 1) {
            //开始下载
            basePresenter.startDownload(basePresenter.creatDownLoadBean());

        } else if (netType == 2 || netType == 3 || netType == 4) {
            Bundle bundle = new Bundle();
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.CARRY_ON));
            bundle.putString(SimpleDialogFragment.KEY_CONTENT_CONTENT, getString(R.string.Tap1_Firmware_DataTips));
            SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.newInstance(bundle);
            simpleDialogFragment.setAction((int id, Object value) -> {
                //开始下载
                basePresenter.startDownload(basePresenter.creatDownLoadBean());
            });
            simpleDialogFragment.show(getFragmentManager(), "simpleDialogFragment");
        }
    }

    @Override
    public void handlerResult(int code) {
        switch (code) {
            case 1:
                ToastUtil.showNegativeToast(getString(R.string.Tap1_DownloadFirmwareFai));
                break;
            case 2:
                ToastUtil.showPositiveToast(getString(R.string.Tap1_FirmwareUpdateSuc));
                break;
            case 3:
                ToastUtil.showPositiveToast(getString(R.string.Tap1_FirmwareUpdateFai));
                break;
        }
    }

    @Override
    public void onDownloadStart() {
        tvDownloadSoftFile.setEnabled(false);
        llDownloadPgContainer.setVisibility(View.VISIBLE);
        tvLoadingShow.setText("0.0MB" + "/" + fileSize);
        downloadProgress.setProgress(0);
    }

    @Override
    public void onDownloadFinish() {

        tvDownloadSoftFile.setEnabled(true);
        llDownloadPgContainer.setVisibility(View.GONE);
        //设备在线
        DpMsgDefine.DPNet net = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_201_NET);
        String localSSid = NetUtils.getNetName(ContextUtils.getContext());
        String remoteSSid = net.ssid;
        if (TextUtils.equals(localSSid, remoteSSid)) {
            basePresenter.startUpdate();
        } else {
            //不在同局域网
            tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
        }
    }

    @Override
    public void onDownloading(double percent, long downloadedLength) {
        tvLoadingShow.setText(String.format(getString(R.string.Tap1_FirmwareDownloading), basePresenter.FormetSDcardSize(downloadedLength) + "/" + fileSize));
        downloadProgress.setProgress((int) (percent * 100));
    }

    @Override
    public void onDownloadErr(int reason) {
        handlerResult(reason);
    }

    @Override
    public void startUpdate() {
        tvLoadingShow.setText(String.format(getString(R.string.Tap1_FirmwareUpdating), 0 + ""));
        llDownloadPgContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUpdateing(int percent) {
        tvLoadingShow.setText(String.format(getString(R.string.Tap1_FirmwareUpdating), percent + ""));
        downloadProgress.setProgress(percent);
    }

    @Override
    public void initFileSize(String size) {
        fileSize = size;
        tvDownloadSoftFile.setText(String.format(getString(R.string.Tap1a_DownloadInstall), size));
    }

}
