package com.cylan.jiafeigou.n.view.record;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yzd on 17-1-5.
 */


public class DelayRecordDeviceFragment extends BaseFragment {


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
        ArrayList<String> list = null;
        if (getArguments() != null) {
            list = getArguments().getStringArrayList(KEY_DEVICES);
        }
        mDeviceAdapter = new DeviceListAdapter(getActivityContext(), list, R.layout.item_delay_record_device);
    }

    @Override
    public void onViewAction(int action, String handler, Object extra) {
        if (action == JFGView.VIEW_ACTION_OFFER) {
            if (TextUtils.equals(handler, "devices")) {
                mDeviceEmptyView.setVisibility(View.GONE);
                mDeviceAdapter.clear();
                mDeviceAdapter.addAll((List<String>) extra);
            }
            if (TextUtils.equals(handler, "empty")) {//无任何可用３G狗设备
                mDeviceEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    @OnClick(R.id.header_delay_record_back)
    public void back() {
        getActivity().onBackPressed();
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_delay_record_select_device;
    }

    private static class DeviceListAdapter extends SuperAdapter<String> {

        public DeviceListAdapter(Context context, List<String> items, int layoutResId) {
            super(context, items, layoutResId);
        }

        @Override
        public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, String item) {
            JFGDevice device = GlobalDataProxy.getInstance().fetch(item);
            holder.setText(R.id.item_bind_device_name, device.alias);
            holder.setText(R.id.item_bind_device_state, "未开启");
        }
    }
}
