package com.cylan.jiafeigou.n.view.mag;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mag.MagLiveInformationContract;
import com.cylan.jiafeigou.n.mvp.impl.mag.MagLiveInformationPresenterImp;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者     谢坤
 * 创建时间   2016/7/12 17:53
 * 用来控制摄像头模块下的设备信息，点击设备名称和设备时区时进行切换
 */
public class MagLiveInformationFragment extends Fragment implements MagLiveInformationContract.View {


    public static final String KEY_TITLE = "key_title";
    @BindView(R.id.iv_mag_information_back)
    ImageView ivMagInformationBack;
    @BindView(R.id.tv_mag_information_facility_name)
    TextView tvMagInformationFacilityName;
    @BindView(R.id.lLayout_mag_information_facility_name)
    LinearLayout lLayoutMagInformationFacilityName;
    @BindView(R.id.tv_mag_information_facility_time_zone)
    TextView tvMagInformationFacilityTimeZone;
    @BindView(R.id.lLayout_mag_information_facility_timezone)
    LinearLayout lLayoutMagInformationFacilityTimezone;
    @BindView(R.id.tv_device_sdcard_state)
    TextView tvInformationFacilityCardState;
    @BindView(R.id.ll_information_facility_card_state)
    LinearLayout llInformationFacilityCardState;
    @BindView(R.id.tv_device_wifi_state)
    TextView tvInformationFacilityWifiState;
    @BindView(R.id.tv_device_cid)
    TextView tvInformationFacilityCidNumber;
    @BindView(R.id.tv_device_battery_level)
    TextView tvInformationFacilityBatteryLeve;
    @BindView(R.id.tv_device_storage)
    TextView tvInformationFacilityStorage;
    @BindView(R.id.tv_device_mobile_net)
    TextView tvInformationFacilityNet;

    //实例化fragment对象
    private MagDeviceNameDialogFragment magDeviceNameDialogFragment;
    private MagLiveInformationContract.Presenter presenter;

    private OnMagLiveDataChangeListener mListener;

    public void setListener(OnMagLiveDataChangeListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void setPresenter(MagLiveInformationContract.Presenter presenter) {

    }

    @Override
    public void initSdCardState(int state) {

        switch (state) {
            case MagLiveInformationPresenterImp.SD_NORMAL:          //sdcard正常显示
                tvInformationFacilityCardState.setText("正常使用");
                break;

            case MagLiveInformationPresenterImp.SD_UNINSTALL:       //SD卡不存在
                tvInformationFacilityCardState.setText("插入SD卡");
                break;

            case MagLiveInformationPresenterImp.SD_FAIL_RW:         //Sd卡读写失败
                tvInformationFacilityCardState.setText("读写失败");
                break;
        }

    }

    public interface OnMagLiveDataChangeListener {
        void magLiveDataChange(String content);
    }

    public static MagLiveInformationFragment newInstance(Bundle bundle) {
        MagLiveInformationFragment fragment = new MagLiveInformationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        magDeviceNameDialogFragment = MagDeviceNameDialogFragment.newInstance(new Bundle());
        //在执行该回调方法前，可以进行数据的预先加载
        //在oncreatView之前，把fragment页面的布局内的需要修改的信息，修改一下。
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mag_facility_information, null);
        ButterKnife.bind(this, view);
        initPresenter();
        initFacilityInfo();
        return view;
    }

    private void initPresenter() {
        presenter = new MagLiveInformationPresenterImp(this);
    }

    private void initFacilityInfo() {
        tvMagInformationFacilityName.setText(getArguments().getString(KEY_TITLE));
        tvInformationFacilityNet.setText(presenter.getMobileType());
        tvInformationFacilityWifiState.setText(presenter.getWifiState());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mListener != null) {
            mListener.magLiveDataChange(tvMagInformationFacilityName.getText().toString());
        }
    }

    public void onStart() {
        super.onStart();
        String editName = PreferencesUtils.getString("magEditName", "客厅摄像头");
        tvMagInformationFacilityName.setText(editName);
        String detailText = PreferencesUtils.getString("magDetailText", "北京/中国");
        tvMagInformationFacilityTimeZone.setText(detailText);
        initSdCardState(presenter.checkSdCard());
    }

    @OnClick({R.id.iv_mag_information_back, R.id.lLayout_mag_information_facility_name,
            R.id.lLayout_mag_information_facility_timezone, R.id.ll_information_facility_card_state})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_mag_information_back:
                getActivity().onBackPressed();
                break;
            case R.id.lLayout_mag_information_facility_name:
                magDeviceNameDialogFragment.show(getActivity().getFragmentManager(),
                        "MsgDeviceNameDialogFragment");
                if (getActivity() != null && getActivity().getFragmentManager() != null) {
                    magDeviceNameDialogFragment.setListener(new MagDeviceNameDialogFragment.OnMagDataChangeListener() {
                        @Override
                        public void magDataChangeListener(String content) {
                            tvMagInformationFacilityName.setText(content);
                        }
                    });
                }
                break;
            case R.id.lLayout_mag_information_facility_timezone:
                break;
            case R.id.ll_information_facility_card_state:
                onSdcardClick();
                break;
        }
    }

    /**
     * desc：Sd卡不同状态的点击处理
     */
    public void onSdcardClick() {
        switch (presenter.checkSdCard()) {
            case MagLiveInformationPresenterImp.SD_NORMAL:
                llInformationFacilityCardState.setEnabled(false);
                break;

            case MagLiveInformationPresenterImp.SD_UNINSTALL:
                ToastUtil.showToast("请插入SD卡");
                break;

            case MagLiveInformationPresenterImp.SD_FAIL_RW:
                showFormatSDDialog();
                break;
        }
    }

    /**
     * desc：弹出格式化sd卡的对话框
     */
    private void showFormatSDDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("格式化SD卡");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ToastUtil.showToast("正在格式化中。。。");
                dialog.dismiss();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
}
