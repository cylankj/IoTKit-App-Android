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
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    private SimpleDialogFragment simpleDialogFragment;


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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeviceBean bean = getArguments().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_bell_setting, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected BellSettingContract.Presenter onCreatePresenter() {
        return new BellSettingPresenterImpl();
    }

    @Override
    protected int getContentViewID() {
        return 0;
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
        switch (view.getId()) {
            case R.id.sv_setting_device_detail: {
                BellDetailFragment fragment = BellDetailFragment.newInstance(null);
                Bundle bundle = new Bundle();
                bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, mUUID);
                fragment.setArguments(bundle);
                fragment.setCallBack(t -> onSettingInfoRsp(mPresenter.getBellInfo()));
                loadFragment(android.R.id.content, getActivity().getSupportFragmentManager(), fragment);
            }
            break;
            case R.id.imgV_top_bar_center:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.sv_setting_device_wifi:
                break;
            case R.id.tv_setting_clear_:
                break;
            case R.id.tv_setting_unbind:
                ViewUtils.deBounceClick(view);
                if (simpleDialogFragment == null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.DELETE_CID));
                    simpleDialogFragment = SimpleDialogFragment.newInstance(bundle);
                }
                simpleDialogFragment.setAction(this);
                simpleDialogFragment.show(getActivity().getSupportFragmentManager(), "simpleDialogFragment");
                break;
        }
    }

    @Override
    public void onSettingInfoRsp(BeanBellInfo bellInfoBean) {
        if (bellInfoBean.deviceBase != null && !TextUtils.isEmpty(bellInfoBean.deviceBase.shareAccount)) {
            //分享账号
            final int count = lLayoutSettingContainer.getChildCount();
            for (int i = 3; i < count; i++) {
                View v = lLayoutSettingContainer.getChildAt(i);
                if (v != null) {
                    v.setVisibility(View.GONE);
                }
            }
        }
        svSettingDeviceDetail.setTvSubTitle(TextUtils.isEmpty(bellInfoBean.deviceBase.alias)
                ? bellInfoBean.deviceBase.uuid : bellInfoBean.deviceBase.alias);
        String ssid = bellInfoBean.net == null || TextUtils.isEmpty(bellInfoBean.net.ssid)
                ? getString(R.string.OFF_LINE) : bellInfoBean.net.ssid;
        svSettingDeviceWifi.setTvSubTitle(ssid);
        if (!TextUtils.isEmpty(bellInfoBean.deviceBase.shareAccount)) {
            final int count = lLayoutSettingContainer.getChildCount();
            for (int i = 3; i < count - 2; i++) {
                View v = lLayoutSettingContainer.getChildAt(i);
                v.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoginState(boolean state) {
    }

    @Override
    public void unbindDeviceRsp(int state) {
        if (state == JError.ErrorOK) {
            LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
            ToastUtil.showPositiveToast(getString(R.string.DOOR_UNBIND));
            getActivity().finish();
        }
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
    public String onResolveViewLaunchType() {
        return null;
    }
}
