package com.cylan.jiafeigou.n.view.bell;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.mvp.model.BellInfoBean;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.SettingItemView2;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BellSettingFragment extends Fragment
        implements BellSettingContract.View,
        SimpleDialogFragment.SimpleDialogAction {

    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.tv_bell_detail)
    SettingItemView2 tvBellDetail;
    @BindView(R.id.tv_bell_wifi)
    SettingItemView2 tvBellWifi;
    private BellSettingContract.Presenter presenter;
    private SimpleDialogFragment simpleDialogFragment;


    public static BellSettingFragment newInstance(Bundle bundle) {
        BellSettingFragment fragment = new BellSettingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    @Override
    public void onStart() {
        super.onStart();
        presenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null)
            presenter.stop();
    }

    private void initTopBar() {
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }


    @OnClick({R.id.imgV_top_bar_center,
            R.id.tv_bell_detail,
            R.id.tv_bell_wifi,
            R.id.tv_setting_clear_,
            R.id.tv_setting_unbind})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_top_bar_center:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.tv_bell_detail:
                break;
            case R.id.tv_bell_wifi:
                break;
            case R.id.tv_setting_clear_:
                break;
            case R.id.tv_setting_unbind:
                if (simpleDialogFragment == null) {
                    simpleDialogFragment = SimpleDialogFragment.newInstance(new Bundle());
                    simpleDialogFragment.setAction(this);
                }
                simpleDialogFragment.show(getActivity().getSupportFragmentManager(), "simpleDialogFragment");
                break;
        }
    }

    @Override
    public void onSettingInfoRsp(BellInfoBean bellInfoBean) {
        tvBellDetail.setTvSubTitle(bellInfoBean.nickName);
        tvBellWifi.setTvSubTitle(bellInfoBean.ssid);
    }

    @Override
    public void onLoginState(boolean state) {
    }

    @Override
    public void setPresenter(BellSettingContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onDialogAction(int id, Object value) {
        switch (id) {
            case R.id.tv_dialog_btn_left:
                Bundle bundle = getArguments();
                if (bundle == null) {
                    AppLogger.d("bundle is null");
                    return;
                }
                Object o = bundle.getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
                if (o != null) {
                    bundle.putParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE, (Parcelable) o);
                }
                if (o instanceof DeviceBean) {
                    AppLogger.d("yes get it");
                }
                bundle.putInt(JConstant.KEY_ACTIVITY_RESULT_CODE, JConstant.RESULT_CODE_REMOVE_ITEM);
                bundle.putString(JConstant.KEY_REMOVE_DEVICE, JConstant.KEY_REMOVE_DEVICE);
                RxEvent.ActivityResult result = new RxEvent.ActivityResult();
                result.bundle = bundle;
                if (presenter != null) presenter.sendActivityResult(result);
                break;
        }

    }
}
