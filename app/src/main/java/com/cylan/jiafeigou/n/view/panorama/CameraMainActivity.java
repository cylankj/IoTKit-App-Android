package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;

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
        if (getIntent().hasExtra(JConstant.KEY_JUMP_TO_MESSAGE)) {
            removeHint();
            PanoramaMessageWrapper messageWrapper = PanoramaMessageWrapper.newInstance(uuid);
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(0,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right)
                    .replace(R.id.camera_main_container, messageWrapper)
                    .addToBackStack(PanoramaMessageWrapper.class.getSimpleName())
                    .commit();
        }
    }

    private void removeHint() {
        try {
            BaseApplication.getAppComponent().getSourceManager().clearValue(uuid, 1001, 1002, 1003, 1004, 1005);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {

    }
}
