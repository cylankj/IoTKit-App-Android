package com.cylan.jiafeigou.n.view.record;

import android.os.Bundle;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;

/**
 * Created by yzd on 17-1-5.
 */


public class DelayRecordDeviceFragment extends BaseFragment {


    //// TODO: 17-1-5 获取类型为3G狗的在线设备，目前不知道怎么获取
    public static DelayRecordDeviceFragment newInstance(Bundle bundle) {
        DelayRecordDeviceFragment fragment = new DelayRecordDeviceFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected JFGPresenter onCreatePresenter() {
        return null;
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_delay_record_select_device;
    }
}
