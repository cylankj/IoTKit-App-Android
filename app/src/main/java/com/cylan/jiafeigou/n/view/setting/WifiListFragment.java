package com.cylan.jiafeigou.n.view.setting;


import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.setting.WifiListContract;
import com.cylan.jiafeigou.n.mvp.impl.setting.WifiListPresenterImpl;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.dialog.EditFragmentDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;
import static com.cylan.jiafeigou.widget.dialog.BaseDialog.KEY_TITLE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_LEFT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_RIGHT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_SHOW_EDIT;

/**
 * A simple {@link Fragment} subclass.
 */
public class WifiListFragment extends IBaseFragment<WifiListContract.Presenter>
        implements WifiListContract.View, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.rv_wifi_list)
    RecyclerView rvWifiList;
    @BindView(R.id.sw_refresh_wifi)
    SwipeRefreshLayout swRefreshWifi;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    private String uuid;

    public WifiListFragment() {
        // Required empty public constructor
    }

    public static WifiListFragment getInstance(Bundle bundle) {
        WifiListFragment fragment = new WifiListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
        this.basePresenter = new WifiListPresenterImpl(this, uuid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wifi_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(WifiListContract.Presenter presenter) {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvWifiList.setAdapter(new AAdapter(getContext(), null, R.layout.layout_wifi_list_item));
        rvWifiList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        swRefreshWifi.setOnRefreshListener(this);
        customToolbar.setBackAction(v -> {//回退事件
            getActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewUtils.deBounceClick(v);
            int position = ViewUtils.getParentAdapterPosition(rvWifiList, v, R.id.lLayout_wifi_list_item);
            final ScanResult item = ((AAdapter) rvWifiList.getAdapter()).getItem(position);
            Bundle bundle = new Bundle();
            final String ssid = item.SSID.replace("\"", "");
            bundle.putString(KEY_TITLE, String.format(Locale.getDefault(), "给%s输入密码,缺少语言包", ssid));
            bundle.putString(KEY_LEFT_CONTENT, getString(R.string.CARRY_ON));
            bundle.putString(KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
            final int security = NetUtils.getSecurity(item);
            bundle.putBoolean(KEY_SHOW_EDIT, security != 0);
            EditFragmentDialog dialog = EditFragmentDialog.newInstance(bundle);
            dialog.setAction(new EditFragmentDialog.DialogAction<Object>() {
                @Override
                public void onDialogAction(int id, Object value) {
                    if (value != null && value instanceof String) {
                        //pwd
                        if (basePresenter != null)
                            basePresenter.sendWifiInfo(ssid, (String) value, security);
                    }
                }
            });
            dialog.show(getChildFragmentManager(), "dialog");
        }
    };

    @Override
    public void onResults(ArrayList<ScanResult> results) {
        swRefreshWifi.setRefreshing(false);
        ((AAdapter) rvWifiList.getAdapter()).clear();
        ((AAdapter) rvWifiList.getAdapter()).addAll(results);
    }

    @Override
    public void onErr(int err) {

    }

    @Override
    public void onRefresh() {
        if (Build.VERSION.SDK_INT >= 23) {
            new AlertDialog.Builder(getContext())
                    .setMessage("1.允许\"位置信息\"权限\n2.请确保GPS已经打开\n3.不再提醒")
                    .setPositiveButton(getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                        if (basePresenter != null) basePresenter.startScan();
                    })
                    .setNegativeButton(getString(R.string.CALL_CAMERA_NAME), null)
                    .show();
            swRefreshWifi.setRefreshing(true);
        } else {
            if (basePresenter != null) basePresenter.startScan();
            swRefreshWifi.setRefreshing(true);
        }
    }

    private class AAdapter extends SuperAdapter<ScanResult> {

        private AAdapter(Context context, List<ScanResult> items, int layoutResId) {
            super(context, items, layoutResId);
        }

        @Override
        public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, ScanResult item) {
            String ssid = item.SSID.replace("\"", "");
            holder.setImageResource(R.id.imv_wifi_ssid, getWifiIcon(item));
            holder.setText(R.id.tv_wifi_ssid, ssid);
            holder.setOnClickListener(R.id.lLayout_wifi_list_item, clickListener);
            Log.d("WifiList", "list: " + ssid);
        }

        private int getWifiIcon(ScanResult item) {
            int security = NetUtils.getSecurity(item);
            int strength = WifiManager.calculateSignalLevel(item.level, 3);
            if (security == 0) {
                //open
                strength += 4;
            } else {
                //encrypt
                strength += 1;
            }
            int base = R.drawable.setting_icon_wifi_network_security1;
            return base + strength;
        }
    }
}
