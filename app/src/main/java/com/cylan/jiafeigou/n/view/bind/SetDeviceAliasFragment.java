package com.cylan.jiafeigou.n.view.bind;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetDeviceAliasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetDeviceAliasFragment extends Fragment {


    public SetDeviceAliasFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SetDeviceAliasFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetDeviceAliasFragment newInstance(Bundle bundleo) {
        SetDeviceAliasFragment fragment = new SetDeviceAliasFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_device_alias, container, false);
    }

}
