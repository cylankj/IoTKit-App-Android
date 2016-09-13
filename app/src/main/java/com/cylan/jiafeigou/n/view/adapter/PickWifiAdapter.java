package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.BeanWifiList;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;
import com.cylan.utils.NetUtils;

/**
 * Created by cylan-hunt on 16-7-6.
 */
public class PickWifiAdapter extends SuperAdapter<BeanWifiList> {

    private final Object object = new Object();

    public PickWifiAdapter(Context context) {
        super(context, null, R.layout.layout_pick_wifi_item);
    }

    public void setCheckedResult(ScanResult checkedResult) {
        synchronized (object) {
            int preSecurity = NetUtils.getSecurity(checkedResult);
            for (int i = 0; i < getCount(); i++) {
                BeanWifiList bean = getItem(i);
                boolean flag = TextUtils.equals(checkedResult.SSID, getItem(i).result.SSID)
                        && NetUtils.getSecurity(getItem(i).result) == preSecurity;
                if (bean.checked) {
                    bean.checked = false;
                    notifyItemChanged(i);
                }
                if (!bean.checked && flag) {
                    bean.checked = true;
                    notifyItemChanged(i);
                }
            }
        }
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, BeanWifiList item) {
        holder.setText(R.id.tv_item_ssid, item.result.SSID);
        View view = holder.getView(R.id.lLayout_device_item);
        if (view != null) {
            if (onItemClickListener != null)
                view.setOnClickListener(onItemClickListener);
        }
        ((RadioButton) holder.getView(R.id.rbtn_item_check)).setChecked(item.checked);
    }


    public void setOnItemClickListener(ItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private ItemClickListener onItemClickListener;

    public interface ItemClickListener extends View.OnClickListener {

    }
}
