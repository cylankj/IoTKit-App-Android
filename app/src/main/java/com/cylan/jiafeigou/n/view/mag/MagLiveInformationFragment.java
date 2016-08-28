package com.cylan.jiafeigou.n.view.mag;

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
public class MagLiveInformationFragment extends Fragment {


    public static final String KEY_TITLE = "key_title";
    //实例化fragment对象
    private ImageView mMsgInformationBack;
    private LinearLayout mMsgInformationName;
    private LinearLayout mMsgInformationTimeZone;
    private TextView mMsgTvName;
    private TextView mMsgTvTimezone;
    private com.cylan.jiafeigou.n.view.mag.MagDeviceNameDialogFragment magDeviceNameDialogFragment;
    private MagDeviceTimeZoneFragment magDeviceTimeZoneFragment;

    private OnMagLiveDataChangeListener mListener;

    public void setListener(OnMagLiveDataChangeListener mListener) {
        this.mListener = mListener;
    }

    public interface OnMagLiveDataChangeListener {
        void magLiveDataChange(String content);
    }

    public static MagLiveInformationFragment newInstance(Bundle bundle) {
        MagLiveInformationFragment fragment = new MagLiveInformationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        magDeviceNameDialogFragment = com.cylan.jiafeigou.n.view.mag.MagDeviceNameDialogFragment.newInstance(new Bundle());
        magDeviceTimeZoneFragment = MagDeviceTimeZoneFragment.newInstance(new Bundle());
        //在执行该回调方法前，可以进行数据的预先加载
        //在oncreatView之前，把fragment页面的布局内的需要修改的信息，修改一下。
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mag_facility_information, null);
        mMsgInformationBack = (ImageView) view.findViewById(R.id.iv_mag_information_back);
        mMsgInformationName = (LinearLayout) view.findViewById(R.id.lLayout_mag_information_facility_name);
        mMsgInformationTimeZone = (LinearLayout) view.findViewById(R.id.lLayout_mag_information_facility_timezone);
        mMsgTvName = (TextView) view.findViewById(R.id.tv_mag_information_facility_name);
        mMsgTvTimezone = (TextView) view.findViewById(R.id.tv_mag_information_facility_time_zone);

        mMsgTvName.setText(getArguments().getString(KEY_TITLE));
        mMsgInformationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();

            }
        });

        mMsgInformationName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                magDeviceNameDialogFragment.show(getActivity().getFragmentManager(),
                        "MsgDeviceNameDialogFragment");
                if (getActivity() != null && getActivity().getFragmentManager() != null) {
                    magDeviceNameDialogFragment.setListener(new MagDeviceNameDialogFragment.OnMagDataChangeListener() {
                        @Override
                        public void magDataChangeListener(String content) {
                            mMsgTvName.setText(content);
                        }
                    });
                }
            }
        });

        mMsgInformationTimeZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(R.id.lLayout_msg_information, magDeviceTimeZoneFragment, "MagDeviceTimeZoneFragment")
                        .addToBackStack("MagLiveInformationFragment")
                        .commit();
                /**
                 * 接口回调，得到相应的text，并且赋值给当前fragment
                 */
                magDeviceTimeZoneFragment.setListener(new MagDeviceTimeZoneFragment.OnMagTimezoneChangeListener() {
                    @Override
                    public void magTimezoneChangeListener(String content) {
                        mMsgTvTimezone.setText(content);
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mListener != null) {
            mListener.magLiveDataChange(mMsgTvName.getText().toString());
        }
    }

    public void onStart() {
        super.onStart();
        String editName = PreferencesUtils.getString(getActivity(), "magEditName", "客厅摄像头");
        mMsgTvName.setText(editName);
        String detailText = PreferencesUtils.getString(getActivity(), "magDetailText", "北京/中国");
        mMsgTvTimezone.setText(detailText);
    }
}
