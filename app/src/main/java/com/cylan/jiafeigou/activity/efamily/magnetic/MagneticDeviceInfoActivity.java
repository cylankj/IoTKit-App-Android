package com.cylan.jiafeigou.activity.efamily.magnetic;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.jiafeigou.activity.video.setting.DeviceState;

/**
 * Created by yangc on 2015/12/14.
 */
public class MagneticDeviceInfoActivity extends DeviceState {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mData.os == Constants.OS_MAGNET) {

            findViewById(R.id.line1).setVisibility(View.GONE);
            findViewById(R.id.line2).setVisibility(View.GONE);
            findViewById(R.id.layout_device_mobilenet).setVisibility(View.GONE);
            findViewById(R.id.layout_canuse_memory).setVisibility(View.GONE);

            findViewById(R.id.line4).setVisibility(View.GONE);
            findViewById(R.id.layout_sys_vision).setVisibility(View.GONE);

            findViewById(R.id.layout_continuous_operation).setVisibility(View.GONE);

            findViewById(R.id.device_network_text).setVisibility(View.GONE);
            findViewById(R.id.device_network_text_layout).setVisibility(View.GONE);
            findViewById(R.id.device_software_vision_layout).setVisibility(View.GONE);
            findViewById(R.id.layout_continuous_operation).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.device_mac_address_title)).setText(
                    getString(R.string.BLE_SENSOR));
            findViewById(R.id.device_mac_address_layout).setVisibility(View.GONE);
            findViewById(R.id.line5).setVisibility(View.GONE);
        }
    }
}
