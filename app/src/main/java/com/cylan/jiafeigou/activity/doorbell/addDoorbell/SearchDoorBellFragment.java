package com.cylan.jiafeigou.activity.doorbell.addDoorbell;


import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.video.addDevice.SearchDeviceFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchDoorBellFragment extends SearchDeviceFragment {

    public static SearchDoorBellFragment newInstance() {
        return new SearchDoorBellFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSearchPromptView.setText(R.string.DOOR_BLUE_BLINKING);
        mNextBtn.setText(R.string.DOOR_BLINKING);
        imageView.setImageResource(R.drawable.doorbell_sureing);
        mAni = (AnimationDrawable) (imageView.getDrawable());
        mAni.start();
    }
}
