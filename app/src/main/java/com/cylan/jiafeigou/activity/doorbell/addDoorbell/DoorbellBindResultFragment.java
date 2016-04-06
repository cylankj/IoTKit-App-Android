package com.cylan.jiafeigou.activity.doorbell.addDoorbell;


import android.app.Fragment;
import android.os.Bundle;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.video.addDevice.BindResultFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class DoorbellBindResultFragment extends BindResultFragment {

    public static DoorbellBindResultFragment newInstance() {
        return new DoorbellBindResultFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDeviceView.setImageResource(R.drawable.ico_addvideo_doorbell);
    }
}
