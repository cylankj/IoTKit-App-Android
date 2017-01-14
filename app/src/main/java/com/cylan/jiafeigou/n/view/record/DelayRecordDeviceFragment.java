package com.cylan.jiafeigou.n.view.record;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.CameraDevice;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.superadapter.OnItemClickListener;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yzd on 17-1-5.
 */


public class DelayRecordDeviceFragment extends BaseFragment implements OnItemClickListener {


    private static final String KEY_DEVICES = "KEY_DEVICES";

    @BindView(R.id.fragment_delay_record_device_list)
    RecyclerView mDevicesList;
    @BindView(R.id.fragment_delay_record_empty)
    ViewGroup mDeviceEmptyView;

    @BindView(R.id.header_delay_record_container)
    ViewGroup mHeaderContainer;
    private SuperAdapter<String> mDeviceAdapter;

    //// TODO: 17-1-5 获取类型为3G狗的在线设备，目前不知道怎么获取
    public static DelayRecordDeviceFragment newInstance(List<String> devices) {
        DelayRecordDeviceFragment fragment = new DelayRecordDeviceFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(KEY_DEVICES, (ArrayList<String>) devices);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected JFGPresenter onCreatePresenter() {//保留此接口,此view所需数据简单的由父activity提供
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewUtils.setViewMarginStatusBar(mHeaderContainer);
    }

    @Override
    public void onStop() {
        super.onStop();
        ViewUtils.clearViewMarginStatusBar(mHeaderContainer);
    }

    @Override
    protected void initViewAndListener() {
        mDevicesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        ArrayList<String> list = null;
        if (getArguments() != null) {
            list = getArguments().getStringArrayList(KEY_DEVICES);
        }
        mDeviceAdapter = new DeviceListAdapter(getActivityContext(), list, R.layout.item_delay_record_device);
        mDeviceAdapter.setOnItemClickListener(this);
        mDevicesList.setAdapter(mDeviceAdapter);
        refreshLayout();
    }

    private void refreshLayout() {
        if (mDeviceAdapter != null && mDeviceAdapter.getList() != null && mDeviceAdapter.getList().size() > 0) {
            mDeviceEmptyView.setVisibility(View.GONE);
        } else {
            mDeviceEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewAction(int action, String handler, Object extra) {
//        if (action == JFGView.VIEW_ACTION_OFFER) {
//            if (TextUtils.equals(handler, "devices")) {
//                mDeviceEmptyView.setVisibility(View.GONE);
//                mDeviceAdapter.clear();
//                mDeviceAdapter.addAll((List<String>) extra);
//            }
//            if (TextUtils.equals(handler, "empty")) {//无任何可用３G狗设备
//                mDeviceEmptyView.setVisibility(View.VISIBLE);
//            }
//        }
    }

    @OnClick(R.id.header_delay_record_back)
    public void back() {
        getActivity().onBackPressed();
    }


    @Override
    protected int getContentViewID() {
        return R.layout.fragment_delay_record_select_device;
    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {
        String uuid = mDeviceAdapter.getItem(position);
        onViewActionToActivity(JFGView.VIEW_ACTION_OK, DelayRecordContract.View.VIEW_HANDLER_TO_MAIN_VIEW, uuid);
    }

    private static class DeviceListAdapter extends SuperAdapter<String> {

        public DeviceListAdapter(Context context, List<String> items, int layoutResId) {
            super(context, items, layoutResId);
        }

        @Override
        public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, String item) {
            CameraDevice device = DataSourceManager.getInstance().getJFGDevice(item);
            boolean online = device.net != null && (device.net.net != 0 && device.net.net != -1);
            holder.setEnabled(R.id.item_device_container, online);
            holder.setText(R.id.item_device_alias, device.alias);
            holder.setText(R.id.item_device_open_state, device.camera_time_lapse_photography == null ? mContext.getString(R.string.Tap1_Setting_Unopened) : mContext.getString(R.string.Tap1_Setting_Opened));
            holder.setImageResource(R.id.item_device_icon, online ? R.drawable.icon_home_camera_online : R.drawable.icon_home_camera_offline);
        }
    }
}
