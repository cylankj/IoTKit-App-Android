package com.cylan.jiafeigou.n.view.panorama;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yanzhendong on 2017/3/11.
 */

public class PanoramaSettingFragment extends BaseFragment<PanoramaSettingContact.Presenter> {
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout toolbarContainer;

    public static PanoramaSettingFragment newInstance(String uuid) {
        PanoramaSettingFragment fragment = new PanoramaSettingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected PanoramaSettingContact.Presenter onCreatePresenter() {
        return new PanoramaSettingPresenter();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewUtils.setViewPaddingStatusBar(toolbarContainer);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_panorama_setting;
    }

    @Override
    @OnClick(R.id.fragment_panorama_setting_header_back)
    public boolean onBackPressed() {
        getActivity().onBackPressed();
        return super.onBackPressed();
    }
}
