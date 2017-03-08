package com.cylan.jiafeigou.n.view.setting;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.setting.SdcardInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.setting.SdcardInfoPresenterImpl;
import com.cylan.jiafeigou.widget.LoginButton;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.SmoothPercent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class SdcardInfoFragment extends IBaseFragment<SdcardInfoContract.Presenter>
        implements SdcardInfoContract.View,
        SmoothPercent.PercentUpdate {

    @BindView(R.id.sv_setting_device_detail)
    SettingItemView0 svSettingDeviceDetail;
    @BindView(R.id.sm_percent)
    SmoothPercent smPercent;
    @BindView(R.id.lb2_sdcard_format)
    LoginButton lb2SdcardFormat;

    public SdcardInfoFragment() {
        // Required empty public constructor
    }

    public static SdcardInfoFragment getInstance(Bundle bundle) {
        SdcardInfoFragment fragment = new SdcardInfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        String uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        this.basePresenter = new SdcardInfoPresenterImpl(this, uuid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sdcard_info, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(SdcardInfoContract.Presenter presenter) {

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        smPercent.setPercentUpdate(this);
        smPercent.smoothSetPercent(true, 0.6f);
    }

    @OnClick(R.id.lb2_sdcard_format)
    public void onClick() {
        if (basePresenter != null) basePresenter.startFormat();
        lb2SdcardFormat.viewZoomBig();
    }

    @Override
    public void percentUpdate(float percent) {

    }
}
