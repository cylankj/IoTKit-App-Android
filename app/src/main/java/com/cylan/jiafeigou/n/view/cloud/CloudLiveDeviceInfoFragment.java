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
import com.cylan.jiafeigou.utils.PreferencesUtils;

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
    @BindView(R.id.tv_device_alias)
    TextView tvInformationFacilityName;
    @BindView(R.id.lLayout_information_facility_name)
    LinearLayout lLayoutInformationFacilityName;
    @BindView(R.id.tv_device_time_zone)
    TextView tvInformationFacilityTimeZone;
    @BindView(R.id.lLayout_information_facility_timezone)
    LinearLayout lLayoutInformationFacilityTimezone;

    public OnChangeNameListener listener;

    public interface OnChangeNameListener {
        void changeName(String name);
    }

    public void setOnChangeNameListener(OnChangeNameListener listener) {
        this.listener = listener;
    }

//    private MagDeviceNameDialogFragment magDeviceNameDialogFragment;

    public static CloudLiveDeviceInfoFragment newInstance(Bundle bundle) {
        CloudLiveDeviceInfoFragment fragment = new CloudLiveDeviceInfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        magDeviceNameDialogFragment = MagDeviceNameDialogFragment.newInstance(new Bundle());
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
//                magDeviceNameDialogFragment.show(getActivity().getFragmentManager(),
//                        "MsgDeviceNameDialogFragment");
//                if (getActivity() != null && getActivity().getFragmentManager() != null) {
//                    magDeviceNameDialogFragment.setListener(new MagDeviceNameDialogFragment.OnMagDataChangeListener() {
//                        @Override
//                        public void magDataChangeListener(String content) {
//                            tvInformationFacilityName.setText(content);
//                        }
//                    });
//                }
                break;
            case R.id.lLayout_information_facility_timezone:
                break;
        }
    }

    @Override
    public void initSdCardState(int state) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.changeName(tvInformationFacilityName.getText().toString().trim());
        }

    }
}
