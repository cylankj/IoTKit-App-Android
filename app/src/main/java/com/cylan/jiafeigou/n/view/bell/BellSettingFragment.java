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
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.utils.ActivityUtils.loadFragment;

public class BellSettingFragment extends IBaseFragment<BellSettingContract.Presenter>
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeviceBean bean = getArguments().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        basePresenter = new BellSettingPresenterImpl(this, bean);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_bell_setting, container, false);
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
                bundle.putParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE, basePresenter.getBellInfo());
                fragment.setArguments(bundle);
                fragment.setCallBack(new IBaseFragment.CallBack() {
                    @Override
                    public void callBack(Object t) {
                        onSettingInfoRsp(basePresenter.getBellInfo());
                    }
                });
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
                    bundle.putString(SimpleDialogFragment.KEY_TITLE, getString(R.string.DELETE_CID));
                    simpleDialogFragment = SimpleDialogFragment.newInstance(bundle);
                }
                simpleDialogFragment.setAction(this);
                simpleDialogFragment.show(getActivity().getSupportFragmentManager(), "simpleDialogFragment");
                break;
        }
    }

    @Override
    public void onSettingInfoRsp(BeanBellInfo bellInfoBean) {
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
    public void setPresenter(BellSettingContract.Presenter presenter) {
        this.basePresenter = presenter;
    }

    @Override
    public void onDialogAction(int id, Object value) {
        switch (id) {
            case R.id.tv_dialog_btn_left:
                basePresenter.deleteDevice();
                getActivity().finish();
                break;
        }

    }
}
