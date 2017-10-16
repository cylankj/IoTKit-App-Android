package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;

/**
 * Created by cylan-hunt on 16-7-6.
 */
public class ToBindDeviceListAdapter extends SuperAdapter<ScanResult> {

    public ToBindDeviceListAdapter(Context context) {
        super(context, null, R.layout.bind_device_ap_list_item);
    }


    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, ScanResult item) {
        holder.setText(R.id.tv_ap_list_item, item.SSID);

        View view = holder.getView(R.id.tv_ap_list_item);
        if (view != null) {
            view.setTag(item);
            if (onItemClickListener != null) {
                view.setOnClickListener(onItemClickListener);
            }
        }
    }

    public void setOnItemClickListener(ItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private ItemClickListener onItemClickListener;

    public interface ItemClickListener extends View.OnClickListener {

    }
}
