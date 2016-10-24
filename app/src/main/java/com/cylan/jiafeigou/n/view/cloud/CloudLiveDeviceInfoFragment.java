package com.cylan.jiafeigou.n.view.cloud;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveDeviceInfoContract;
import com.cylan.jiafeigou.n.view.mag.MagDeviceNameDialogFragment;
import com.cylan.jiafeigou.n.view.mag.MagDeviceTimeZoneFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLiveDeviceInfoFragment extends Fragment implements CloudLiveDeviceInfoContract.View {

    @BindView(R.id.iv_information_back)
    ImageView ivInformationBack;
    @BindView(R.id.tv_information_facility_name)
    TextView tvInformationFacilityName;
    @BindView(R.id.lLayout_information_facility_name)
    LinearLayout lLayoutInformationFacilityName;
    @BindView(R.id.tv_information_facility_time_zone)
    TextView tvInformationFacilityTimeZone;
    @BindView(R.id.lLayout_information_facility_timezone)
    LinearLayout lLayoutInformationFacilityTimezone;

    private MagDeviceNameDialogFragment magDeviceNameDialogFragment;
    private MagDeviceTimeZoneFragment magDeviceTimeZoneFragment;

    public static CloudLiveDeviceInfoFragment newInstance(Bundle bundle) {
        CloudLiveDeviceInfoFragment fragment = new CloudLiveDeviceInfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        magDeviceNameDialogFragment = MagDeviceNameDialogFragment.newInstance(new Bundle());
        magDeviceTimeZoneFragment = MagDeviceTimeZoneFragment.newInstance(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_live_device_info, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        String editName = PreferencesUtils.getString("magEditName", "大门口的门铃");
        tvInformationFacilityName.setText(editName);
        String detailText = PreferencesUtils.getString("magDetailText", "北京/中国");
        tvInformationFacilityTimeZone.setText(detailText);
    }

    @Override
    public void setPresenter(CloudLiveDeviceInfoContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_information_back, R.id.lLayout_information_facility_name, R.id.lLayout_information_facility_timezone})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_information_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.lLayout_information_facility_name:
                magDeviceNameDialogFragment.show(getActivity().getFragmentManager(),
                        "MsgDeviceNameDialogFragment");
                if (getActivity() != null && getActivity().getFragmentManager() != null) {
                    magDeviceNameDialogFragment.setListener(new MagDeviceNameDialogFragment.OnMagDataChangeListener() {
                        @Override
                        public void magDataChangeListener(String content) {
                            tvInformationFacilityName.setText(content);
                        }
                    });
                }
                break;
            case R.id.lLayout_information_facility_timezone:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.lLayout_information_facility_timezone));
                AppLogger.e("lLayout_information_facility_timezone");
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, magDeviceTimeZoneFragment, "MagDeviceTimeZoneFragment")
                        .addToBackStack("MagLiveInformationFragment")
                        .commit();
                /**
                 * 接口回调，得到相应的text，并且赋值给当前fragment
                 */
                magDeviceTimeZoneFragment.setListener(new MagDeviceTimeZoneFragment.OnMagTimezoneChangeListener() {
                    @Override
                    public void magTimezoneChangeListener(String content) {
                        tvInformationFacilityTimeZone.setText(content);
                    }
                });
                break;
        }
    }

    @Override
    public void initSdCardState(int state) {


    }
}
