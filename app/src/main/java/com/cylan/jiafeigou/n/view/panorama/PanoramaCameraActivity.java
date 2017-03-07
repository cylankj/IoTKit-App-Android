package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;

/**
 * Created by yanzhendong on 2017/3/7.
 */

public class PanoramaCameraActivity extends BaseActivity<PanoramaCameraContact.Presenter> {
    @Override
    protected PanoramaCameraContact.Presenter onCreatePresenter() {
        return null;
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_panorama_camera;
    }
}
