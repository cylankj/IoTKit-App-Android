package com.cylan.jiafeigou.n.view.bind;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.SubmitBindingInfoContractImpl;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoginButton;
import com.cylan.jiafeigou.widget.SimpleProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public class SubmitBindingInfoFragment extends IBaseFragment<SubmitBindingInfoContract.Presenter>
        implements SubmitBindingInfoContract.View {

    @BindView(R.id.progress_loading)
    SimpleProgressBar progressLoading;
    @BindView(R.id.tv_loading_percent)
    TextView tvLoadingPercent;
    @BindView(R.id.btn_bind_repeat)
    LoginButton btnBindRepeat;
    @BindView(R.id.vs_layout_switch)
    ViewSwitcher vsLayoutSwitch;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    public static SubmitBindingInfoFragment newInstance(Bundle bundle) {
        SubmitBindingInfoFragment fragment = new SubmitBindingInfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.basePresenter = new SubmitBindingInfoContractImpl(this, getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_submit_binding_info, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adjustViewSize();
        if (basePresenter != null)
            basePresenter.startCounting();
    }

    private void adjustViewSize() {
        ViewGroup.LayoutParams l = progressLoading.getLayoutParams();
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        l.height = l.width = (int) (screenWidth * 0.6f);
        progressLoading.setLayoutParams(l);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void bindState(int state) {
        if (state == 0) {
            //绑定失败
            vsLayoutSwitch.showNext();
            return;
        }
        if (state > 0) {
            progressLoading.setVisibility(View.INVISIBLE);
            if (basePresenter != null)
                basePresenter.stop();
            SetDeviceAliasFragment fragment = SetDeviceAliasFragment.newInstance(getArguments());
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                    fragment, android.R.id.content);
            if (basePresenter != null)
                basePresenter.stop();
        }
    }

    @Override
    public void onCounting(int percent) {
        Log.d("SubmitBindingInfo", "SubmitBindingInfo: " + percent);
        tvLoadingPercent.setText(percent + "");
    }

    @Override
    public void setPresenter(SubmitBindingInfoContract.Presenter presenter) {
        this.basePresenter = presenter;
    }

    @OnClick(R.id.btn_bind_repeat)
    public void onClick() {
    }
}
