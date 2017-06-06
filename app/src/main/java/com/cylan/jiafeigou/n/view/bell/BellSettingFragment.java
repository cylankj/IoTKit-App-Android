package com.cylan.jiafeigou.n.view.bell;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.view.activity.BindBellActivity;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.JUST_SEND_INFO;
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

    private SimpleDialogFragment mClearRecordFragment;

    public static BellSettingFragment newInstance(String uuid) {
        BellSettingFragment fragment = new BellSettingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getContentViewID() {
        return R.layout.layout_fragment_bell_setting;
    }

    @Override
    protected void setFragmentComponent(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initTopBar();
    }

    private void initTopBar() {
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
                int cnet = NetUtils.getJfgNetType(getActivity());
                if (cnet == 0) {
                    ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                if (mClearRecordFragment == null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.Tap1_Tipsforclearrecents));
                    bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.CANCEL));
                    bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.DELETE));
                    mClearRecordFragment = SimpleDialogFragment.newInstance(bundle);
                }
                mClearRecordFragment.setAction((id, value) -> {
                    switch (id) {
                        case R.id.tv_dialog_btn_right:
                            presenter.clearBellRecord(mUUID);
                            LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.DELETEING));
                    }
                });
                mClearRecordFragment.show(getActivity().getSupportFragmentManager(), "ClearBellRecordFragment");
                break;
            case R.id.tv_setting_unbind:
                ViewUtils.deBounceClick(view);
                if (!NetUtils.isPublicNetwork()) {
                    ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                Device device = sourceManager.getDevice(mUUID);
                String name = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.SURE_DELETE_1, name))
                        .setPositiveButton(getString(R.string.OK), (DialogInterface dialogInterface, int i) -> {
                            presenter.unbindDevice();
                            LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.DELETEING));
                        })
                        .setNegativeButton(getString(R.string.CANCEL), null);
                AlertDialogManager.getInstance().showDialog(getString(R.string.SURE_DELETE_1, name), getActivity(), builder);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void handleJumpToConfig() {
        String uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
        Device device = sourceManager.getDevice(uuid);
        if (device == null) {
            getActivity().finish();
            return;
        }
        Intent intent = new Intent(getActivity(), BindBellActivity.class);
        intent.putExtra(JUST_SEND_INFO, uuid);
        startActivity(intent);
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

        DpMsgDefine.DPNet net = sourceManager.getDevice(mUUID).$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
        if (net != null) svSettingDeviceWifi.setTvSubTitle(DpMsgDefine.DPNet.getNormalString(net));
        tvSettingClear.setVisibility(TextUtils.isEmpty(device.shareAccount) ? View.VISIBLE : View.GONE);
        mNetWorkContainer.setVisibility(TextUtils.isEmpty(device.shareAccount) ? View.VISIBLE : View.GONE);
    }
}
