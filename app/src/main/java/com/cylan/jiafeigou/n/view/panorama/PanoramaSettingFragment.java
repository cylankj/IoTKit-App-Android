package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;

/**
 * Created by yanzhendong on 2017/3/11.
 */

public class PanoramaSettingFragment extends BaseFragment<PanoramaSettingContact.Presenter> {
    @Override
    protected PanoramaSettingContact.Presenter onCreatePresenter() {
        return new PanoramaSettingPresenter();
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_panorama_setting;
    }
}
