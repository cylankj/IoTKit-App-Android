package com.cylan.jiafeigou.n.view.bind;


import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.impl.bind.ConfigApPresenterImpl;
import com.cylan.jiafeigou.n.view.BaseTitleFragment;
import com.cylan.jiafeigou.n.view.adapter.ToBindDeviceListAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BindDeviceListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BindDeviceListFragment extends BaseTitleFragment implements ToBindDeviceListAdapter.ItemClickListener {

    @BindView(R.id.rv_to_bind_device_list)
    RecyclerView rvToBindDeviceList;
    ToBindDeviceListAdapter toBindDeviceListAdapter;
    @BindView(R.id.fLayout_top_bar)
    FrameLayout fLayoutTopBar;


    public BindDeviceListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate activity_cloud_live_mesg_call_out_item fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
    protected int getSubContentViewId() {
        return R.layout.fragment_bind_device_list;
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
                    .add(android.R.id.content, fragment, "ConfigApFragment")
                    .addToBackStack("ConfigApFragment")
                    .commit();
            new ConfigApPresenterImpl(fragment);
        } else {
            Toast.makeText(getContext(), "null", Toast.LENGTH_SHORT).show();
        }
    }

}
