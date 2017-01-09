package com.cylan.jiafeigou.n.view.cloud;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.cloud.CloudLiveSettingPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.BeanCloudInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView2;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_BUNDLE;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLiveSettingFragment extends Fragment implements CloudLiveSettingContract.View {

    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.tv_bell_detail)
    SettingItemView2 tvBellDetail;
    @BindView(R.id.tv_bell_detail2)
    SettingItemView2 tvBellDetail2;
    @BindView(R.id.tv_setting_clear_)
    TextView tvSettingClear;
    @BindView(R.id.tv_setting_unbind)
    TextView tvSettingUnbind;
    @BindView(R.id.tv_door_bell)
    TextView tvDoorBell;

    private CloudLiveDeviceInfoFragment cloudLiveDeviceInfoFragment;
    private CloudCorrelationDoorBellFragment cloudCorrelationDoorBellFragment;
    private CloudLiveSettingContract.Presenter presenter;

    private OnClearMesgRecordListener listener;

    public interface OnClearMesgRecordListener {
        void onClear();
    }

    public void setOnClearMesgRecordListener(OnClearMesgRecordListener listener) {
        this.listener = listener;
    }

    public static CloudLiveSettingFragment newInstance(Bundle bundle) {
        CloudLiveSettingFragment fragment = new CloudLiveSettingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cloudCorrelationDoorBellFragment = CloudCorrelationDoorBellFragment.newInstance(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_live_setting, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        return view;
    }

    private void initPresenter() {
        Bundle bundle = getArguments();
        DeviceBean deviceBean = (DeviceBean) bundle.getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        presenter = new CloudLiveSettingPresenterImp(this, deviceBean);
    }

    @Override
    public void setPresenter(CloudLiveSettingContract.Presenter presenter) {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initTopBar();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.start();
        }
    }

    private void initTopBar() {
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }

    @OnClick({R.id.tv_setting_clear_, R.id.tv_bell_detail, R.id.tv_bell_detail2, R.id.tv_setting_unbind})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_setting_clear_:
                showClearRecordDialog();
                break;
            case R.id.tv_bell_detail:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_bell_detail));
                AppLogger.e("tv_bell_detail");
                jump2DeviceInfoFragment();
                break;
            case R.id.tv_bell_detail2:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_bell_detail2));
                AppLogger.e("tv_bell_detail2");
                jump2CorrelationDoorBellFragment();
                break;
            case R.id.tv_setting_unbind:
                showClearDeviceDialog();
                break;
        }
    }

    /**
     * desc：删除设备弹出框
     */
    private void showClearDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(String.format(getString(R.string.SURE_DELETE_1),presenter.getDeviceName()));
        builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (RxBus.getCacheInstance().hasObservers()) {
                    RxBus.getCacheInstance().post(new RxEvent.CloudLiveDelete());
                }
                ToastUtil.showToast(getString(R.string.DELETEING));
            }
        });
        builder.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();

    }

    private void jump2CorrelationDoorBellFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, cloudCorrelationDoorBellFragment, "cloudCorrelationDoorBellFragment")
                .addToBackStack("cloudVideoChatConettionFragment")
                .commit();
    }

    private void jump2DeviceInfoFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DEVICE_ITEM_BUNDLE, presenter.getCloudInfoBean());
        cloudLiveDeviceInfoFragment = CloudLiveDeviceInfoFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, cloudLiveDeviceInfoFragment, "cloudLiveDeviceInfoFragment")
                .addToBackStack("cloudVideoChatConettionFragment")
                .commit();
        initListener();
    }

    /**
     * 设置设备别名回调监听
     */
    private void initListener() {
        cloudLiveDeviceInfoFragment.setOnChangeNameListener(new CloudLiveDeviceInfoFragment.OnChangeNameListener() {
            @Override
            public void changeName(String name) {
                tvBellDetail.setTvSubTitle(name);
            }
        });
    }

    @Override
    public void initSomeViewVisible(boolean isVisible) {
        if (isVisible) {
            tvDoorBell.setVisibility(View.GONE);
            tvSettingClear.setVisibility(View.GONE);
            tvBellDetail2.setVisibility(View.GONE);
        } else {
            tvDoorBell.setVisibility(View.VISIBLE);
            tvSettingClear.setVisibility(View.VISIBLE);
            tvBellDetail2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showClearRecordDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(getString(R.string.Tap1_Tipsforclearrecents));
        b.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                presenter.clearMesgRecord();
                if (listener != null) {
                    listener.onClear();
                }
            }
        });
        b.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void showClearRecordProgress() {
        LoadingDialog.showLoading(getFragmentManager(),getString(R.string.ClearingTips));
    }

    @Override
    public void hideClearRecordProgress() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    /**
     * 设置设备名称
     *
     * @param beanCloudInfo
     */
    @Override
    public void onCloudInfoRsp(BeanCloudInfo beanCloudInfo) {
        tvBellDetail.setTvSubTitle(presenter.getDeviceName());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }
}
