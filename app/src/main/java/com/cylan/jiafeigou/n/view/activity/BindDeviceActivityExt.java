package com.cylan.jiafeigou.n.view.activity;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;

public class BindDeviceActivityExt extends BaseFullScreenFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_device_ext);
        getFragmentManager().beginTransaction()
                .replace(R.id.v_layout_container, new BindListFragment())
                .commit();
    }

    public static class BindListFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pre_bind_device_ext);
        }
    }
}
