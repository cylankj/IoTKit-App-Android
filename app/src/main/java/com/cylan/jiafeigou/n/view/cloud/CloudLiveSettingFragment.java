package com.cylan.jiafeigou.n.view.cloud;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.cloud.CloudLiveSettingPresenterImp;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.SettingItemView2;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    @BindView(R.id.progress_clear_record)
    ProgressBar progressClearRecord;

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
        cloudLiveDeviceInfoFragment = CloudLiveDeviceInfoFragment.newInstance(new Bundle());
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
        presenter = new CloudLiveSettingPresenterImp(this);
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

    @OnClick({R.id.tv_setting_clear_, R.id.tv_bell_detail, R.id.tv_bell_detail2})
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
        }
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
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, cloudLiveDeviceInfoFragment, "cloudLiveDeviceInfoFragment")
                .addToBackStack("cloudVideoChatConettionFragment")
                .commit();
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
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setTitle("确认清空消息记录？");
        b.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                presenter.clearMesgRecord();
                if (listener != null) {
                    listener.onClear();
                }
            }
        });
        b.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void showClearRecordProgress() {
        progressClearRecord.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideClearRecordProgress() {
        progressClearRecord.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null){
            presenter.stop();
        }
    }
}
