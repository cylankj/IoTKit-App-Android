package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.SdCardInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.SdCardInfoPresenterImpl;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;

public class SdcardDetailActivity extends BaseFullScreenFragmentActivity<SdCardInfoContract.Presenter>
        implements SdCardInfoContract.View {
    @BindView(R.id.tv_sdcard_volume)
    TextView tvSdcardVolume;
    @BindView(R.id.view_has_use_volume)
    View viewHasUseVolume;
    @BindView(R.id.tv_clear_sdcard)
    TextView tvClecrSdcard;
    @BindView(R.id.view_total_volume)
    View viewTotalVolume;
    @BindView(R.id.iv_loading_rotate)
    ImageView ivLoadingRotate;
    @BindView(R.id.tv_clear_restart)
    TextView tvClearRestart;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    private AlertDialog alertDialog;
    private AlertDialog formatSdcardDialog;
    private AlertDialog noSdcardDialog;
    private String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragement_sdcard_detail_info);
        ButterKnife.bind(this);
        this.uuid = getIntent().getStringExtra(KEY_DEVICE_ITEM_UUID);
        basePresenter = new SdCardInfoPresenterImpl(this, uuid);
        customToolbar.setBackAction(o -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        finishExt();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initDetailData();
    }

    private void dismissDilaog(AlertDialog... dialog) {
        if (dialog != null) {
            for (AlertDialog dialog1 : dialog) {
                if (dialog1.isShowing()) dialog1.dismiss();
            }
        }
    }

    @OnClick({R.id.tv_clear_sdcard})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_clear_sdcard:
                //格式化SD卡
                showClearSdDialog();
                break;
        }
    }

    private void showClearSdDialog() {
        String[] split = tvSdcardVolume.getText().toString().split("/");
        if (split == null || split.length == 0) {
            return;
        }
        if ("0.0MB".equals(split[0])) {
            ToastUtil.showPositiveToast(getString(R.string.Clear_Sdcard_tips3));
        } else {
            if (formatSdcardDialog != null && formatSdcardDialog.isShowing()) return;
            if (formatSdcardDialog == null) {
                formatSdcardDialog = new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.Clear_Sdcard_tips))
                        .setPositiveButton(getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                            basePresenter.updateInfoReq();
                            showLoading();
                        })
                        .setNegativeButton(getString(R.string.CANCEL), null)
                        .create();
                formatSdcardDialog.show();
                dismissDilaog(noSdcardDialog, alertDialog);
            }
        }

    }

    @Override
    public void sdUseDetail(String volume, float data) {
        tvSdcardVolume.setText(volume);
        viewTotalVolume.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = viewTotalVolume.getWidth();
                ViewGroup.LayoutParams layoutParams = viewHasUseVolume.getLayoutParams();
                int wide = (int) (data * width);
                layoutParams.width = wide;
                viewHasUseVolume.setLayoutParams(layoutParams);
                viewTotalVolume.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void showLoading() {
        tvClecrSdcard.setEnabled(false);
        ivLoadingRotate.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.loading_progress_rotate);
        ivLoadingRotate.startAnimation(animation);
    }

    @Override
    public void hideLoading() {
        tvClecrSdcard.setEnabled(true);
        ivLoadingRotate.setVisibility(View.GONE);
        ivLoadingRotate.clearAnimation();
    }

    // 格式化的结果
    @Override
    public void clearSdResult(int code) {
        hideLoading();
        switch (code) {
            case 0:
                ToastUtil.showPositiveToast(getString(R.string.Clear_Sdcard_tips3));
                break;
            case 1:
                ToastUtil.showNegativeToast(getString(R.string.Clear_Sdcard_tips4));
                break;
            case 2:
                ToastUtil.showNegativeToast(getString(R.string.Clear_Sdcard_tips5));
                break;
        }
    }

    @Override
    public void initSdUseDetailRsp(DpMsgDefine.DPSdStatus sdStatus) {
        sdStatus = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid)
                .$(204, new DpMsgDefine.DPSdStatus());
        if (sdStatus.err == 0 && sdStatus.hasSdcard) {
            long sdcardTotalCapacity = sdStatus.total;
            long sdcardUsedCapacity = sdStatus.used;
            float v = (float) ((sdcardUsedCapacity * 1.0) / sdcardTotalCapacity);
            sdUseDetail(MiscUtils.FormetSDcardSize(sdcardUsedCapacity) + "/" + MiscUtils.FormetSDcardSize(sdcardTotalCapacity), v);
        } else {
            showSdPopDialog();
        }
    }

    @Override
    public void showSdPopDialog() {
        if (alertDialog != null && alertDialog.isShowing()) return;
        if (alertDialog == null)
            alertDialog = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.MSG_SD_OFF))
                    .setNegativeButton(getString(R.string.CANCEL), null)
                    .setPositiveButton(getString(R.string.OK), null)
                    .create();
        alertDialog.show();
        dismissDilaog(noSdcardDialog, alertDialog);
    }

    private void initDetailData() {
        if (!basePresenter.getSdcardState()) {
            showHasNoSdDialog();
            return;
        }
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(this.uuid);
        //仅3G摄像头显示此栏
        if (device != null && JFGRules.is3GCam(device.pid)) {
            tvClearRestart.setVisibility(View.VISIBLE);
        }

        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        boolean show = net != null && JFGRules.isDeviceOnline(net);
        if (!show || NetUtils.getNetType(getContext()) == -1) {
            tvClecrSdcard.setTextColor(Color.parseColor("#8c8c8c"));
            tvClecrSdcard.setEnabled(false);
        }
    }


    private void showHasNoSdDialog() {
        if (noSdcardDialog != null && noSdcardDialog.isShowing()) return;
        if (noSdcardDialog == null) {
            noSdcardDialog = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.MSG_SD_OFF))
                    .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        finishExt();
                    })
                    .create();
        }
        noSdcardDialog.show();
        dismissDilaog(formatSdcardDialog, alertDialog);
    }

    @Override
    public void setPresenter(SdCardInfoContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
