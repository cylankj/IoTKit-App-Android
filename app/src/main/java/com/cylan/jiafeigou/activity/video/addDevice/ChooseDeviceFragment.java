package com.cylan.jiafeigou.activity.video.addDevice;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.adapter.WifiListAdapter;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.MyScanResult;

import java.util.ArrayList;
import java.util.List;

public class ChooseDeviceFragment extends Fragment implements AdapterView.OnItemClickListener {

    private OnSelectDeviceListener mListener;

    private List<MyScanResult> mList;

    private WifiListAdapter mAdapter;


    public static ChooseDeviceFragment newInstance(ArrayList<MyScanResult> param1) {
        ChooseDeviceFragment fragment = new ChooseDeviceFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ClientConstants.PARAM_SCAN_LIST, param1);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mList = getArguments().getParcelableArrayList(ClientConstants.PARAM_SCAN_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_search_result, container, false);
        ListView mResultListView = (ListView) view.findViewById(R.id.search_result_listView);
        mResultListView.setOnItemClickListener(this);
        mAdapter = new WifiListAdapter(getActivity());
        mAdapter.addAll(mList);
        mResultListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSelectDeviceListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null)
            mListener.onSelectDevice(mAdapter.getItem(position));
    }

    public void setOnSelectDeviceListener(OnSelectDeviceListener listener) {
        mListener = listener;
    }


    public interface OnSelectDeviceListener {
        void onSelectDevice(MyScanResult scan);
    }

}
