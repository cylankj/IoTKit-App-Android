package com.cylan.jiafeigou.n.view.cloud;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveSettingContract;
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

    private CloudLiveDeviceInfoFragment cloudLiveDeviceInfoFragment;

    public static CloudLiveSettingFragment newInstance(Bundle bundle) {
        CloudLiveSettingFragment fragment = new CloudLiveSettingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cloudLiveDeviceInfoFragment = CloudLiveDeviceInfoFragment.newInstance(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_live_setting, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(CloudLiveSettingContract.Presenter presenter) {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initTopBar();
    }


    private void initTopBar() {
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }

    @OnClick({R.id.tv_setting_clear_,R.id.tv_bell_detail, R.id.tv_bell_detail2})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_setting_clear_:

                break;
            case R.id.tv_bell_detail:

                break;
            case R.id.tv_bell_detail2:
                jump2DeviceInfoFragment();
                break;
        }
    }

    private void jump2DeviceInfoFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, cloudLiveDeviceInfoFragment, "cloudLiveDeviceInfoFragment")
                .addToBackStack("cloudVideoChatConettionFragment")
                .commit();
    }
}
