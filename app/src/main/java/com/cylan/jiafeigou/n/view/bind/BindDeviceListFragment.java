package com.cylan.jiafeigou.n.view.bind;


import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.adapter.ToBindDeviceListAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BindDeviceListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BindDeviceListFragment extends Fragment implements ToBindDeviceListAdapter.ItemClickListener {

    @BindView(R.id.rv_to_bind_device_list)
    RecyclerView rvToBindDeviceList;
    ToBindDeviceListAdapter toBindDeviceListAdapter;


    public BindDeviceListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 2.
     * @return A new instance of fragment BindDeviceListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BindDeviceListFragment newInstance(Bundle bundle) {
        BindDeviceListFragment fragment = new BindDeviceListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        toBindDeviceListAdapter = new ToBindDeviceListAdapter(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bind_device_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvToBindDeviceList.setLayoutManager(layoutManager);
        ArrayList<ScanResult> results = bundle.getParcelableArrayList(BindCameraFragment.KEY_DEVICE_LIST);
        toBindDeviceListAdapter.addAll(results);
        toBindDeviceListAdapter.setOnItemClickListener(this);
        rvToBindDeviceList.setAdapter(toBindDeviceListAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void updateList(ArrayList<ScanResult> resultArrayList) {
        toBindDeviceListAdapter.clear();
        toBindDeviceListAdapter.addAll(resultArrayList);
    }

    @Override
    public void onClick(View v) {
        Object o = v.getTag();
        if (o != null && o instanceof ScanResult) {
            Bundle bundle = getArguments();
            if (bundle == null)
                bundle = new Bundle();
            ConfigApFragment fragment = ConfigApFragment.newInstance(bundle);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                            , R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.fLayout_bind_device_fragment_container_id, fragment, "ConfigApFragment")
//                    .addToBackStack("ConfigApFragment")
                    .commit();
        } else {
            Toast.makeText(getContext(), "null", Toast.LENGTH_SHORT).show();
        }
    }

}
