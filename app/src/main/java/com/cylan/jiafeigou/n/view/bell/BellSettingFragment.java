package com.cylan.jiafeigou.n.view.bell;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellSettingPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.BindBellActivity;
import com.cylan.jiafeigou.n.view.activity.ConfigWifiActivity_2;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;

import butterknife.BindView;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;
import static com.cylan.jiafeigou.utils.ActivityUtils.loadFragment;

public class BellSettingFragment extends BaseFragment<BellSettingContract.Presenter>
        implements BellSettingContract.View {

    @BindView(R.id.sv_setting_device_detail)
    SettingItemView0 svSettingDeviceDetail;
    @BindView(R.id.sv_setting_device_wifi)
    SettingItemView0 svSettingDeviceWifi;
    @BindView(R.id.tv_setting_clear_)
    TextView tvSettingClear;
    @BindView(R.id.tv_setting_unbind)
    TextView tvSettingUnbind;
    @BindView(R.id.lLayout_setting_container)
    LinearLayout lLayoutSettingContainer;
    @BindView(R.id.ll_bell_net_work_container)
    LinearLayout mNetWorkContainer;

    //    private SimpleDialogFragment mClearRecordFragment;
    private AlertDialog mClearRecordDialog;

    public static BellSettingFragment newInstance(String uuid) {
        BellSettingFragment fragment = new BellSettingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected BellSettingContract.Presenter onCreatePresenter() {
        return new BellSettingPresenterImpl();
    }

    @Override
    protected int getContentViewID() {
        return R.layout.layout_fragment_bell_setting;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initTopBar();
    }

    private void initTopBar() {
//        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }


    @OnClick({R.id.tv_toolbar_icon,
            R.id.sv_setting_device_detail,
            R.id.sv_setting_device_wifi,
            R.id.tv_setting_clear_,
            R.id.tv_setting_unbind})
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);//防重复点击
        switch (view.getId()) {
            case R.id.sv_setting_device_detail: {
                BellDetailFragment fragment = BellDetailFragment.newInstance(null);
                Bundle bundle = new Bundle();
                bundle.putString(KEY_DEVICE_ITEM_UUID, mUUID);

                fragment.setArguments(bundle);
                loadFragment(android.R.id.content, getActivity().getSupportFragmentManager(), fragment);
            }
            break;
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.sv_setting_device_wifi:
                handleJumpToConfig();
                break;
            case R.id.tv_setting_clear_:
                ViewUtils.deBounceClick(view);
                if (mClearRecordDialog != null && mClearRecordDialog.isShowing()) return;
                if (mClearRecordDialog == null) {
                    mClearRecordDialog = new AlertDialog.Builder(getActivity())
                            .setMessage(getString(R.string.Tap1_Tipsforclearrecents))
                            .setNegativeButton(getString(R.string.CANCEL), null)
                            .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mPresenter.clearBellRecord(mUUID);
                                    LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.DELETEING));
                                }
                            })
                            .create();
                }
                mClearRecordDialog.show();
                break;
            case R.id.tv_setting_unbind:
                ViewUtils.deBounceClick(view);
                int net = NetUtils.getJfgNetType(getActivity());
                if (net == 0) {
                    ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                Device device = DataSourceManager.getInstance().getJFGDevice(mUUID);
                String name = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.SURE_DELETE_1, name))
                        .setPositiveButton(getString(R.string.OK), (DialogInterface dialogInterface, int i) -> {
                            mPresenter.unbindDevice();
                            LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.DELETEING));
                        })
                        .setNegativeButton(getString(R.string.CANCEL), null)
                        .create().show();
                break;
        }
    }

    private void handleJumpToConfig() {
        String uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
        if (device == null) {
            getActivity().finish();
            return;
        }
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        if (!JFGRules.isDeviceOnline(net)) {
            //设备离线
            Intent intent = new Intent(getActivity(), BindBellActivity.class);
            startActivity(intent);
        } else {
            //设备在线
            String localSSid = NetUtils.getNetName(ContextUtils.getContext());
            String remoteSSid = net.ssid;
            if (!TextUtils.equals(localSSid, remoteSSid)) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.setwifi_check, remoteSSid))
                        .setNegativeButton(getString(R.string.CANCEL), null)
                        .setPositiveButton(getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        })
                        .show();
            } else {
                //设备离线
                Intent intent = new Intent(getActivity(), ConfigWifiActivity_2.class);
                intent.putExtra(JConstant.JUST_SEND_INFO, JConstant.JUST_SEND_INFO);
                startActivity(intent);
            }
        }
    }

    @Override
    public void unbindDeviceRsp(int state) {
        if (state == JError.ErrorOK) {
            LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
            ToastUtil.showPositiveToast(getString(R.string.DELETED_SUC));
            getActivity().finish();
        }
    }

    @Override
    public void onClearBellRecordSuccess() {
        ToastUtil.showPositiveToast(getString(R.string.Clear_Sdcard_tips3));
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onClearBellRecordFailed() {
        ToastUtil.showNegativeToast(getString(R.string.Clear_Sdcard_tips4));
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onShowProperty(Device device) {
        svSettingDeviceDetail.setTvSubTitle(TextUtils.isEmpty(device.alias)
                ? device.uuid : device.alias);
        if (!TextUtils.isEmpty(device.shareAccount)) {
            final int count = lLayoutSettingContainer.getChildCount();
            for (int i = 3; i < count - 2; i++) {
                View v = lLayoutSettingContainer.getChildAt(i);
                v.setVisibility(View.GONE);
            }
        }
//        svSettingDeviceWifi.setTvSubTitle(DpMsgDefine.DPNet.getNormalString(device.$(DpMsgMap.ID_201_NET, null)));

        DpMsgDefine.DPNet net = DataSourceManager.getInstance().getJFGDevice(mUUID).$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
        if (net != null) svSettingDeviceWifi.setTvSubTitle(DpMsgDefine.DPNet.getNormalString(net));
        tvSettingClear.setVisibility(TextUtils.isEmpty(device.shareAccount) ? View.VISIBLE : View.GONE);
        mNetWorkContainer.setVisibility(TextUtils.isEmpty(device.shareAccount) ? View.VISIBLE : View.GONE);
    }
}
