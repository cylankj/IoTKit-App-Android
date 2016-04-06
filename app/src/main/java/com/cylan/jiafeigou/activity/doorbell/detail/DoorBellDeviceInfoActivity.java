package com.cylan.jiafeigou.activity.doorbell.detail;

import android.os.Bundle;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.jiafeigou.activity.video.setting.DeviceState;

public class DoorBellDeviceInfoActivity extends DeviceState {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mData.os == Constants.OS_DOOR_BELL) {

            findViewById(R.id.line1).setVisibility(View.GONE);
            findViewById(R.id.line2).setVisibility(View.GONE);
            findViewById(R.id.layout_device_mobilenet).setVisibility(View.GONE);
            findViewById(R.id.layout_canuse_memory).setVisibility(View.GONE);
            findViewById(R.id.line3).setVisibility(View.GONE);
            findViewById(R.id.layout_continuous_operation).setVisibility(View.GONE);
        }

    }
}
