package com.cylan.jiafeigou.n.view.setting;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.setting.WifiListContract;
import com.cylan.jiafeigou.n.mvp.impl.setting.WifiListPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.BindDeviceActivity;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.dialog.EditFragmentDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;
import static com.cylan.jiafeigou.n.mvp.contract.setting.WifiListContract.ERR_NO_RAW_LIST;
import static com.cylan.jiafeigou.widget.dialog.BaseDialog.KEY_TITLE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_EXCLUDE_CHINESE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_INPUT_HINT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_INPUT_LENGTH;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_LEFT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_RIGHT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_SHOW_EDIT;

/**
 * A simple {@link Fragment} subclass.
 */
@RuntimePermissions
public class WifiListFragment extends IBaseFragment<WifiListContract.Presenter>
        implements WifiListContract.View, SwipeRefreshLayout.OnRefreshListener, OnItemClickListener {
    private static final int REQ_CODE = 100;
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
    public void onStart() {
        super.onStart();
        WifiListFragmentPermissionsDispatcher.onGrantedLocationPermissionWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        WifiListFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        if (permissions.length == 1) {
            if (TextUtils.equals(permissions[0], ACCESS_FINE_LOCATION) && grantResults[0] > -1) {
                WifiListFragmentPermissionsDispatcher.onGrantedLocationPermissionWithCheck(this);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE) {
            onGrantedLocationPermission();
        }
    }

    private AlertDialog locationGrantedDialog;
    private AlertDialog locationDeniedDialog;

    @NeedsPermission(ACCESS_FINE_LOCATION)
    public void onGrantedLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!MiscUtils.checkGpsAvailable(getActivity().getApplication())) {
                if (locationGrantedDialog == null) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.GetWifiList_FaiTips))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.OK), (@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) -> {
                            });
                    locationGrantedDialog = builder.create();
                }
                if (locationGrantedDialog.isShowing()) return;
                locationGrantedDialog.show();
                return;
            }
        }
        if (basePresenter != null)
            basePresenter.startScan();
    }

    @OnPermissionDenied(ACCESS_FINE_LOCATION)
    public void onDeniedLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (locationDeniedDialog == null) {
                locationDeniedDialog = new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.turn_on_gps))
                        .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
//                    finishExt();
                            if (getActivity() != null && getActivity() instanceof BindDeviceActivity) {
                                ((BindDeviceActivity) getActivity()).finishExt();
                            }
                        })
                        .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                        })
                        .create();
            }
        if (locationDeniedDialog.isShowing()) return;
        locationDeniedDialog.show();
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    public void showRationaleForLocation(PermissionRequest request) {
        onDeniedLocationPermission();
    }

    @Override
    public void setPresenter(WifiListContract.Presenter presenter) {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvWifiList.setAdapter(new AAdapter(getContext(), null, R.layout.layout_wifi_list_item));
        ((AAdapter) rvWifiList.getAdapter()).setOnItemClickListener(this);
        rvWifiList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        swRefreshWifi.setOnRefreshListener(this);
        customToolbar.setBackAction(v -> {//回退事件
            getActivity().getSupportFragmentManager().popBackStack();
        });
    }

    @Override
    public void onResults(ArrayList<ScanResult> results) {
        ((AAdapter) rvWifiList.getAdapter()).clear();
        ((AAdapter) rvWifiList.getAdapter()).addAll(results);
        swRefreshWifi.setRefreshing(false);
    }

    @Override
    public void onErr(int err) {
        if (err == ERR_NO_RAW_LIST) {
            if (Build.VERSION.SDK_INT >= 23) {
                new AlertDialog.Builder(getContext())
                        .setMessage(getString(R.string.GetWifiList_FaiTips))
                        .setPositiveButton(getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                            if (basePresenter != null) basePresenter.startScan();
                        })
                        .setNegativeButton(getString(R.string.CALL_CAMERA_NAME), null)
                        .show();
            } else {
                ToastUtil.showNegativeToast(getString(R.string.Tap1_AddDevice_refreshWifi));
            }
        }
        swRefreshWifi.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if (basePresenter != null) basePresenter.startScan();
        swRefreshWifi.setRefreshing(true);
    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {
        final ScanResult item = ((AAdapter) rvWifiList.getAdapter()).getItem(position);
        Bundle bundle = new Bundle();
        final String ssid = item.SSID.replace("\"", "");
        bundle.putString(KEY_TITLE, ssid);
        bundle.putString(KEY_LEFT_CONTENT, getString(R.string.CARRY_ON));
        bundle.putString(KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
        bundle.putString(KEY_INPUT_HINT, getString(R.string.ENTER_PWD_1));
        bundle.putInt(KEY_INPUT_LENGTH, 63);
        bundle.putBoolean(KEY_EXCLUDE_CHINESE, true);
        final int security = NetUtils.getSecurity(item);
        bundle.putBoolean(KEY_SHOW_EDIT, security != 0);
        EditFragmentDialog dialog = EditFragmentDialog.newInstance(bundle);
        dialog.setAction((int id, Object value) -> {
            if (value != null && value instanceof String) {
                //pwd
                String routeName = NetUtils.getNetName(ContextUtils.getContext());
                Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                DpMsgDefine.DPNet net = device == null ? new DpMsgDefine.DPNet() : device.$(201, new DpMsgDefine.DPNet());
                if (!TextUtils.equals(routeName, net.ssid)) {
                    ToastUtil.showNegativeToast(getString(R.string.setwifi_check, net.ssid));
                    return;
                }
                if (basePresenter != null)
                    basePresenter.sendWifiInfo(ssid, (String) value, security);
                ToastUtil.showToast(getString(R.string.DOOR_SET_WIFI_MSG));
                Intent intent = new Intent(getActivity(), NewHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivity(intent);
            }
        });
        dialog.show(getChildFragmentManager(), "dialog");
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
            Log.d("WifiList", "list: " + ssid);
        }

        private int getWifiIcon(ScanResult item) {
            int security = NetUtils.getSecurity(item);
            int strength = WifiManager.calculateSignalLevel(item.level, 3);
            if (security == 0) {
                //open
                strength += 3;
            } else {
                //encrypt
                strength += 0;
            }
            int base = R.drawable.setting_icon_wifi_network_security1;
            return base + strength;
        }
    }
}
