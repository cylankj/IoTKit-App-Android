package com.cylan.jiafeigou.n.view.cam;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeviceStandbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceStandbyFragment extends Fragment {

    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.cb_device_standby)
    CheckBox cbDeviceStandby;
    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;

    public DeviceStandbyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_video_talk_item new instance of
     * this fragment using the provided parameters.
     *
     * @param args Parameter 2.
     * @return A new instance of fragment DeviceStandbyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DeviceStandbyFragment newInstance(Bundle args) {
        DeviceStandbyFragment fragment = new DeviceStandbyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device_standby, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }

    @OnClick(R.id.imgV_top_bar_center)
    public void onClick() {
        getActivity().getSupportFragmentManager().popBackStack();
    }
}
