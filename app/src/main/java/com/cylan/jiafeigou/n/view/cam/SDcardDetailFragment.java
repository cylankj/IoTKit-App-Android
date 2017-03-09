package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDPDevice;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.SdCardInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.SdCardInfoPresenterImpl;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class SDcardDetailFragment extends IBaseFragment<SdCardInfoContract.Presenter> implements SdCardInfoContract.View {

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

    private String uuid;
    private SdCardInfoContract.Presenter basePresenter;

    public static SDcardDetailFragment newInstance(Bundle bundle) {
        SDcardDetailFragment fragment = new SDcardDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragement_sdcard_detail_info, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
        basePresenter = new SdCardInfoPresenterImpl(this, uuid);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customToolbar.setBackAction(o -> getFragmentManager().popBackStack());
    }

    @Override
    public void onStart() {
        super.onStart();
        initDetialData();
    }

    @Override
    public void setPresenter(SdCardInfoContract.Presenter presenter) {

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
            Toast.makeText(getContext(), getString(R.string.Clear_Sdcard_tips3), Toast.LENGTH_SHORT).show();
        } else {
            Bundle bundle = new Bundle();
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.CARRY_ON));
            bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.Clear_Sdcard_tips));
            SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.newInstance(bundle);
            simpleDialogFragment.setAction((int id, Object value) -> {
                DpMsgDefine.DPPrimary<Integer> wFlag = new DpMsgDefine.DPPrimary<>();
                wFlag.value = 0;
                basePresenter.updateInfoReq(wFlag, DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD);
                basePresenter.clearCountTime();
                showLoading();
            });
            simpleDialogFragment.show(getFragmentManager(), "simpleDialogFragment");
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

    private void initDetialData() {
        if (!basePresenter.getSdcardState()) {
            showHasNoSdDialog();
            return;
        }
        JFGDPDevice device = DataSourceManager.getInstance().getJFGDevice(this.uuid);
        //仅3G摄像头显示此栏
        if (device != null && JFGRules.is3GCam(device.pid)) {
            tvClearRestart.setVisibility(View.VISIBLE);
        }

        DpMsgDefine.DPNet net = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_201_NET);
        boolean show = net != null && JFGRules.isDeviceOnline(net);
        if (!show || NetUtils.getJfgNetType(getContext()) == 0) {
            tvClecrSdcard.setTextColor(Color.parseColor("#8c8c8c"));
            tvClecrSdcard.setEnabled(false);
        }

        DpMsgDefine.DPSdStatus sdStatus = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE);
        if (sdStatus != null) {
            long sdcardTotalCapacity = sdStatus.total;
            long sdcardUsedCapacity = sdStatus.used;
            float v = (float) ((sdcardUsedCapacity * 1.0) / sdcardTotalCapacity);
            sdUseDetail(FormetSDcardSize(sdcardUsedCapacity) + "/" + FormetSDcardSize(sdcardTotalCapacity), v);
        }
    }

    private void showHasNoSdDialog() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("ishow_cancle_btn", true);
        bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.MSG_SD_OFF));
        SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.newInstance(bundle);
        simpleDialogFragment.setAction((int id, Object value) -> {
            getFragmentManager().popBackStack();
        });
        simpleDialogFragment.show(getFragmentManager(), "simpleDialogFragment");
    }

    /**
     * desc:转换文件的大小
     *
     * @param fileS
     * @return
     */
    public String FormetSDcardSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS == 0) {
            fileSizeString = "0.0MB";
        } else if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
