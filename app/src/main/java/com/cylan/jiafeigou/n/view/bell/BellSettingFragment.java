package com.cylan.jiafeigou.n.view.bell;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDPDevice;
import com.cylan.jiafeigou.base.module.JFGDoorBellDevice;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellSettingPresenterImpl;
import com.cylan.jiafeigou.n.view.bind.BindDoorBellFragment;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;

import static com.cylan.jiafeigou.utils.ActivityUtils.loadFragment;

public class BellSettingFragment extends BaseFragment<BellSettingContract.Presenter>
        implements BellSettingContract.View,
        BaseDialog.BaseDialogAction {

    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;

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

    private SimpleDialogFragment simpleDialogFragment;
    private SimpleDialogFragment mClearRecordFragment;


    public static BellSettingFragment newInstance(Bundle bundle) {
        BellSettingFragment fragment = new BellSettingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static BellSettingFragment newInstance(String uuid) {
        BellSettingFragment fragment = new BellSettingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
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
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }


    @OnClick({R.id.imgV_top_bar_center,
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
                bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, mUUID);
                fragment.setArguments(bundle);
                loadFragment(android.R.id.content, getActivity().getSupportFragmentManager(), fragment);
            }
            break;
            case R.id.imgV_top_bar_center:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.sv_setting_device_wifi:
                Bundle setWiFi = new Bundle();
                BindDoorBellFragment fragment = BindDoorBellFragment.newInstance(setWiFi);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_down_out
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, fragment)
                        .addToBackStack("BindDoorBellFragment")
                        .commit();
                break;
            case R.id.tv_setting_clear_:
                ViewUtils.deBounceClick(view);
                if (mClearRecordFragment == null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.Tap1_Tipsforclearrecents));
                    mClearRecordFragment = SimpleDialogFragment.newInstance(bundle);
                }
                mClearRecordFragment.setAction((id, value) -> {
                    switch (id) {
                        case R.id.tv_dialog_btn_left:
                            mPresenter.clearBellRecord(mUUID);
                            LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.DELETEING));
                    }
                });
                mClearRecordFragment.show(getActivity().getSupportFragmentManager(), "ClearBellRecordFragment");
                break;
            case R.id.tv_setting_unbind:
                ViewUtils.deBounceClick(view);
                if (simpleDialogFragment == null) {
                    Bundle bundle = new Bundle();
                    JFGDPDevice device = DataSourceManager.getInstance().getJFGDevice(mUUID);
                    String name = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
                    bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.SURE_DELETE_1, name));
                    simpleDialogFragment = SimpleDialogFragment.newInstance(bundle);
                }
                simpleDialogFragment.setAction(this);
                simpleDialogFragment.show(getActivity().getSupportFragmentManager(), "simpleDialogFragment");
                break;
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
    public void onDialogAction(int id, Object value) {
        switch (id) {
            case R.id.tv_dialog_btn_left:
                mPresenter.unbindDevice();
                LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.DELETEING));
                break;
        }

    }

    @Override
    public void onShowProperty(JFGDoorBellDevice device) {
        svSettingDeviceDetail.setTvSubTitle(TextUtils.isEmpty(device.alias)
                ? device.uuid : device.alias);
        if (!TextUtils.isEmpty(device.shareAccount)) {
            final int count = lLayoutSettingContainer.getChildCount();
            for (int i = 3; i < count - 2; i++) {
                View v = lLayoutSettingContainer.getChildAt(i);
                v.setVisibility(View.GONE);
            }
        }
        svSettingDeviceWifi.setTvSubTitle(DpMsgDefine.DPNet.getNormalString(device.net));
        tvSettingClear.setVisibility(TextUtils.isEmpty(device.shareAccount) ? View.VISIBLE : View.GONE);
        mNetWorkContainer.setVisibility(TextUtils.isEmpty(device.shareAccount) ? View.VISIBLE : View.GONE);
    }
}
