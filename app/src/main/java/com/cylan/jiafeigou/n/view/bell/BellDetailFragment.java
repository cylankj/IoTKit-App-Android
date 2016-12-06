package com.cylan.jiafeigou.n.view.bell;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellDetailSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.EditFragmentDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_LEFT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_RIGHT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TITLE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TOUCH_OUT_SIDE_DISMISS;

public class BellDetailFragment extends IBaseFragment<BellDetailContract.Presenter>
        implements BellDetailContract.View,
        BaseDialog.BaseDialogAction {

    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.sv_setting_device_alias)
    SettingItemView0 svSettingDeviceAlias;
    @BindView(R.id.sv_setting_device_cid)
    SettingItemView0 svSettingDeviceCid;
    @BindView(R.id.sv_setting_device_wifi)
    SettingItemView0 svSettingDeviceWifi;
    @BindView(R.id.sv_setting_device_mac)
    SettingItemView0 svSettingDeviceMac;
    @BindView(R.id.sv_setting_device_sys_version)
    SettingItemView0 svSettingDeviceSysVersion;
    @BindView(R.id.sv_setting_device_version)
    SettingItemView0 svSettingDeviceVersion;
    @BindView(R.id.sv_setting_device_battery)
    SettingItemView0 svSettingDeviceBattery;
    @BindView(R.id.lLayout_setting_container)
    LinearLayout lLayoutSettingContainer;

    public static BellDetailFragment newInstance(Bundle bundle) {
        BellDetailFragment fragment = new BellDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BeanBellInfo info = getArguments().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        basePresenter = new BellDetailSettingPresenterImpl(this, info);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_bell_detail_setting, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initTopBar();
    }

    private void initTopBar() {
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }


    @Override
    public void onSettingInfoRsp(BeanBellInfo bellInfoBean) {
        String alias = TextUtils.isEmpty(bellInfoBean.deviceBase.alias)
                ? bellInfoBean.deviceBase.uuid : bellInfoBean.deviceBase.alias;
        svSettingDeviceAlias.setTvSubTitle(alias);
        svSettingDeviceCid.setTvSubTitle(bellInfoBean.deviceBase.uuid);
        svSettingDeviceMac.setTvSubTitle(bellInfoBean.mac == null ? "" : bellInfoBean.mac);
        svSettingDeviceSysVersion.setTvSubTitle(bellInfoBean.deviceSysVersion == null ? "" : bellInfoBean.deviceSysVersion);
        svSettingDeviceVersion.setTvSubTitle(bellInfoBean.deviceVersion == null ? "" : bellInfoBean.deviceVersion);
        svSettingDeviceBattery.setTvSubTitle(bellInfoBean.battery + "");
        String ssid = bellInfoBean.net == null || bellInfoBean.net.ssid == null ?
                getString(R.string.OFF_LINE) : bellInfoBean.net.ssid;
        svSettingDeviceWifi.setTvSubTitle(ssid);
    }


    @Override
    public void setPresenter(BellDetailContract.Presenter presenter) {
        basePresenter = presenter;
    }

    @Override
    public void onDialogAction(int id, Object value) {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (callBack != null)
            callBack.callBack(null);
    }

    private EditFragmentDialog editDialogFragment;

    @OnClick(R.id.sv_setting_device_alias)
    public void onClick() {
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
                if (basePresenter != null) {
                    BeanBellInfo info = basePresenter.getBellInfo();
                    if (!TextUtils.isEmpty(value)
                            && !TextUtils.equals(info.deviceBase.alias, value)) {
                        svSettingDeviceAlias.setTvSubTitle(value);
                        info.deviceBase.alias = value;
                        basePresenter.saveBellInfo(info, DpMsgMap.ID_2000003_BASE_ALIAS);
                    }
                }
            }
        });
    }
}
