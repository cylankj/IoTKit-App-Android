package com.cylan.jiafeigou.n.view.bind;


import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.view.BaseTitleFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.utils.NetUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BindGuideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BindGuideFragment extends BaseTitleFragment {
    @BindView(R.id.imv_bind_guide)
    ImageView imvBindGuide;
    @BindView(R.id.iv_top_bar_left)
    ImageView ivTopBarLeft;
    @BindView(R.id.tv_top_bar_center)
    TextView tvTopBarCenter;
    @BindView(R.id.tv_top_bar_right)
    TextView tvTopBarRight;
    @BindView(R.id.fLayout_top_bar)
    FrameLayout fLayoutTopBar;
    @BindView(R.id.tv_bind_guide_next)
    TextView tvBindGuideNext;
    // TODO: Rename parameter arguments, choose names that match

    public BindGuideFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BindGuideFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BindGuideFragment newInstance() {
        BindGuideFragment fragment = new BindGuideFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate activity_cloud_live_mesg_video_talk_item fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GlideDrawableImageViewTarget imageViewTarget =
                new GlideDrawableImageViewTarget(imvBindGuide);
        Glide.with(this).load(R.raw.bind_guide).into(imageViewTarget);
    }

    @Override
    public void onResume() {
        super.onResume();
        tryLoadConfigApFragment();
    }

    @Override
    protected int getSubContentViewId() {
        return R.layout.fragment_bind_guide;
    }

    @OnClick(R.id.tv_bind_guide_next)
    public void onClick() {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    private void tryLoadConfigApFragment() {
        final WifiInfo info = NetUtils.getWifiManager(getContext()).getConnectionInfo();
        if (info == null || !JFGRules.isCylanDevice(info.getSSID())) {
            AppLogger.i("bind: " + info);
            return;
        }
        ConfigApFragment fragment = ConfigApFragment.newInstance(null);
        boolean result = ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                fragment, -1);
        if (result) {
            //add a new one

        }
    }
}
