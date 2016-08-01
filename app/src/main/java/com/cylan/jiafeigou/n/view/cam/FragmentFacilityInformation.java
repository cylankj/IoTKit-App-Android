package com.cylan.jiafeigou.n.view.cam;

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
import com.cylan.jiafeigou.utils.PreferencesUtils;

/**
 * 创建者     谢坤
 * 创建时间   2016/7/12 17:53
 * 用来控制摄像头模块下的设备信息，点击设备名称和设备时区时进行切换
 */
public class FragmentFacilityInformation extends Fragment {


    public static final String KEY_TITLE = "key_title";
    //实例化fragment对象
    private ImageView mInformationBack;
    private LinearLayout mInformationName;
    private LinearLayout mInformationTimeZone;
    private DeviceNameDialogFragment nameDialogFragment;
    private TextView mTvName;
    private DeviceTimeZoneFragment deviceTimeZoneFragment;
    private TextView mTvTimezone;

    public static FragmentFacilityInformation newInstance(Bundle bundle) {
        FragmentFacilityInformation fragment = new FragmentFacilityInformation();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nameDialogFragment = DeviceNameDialogFragment.newInstance(new Bundle());
        deviceTimeZoneFragment = DeviceTimeZoneFragment.newInstance(new Bundle());
        //在执行该回调方法前，可以进行数据的预先加载
        //在oncreatView之前，把fragment页面的布局内的需要修改的信息，修改一下。
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facility_information, null);
        mInformationBack = (ImageView) view.findViewById(R.id.iv_information_back);
        mInformationName = (LinearLayout) view.findViewById(R.id.lLayout_information_facility_name);
        mInformationTimeZone = (LinearLayout) view.findViewById(R.id.lLayout_information_facility_timezone);
        mTvName = (TextView) view.findViewById(R.id.tv_information_facility_name);
        mTvTimezone = (TextView) view.findViewById(R.id.tv_information_facility_time_zone);

        mTvName.setText(getArguments().getString(KEY_TITLE));
        mInformationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();

            }
        });

        mInformationName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameDialogFragment.show(getActivity().getFragmentManager(),
                        "DeviceNameDialogFragment");
                nameDialogFragment.setListener(new DeviceNameDialogFragment.OnDataChangeListener() {
                    @Override
                    public void dataChangeListener(String content) {
                        mTvName.setText(content);
                    }
                });
            }
        });

        mInformationTimeZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, deviceTimeZoneFragment, "DeviceTimeZoneFragment")
                        .addToBackStack("FragmentFacilityInformation")
                        .commit();
                /**
                 * 接口回调，得到相应的text，并且赋值给当前fragment
                 */
                deviceTimeZoneFragment.setListener(new DeviceTimeZoneFragment.OnTimezoneChangeListener() {
                    @Override
                    public void timezoneChangeListener(String content) {
                        mTvTimezone.setText(content);
                    }
                });
            }
        });
        return view;
    }

    public void onStart() {
        super.onStart();
        String editName = PreferencesUtils.getString(getActivity(), "editName", "客厅摄像头");
        mTvName.setText(editName);
        String detailText = PreferencesUtils.getString(getActivity(), "detailText", "北京/中国");
        mTvTimezone.setText(detailText);
    }
}
