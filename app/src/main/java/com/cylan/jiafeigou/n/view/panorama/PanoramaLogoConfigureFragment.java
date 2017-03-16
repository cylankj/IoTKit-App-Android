package com.cylan.jiafeigou.n.view.panorama;

import android.os.Bundle;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;

/**
 * Created by yanzhendong on 2017/3/15.
 */

public class PanoramaLogoConfigureFragment extends BaseFragment<PanoramaLogoConfigureContact.Presenter> {

    public static PanoramaLogoConfigureFragment newInstance() {
        PanoramaLogoConfigureFragment fragment = new PanoramaLogoConfigureFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected PanoramaLogoConfigureContact.Presenter onCreatePresenter() {
        return new PanoramaLogoConfigurePresenter();
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_panorama_logo_configure;
    }
}
