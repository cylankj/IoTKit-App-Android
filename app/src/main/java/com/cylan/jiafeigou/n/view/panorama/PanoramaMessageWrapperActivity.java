package com.cylan.jiafeigou.n.view.panorama;

import android.os.Bundle;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.cam.CamMessageListFragment;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;

/**
 * Created by yanzhendong on 2017/8/2.
 */

public class PanoramaMessageWrapperActivity extends BaseActivity {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    private CamMessageListFragment fragment;

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_panorama_message_wrapper;
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        removeHint();
        customToolbar.setBackAction(view -> finish() );
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        bundle.putBoolean(JConstant.KEY_JUMP_TO_MESSAGE, true);
        bundle.putBoolean("show_edit", false);
        fragment = CamMessageListFragment.newInstance(bundle);
        fragment.hookEdit(customToolbar.getTvToolbarRight());
        getSupportFragmentManager().beginTransaction().replace(R.id.message_container, fragment).commit();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void removeHint() {
        try {
            BaseApplication.getAppComponent().getSourceManager().clearValue(uuid, 1001, 1002, 1003, 1004, 1005);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
