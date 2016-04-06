package com.cylan.jiafeigou.activity.doorbell.addDoorbell;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.video.addDevice.SubmitWifiInfoFragment;
import com.cylan.jiafeigou.engine.ClientConstants;

/**
 * A simple {@link Fragment} subclass.
 */
public class DoorbellSubmitWifiInfoFragment extends SubmitWifiInfoFragment {


    public static DoorbellSubmitWifiInfoFragment newInstance(String cid) {
        DoorbellSubmitWifiInfoFragment fragment = new DoorbellSubmitWifiInfoFragment();
        Bundle args = new Bundle();
        args.putString(ClientConstants.PARAM_BIND_CID, cid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHintNick();
    }

    @Override
    protected void setHintNick() {
        mDeviceName.setHint(getString(R.string.CALL_CAMERA_NAME));
    }
}
