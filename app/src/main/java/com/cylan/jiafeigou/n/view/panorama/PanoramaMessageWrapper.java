package com.cylan.jiafeigou.n.view.panorama;

import android.os.Bundle;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.cam.CamMessageListFragment;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;

/**
 * Created by yanzhendong on 2017/8/2.
 */

public class PanoramaMessageWrapper extends BaseFragment {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    private CamMessageListFragment fragment;

    public static PanoramaMessageWrapper newInstance(String uuid) {
        PanoramaMessageWrapper wrapper = new PanoramaMessageWrapper();
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        wrapper.setArguments(bundle);
        return wrapper;
    }

    @Override
    protected void setFragmentComponent(FragmentComponent fragmentComponent) {

    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_panorama_message_wrapper;
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        customToolbar.setBackAction(view -> getFragmentManager().popBackStack());
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        bundle.putBoolean(JConstant.KEY_JUMP_TO_MESSAGE, true);
        bundle.putBoolean("show_edit", false);
        fragment = CamMessageListFragment.newInstance(bundle);
        fragment.hookEdit(customToolbar.getTvToolbarRight());
        getChildFragmentManager().beginTransaction().replace(R.id.message_container, fragment).commit();

    }

    @Override
    public void onStart() {
        super.onStart();

    }
}
