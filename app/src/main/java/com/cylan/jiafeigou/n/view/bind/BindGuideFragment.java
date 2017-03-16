package com.cylan.jiafeigou.n.view.bind;


import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.impl.bind.ConfigApPresenterImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_BIND_DEVICE;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BindGuideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BindGuideFragment extends IBaseFragment {
    @BindView(R.id.imv_bind_guide)
    ImageView imvBindGuide;
    @BindView(R.id.tv_bind_guide_next)
    TextView tvBindGuideNext;
    @BindView(R.id.tv_guide_sub_content)
    TextView tvGuideSubContent;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.tv_guide_main_content)
    TextView tvGuideMainContent;
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
    public static BindGuideFragment newInstance(Bundle bundle) {
        BindGuideFragment fragment = new BindGuideFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JConstant.ConfigApStep = 0;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null && TextUtils.equals(getArguments().getString(KEY_BIND_DEVICE),
                getString(R.string.DOG_CAMERA_NAME))) {
            //is cam
            tvGuideMainContent.setText(getString(R.string.WIFI_SET_3));
        } else {
            //default bell
            tvGuideMainContent.setText(getString(R.string.WIFI_SET_3_1));
        }
        tvGuideSubContent.setText(getString(R.string.WIFI_SET_4, getString(R.string.app_name)));
        GlideDrawableImageViewTarget imageViewTarget =
                new GlideDrawableImageViewTarget(imvBindGuide);
        Glide.with(this).load(R.raw.bind_guide).into(imageViewTarget);
        ViewUtils.setViewMarginStatusBar(customToolbar);
        customToolbar.setBackAction((View v) -> {
            getActivity().onBackPressed();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        tryLoadConfigApFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bind_guide, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.tv_bind_guide_next)
    public void onClick() {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        JConstant.ConfigApStep = 1;
    }

    private void tryLoadConfigApFragment() {
        final WifiInfo info = NetUtils.getWifiManager(getContext()).getConnectionInfo();
        if (info == null || !JFGRules.isCylanDevice(info.getSSID())) {
            AppLogger.i("bind: " + info);
            return;
        }
        ConfigApFragment fragment = ConfigApFragment.newInstance(getArguments());

        boolean result =
                JConstant.ConfigApStep == 1 ?
                        ActivityUtils.addFragmentSlideInFromLeft(getActivity().getSupportFragmentManager(),
                                fragment, android.R.id.content) :
                        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                                fragment, android.R.id.content);

        if (result) {
            //add a new one
            new ConfigApPresenterImpl(fragment);
        }
        imvBindGuide.setVisibility(View.GONE);
    }
}
