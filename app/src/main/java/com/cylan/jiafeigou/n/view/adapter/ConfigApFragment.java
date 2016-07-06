package com.cylan.jiafeigou.n.view.adapter;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigApFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigApFragment extends Fragment {

    public ConfigApFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 2.
     * @return A new instance of fragment ConfigApFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConfigApFragment newInstance(Bundle bundle) {
        ConfigApFragment fragment = new ConfigApFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_config_ap, container, false);
    }

}
