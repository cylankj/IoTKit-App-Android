package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamInfoPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.EditFragmentDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_BUNDLE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_LEFT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_RIGHT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TITLE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TOUCH_OUT_SIDE_DISMISS;


/**
 * 创建者     谢坤
 * 创建时间   2016/7/12 17:53
 * 用来控制摄像头模块下的设备信息，点击设备名称和设备时区时进行切换
 */
public class DeviceInfoDetailFragment extends IBaseFragment<CamInfoContract.Presenter>
        implements CamInfoContract.View {
    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.tv_device_alias)
    TextView tvDeviceAlias;
    @BindView(R.id.lLayout_information_facility_name)
    LinearLayout lLayoutInformationFacilityName;
    @BindView(R.id.tv_device_time_zone)
    TextView tvDeviceTimeZone;
    @BindView(R.id.lLayout_information_facility_timezone)
    LinearLayout lLayoutInformationFacilityTimezone;
    @BindView(R.id.tv_device_sdcard_state)
    TextView tvDeviceSdcardState;
    @BindView(R.id.tv_device_mobile_net)
    TextView tvDeviceMobileNet;
    @BindView(R.id.tv_device_wifi_state)
    TextView tvDeviceWifiState;
    @BindView(R.id.tv_device_cid)
    TextView tvDeviceCid;
    @BindView(R.id.tv_device_mac)
    TextView tvDeviceMac;
    @BindView(R.id.tv_device_system_version)
    TextView tvDeviceSystemVersion;
    @BindView(R.id.tv_device_soft_version)
    TextView tvDeviceSoftVersion;
    @BindView(R.id.tv_device_battery_level)
    TextView tvDeviceBatteryLevel;
    @BindView(R.id.tv_device_storage)
    TextView tvDeviceStorage;

    private EditFragmentDialog editDialogFragment;

    public static DeviceInfoDetailFragment newInstance(Bundle bundle) {
        DeviceInfoDetailFragment fragment = new DeviceInfoDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BeanCamInfo bean = getArguments().getParcelable(KEY_DEVICE_ITEM_BUNDLE);
        basePresenter = new CamInfoPresenterImpl(this, bean);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facility_information, null);
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
        updateDetails();
    }

    private void updateDetails() {
        BeanCamInfo p = basePresenter.getBeanCamInfo();
        if (p != null) {
            tvDeviceSdcardState.setText(getSdcardState(p.sdcardState, p.sdcardStorage));
            tvDeviceAlias.setText(p.deviceBase.alias);
            tvDeviceCid.setText(p.deviceBase.uuid);
            tvDeviceMac.setText(p.mac);
            tvDeviceBatteryLevel.setText(p.battery + "");
            tvDeviceSoftVersion.setText(p.deviceVersion);
            tvDeviceSystemVersion.setText(p.deviceSysVersion);
        }
    }

    private String getSdcardState(boolean sd, DpMsgDefine.SdStatus sdStatus) {
        if (!sd) {
            return getString(R.string.SD_ERR_1);
        }
        if (sdStatus != null) {
            int sdState = sdStatus.err;
            if (sdState == 0) {
                return getString(R.string.SD_NORMAL);
            }
        }
        return "";
    }

    @OnClick({R.id.imgV_top_bar_center, R.id.lLayout_information_facility_name, R.id.lLayout_information_facility_timezone})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_top_bar_center:
                getActivity().onBackPressed();
                break;
            case R.id.lLayout_information_facility_name:
                toEditAlias();
                break;
            case R.id.lLayout_information_facility_timezone:
                toEditTimezone();

                break;
        }
    }

    /**
     * 编辑时区
     */
    private void toEditTimezone() {

        DeviceTimeZoneFragment timeZoneFragment = DeviceTimeZoneFragment.newInstance(getArguments());
        timeZoneFragment.setCallBack(new CallBack() {
            @Override
            public void callBack(Object zone) {
                if (!(zone instanceof TimeZoneBean)) {
                    return;
                }
                TimeZoneBean timeZoneBean = (TimeZoneBean) zone;
                BeanCamInfo info = basePresenter.getBeanCamInfo();
                DpMsgDefine.MsgTimeZone timeZone = info.deviceTimeZone == null ? new DpMsgDefine.MsgTimeZone() : info.deviceTimeZone;
                timeZone.timezone = timeZoneBean.getGmt();
                info.deviceTimeZone = timeZone;
                basePresenter.updateInfo(info);
                updateDetails();
                Log.d("CYLAN_TAG", "timezone: " + timeZoneBean);
            }
        });
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                timeZoneFragment, android.R.id.content);
    }

    /**
     * 编辑昵称
     */
    private void toEditAlias() {
        if (editDialogFragment == null) {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_TITLE, getString(R.string.EQUIPMENT_NAME));
            bundle.putString(KEY_LEFT_CONTENT, getString(R.string.OK));
            bundle.putString(KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
            bundle.putBoolean(KEY_TOUCH_OUT_SIDE_DISMISS, false);
            editDialogFragment = EditFragmentDialog.newInstance(bundle);
        }
        if (editDialogFragment.isVisible())
            return;
        editDialogFragment.show(getChildFragmentManager(), "editDialogFragment");
        editDialogFragment.setAction(new EditFragmentDialog.DialogAction<String>() {
            @Override
            public void onDialogAction(int id, String value) {
                if (id == R.id.tv_confirm) {
                    tvDeviceAlias.setText(value);
                    if (basePresenter != null) {
                        BeanCamInfo info = basePresenter.getBeanCamInfo();
                        if (!TextUtils.equals(info.deviceBase.alias, value)) {
                            info.deviceBase.alias = value;
                            basePresenter.updateInfo(info);
                            updateDetails();
                            Log.d("BeanCamInfo", "BeanCamInfo: update alias: " + value);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void setPresenter(CamInfoContract.Presenter presenter) {
        basePresenter = presenter;
    }
}