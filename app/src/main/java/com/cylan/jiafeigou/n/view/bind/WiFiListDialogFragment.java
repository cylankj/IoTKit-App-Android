package com.cylan.jiafeigou.n.view.bind;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.adapter.ToBindDeviceListAdapter;
import com.cylan.utils.DensityUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cylan-hunt on 16-7-7.
 */
public class WiFiListDialogFragment extends DialogFragment {

    @BindView(R.id.rv_wifi_list)
    RecyclerView rvWifiList;

    public static WiFiListDialogFragment newInstance(Bundle bundle) {
        WiFiListDialogFragment fragment = new WiFiListDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.l_style_dialog);
        setCancelable(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        final float width = DensityUtil.getScreenWidth(getContext()) * 0.78f;
        getDialog().getWindow()
                .setLayout((int) width,
                        DensityUtil.dip2px(getActivity(), 489));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_wifi_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    ToBindDeviceListAdapter adapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new ToBindDeviceListAdapter(getContext());
        rvWifiList.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvWifiList.setLayoutManager(layoutManager);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
