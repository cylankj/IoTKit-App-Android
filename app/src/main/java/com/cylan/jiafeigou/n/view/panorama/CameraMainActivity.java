package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;

public class CameraMainActivity extends BaseActivity {

    @Override
    protected boolean onSetContentView() {
        super.onSetContentView();
        setContentView(R.layout.activity_camera_main);
        return true;
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
//        PanoramaCameraActivity fragment = PanoramaCameraActivity.newInstance(uuid, getIntent().hasExtra(JConstant.KEY_JUMP_TO_MESSAGE));
//        getSupportFragmentManager().beginTransaction().replace(R.id.camera_main_container, fragment).commit();

        //不需要进入消息页面
//        if (getIntent().hasExtra(JConstant.KEY_JUMP_TO_MESSAGE)) {
//            removeHint();
//            PanoramaMessageWrapperActivity messageWrapper = PanoramaMessageWrapperActivity.newInstance(uuid);
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .setCustomAnimations(0,
//                            R.anim.slide_out_left,
//                            R.anim.slide_in_left,
//                            R.anim.slide_out_right)
//                    .replace(R.id.camera_main_container, messageWrapper)
//                    .addToBackStack(PanoramaMessageWrapperActivity.class.getSimpleName())
//                    .commit();
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


}
