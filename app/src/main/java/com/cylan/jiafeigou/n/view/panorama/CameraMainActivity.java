package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;

public class CameraMainActivity extends BaseActivity {

    @Override
    protected int getContentViewID() {
        return R.layout.activity_camera_main;
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        PanoramaCameraFragment fragment = PanoramaCameraFragment.newInstance(uuid);
        getSupportFragmentManager().beginTransaction().replace(R.id.camera_main_container, fragment).commit();
    }

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {

    }
}
