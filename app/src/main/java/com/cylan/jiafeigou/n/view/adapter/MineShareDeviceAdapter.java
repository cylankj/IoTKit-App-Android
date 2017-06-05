package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/10/24
 * 描述：
 */
public class MineShareDeviceAdapter extends SuperAdapter<DeviceBean> {

    private OnShareClickListener listener;

    public interface OnShareClickListener {
        void onShare(SuperViewHolder holder, int viewType, int layoutPosition, DeviceBean item);
    }

    public void setOnShareClickListener(OnShareClickListener listener) {
        this.listener = listener;
    }

    public MineShareDeviceAdapter(Context context, List<DeviceBean> items, IMulItemViewType<DeviceBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, final int viewType, final int layoutPosition, final DeviceBean item) {
        final int deviceType = item.pid;
        int iconRes = JConstant.getOnlineIcon(deviceType);
        //昵称
        holder.setText(R.id.tv_share_device_name, TextUtils.isEmpty(item.alias) ? item.uuid : item.alias);
        //图标
        holder.setImageDrawable(R.id.iv_share_device_icon, getContext().getResources().getDrawable(iconRes));
        //已分享数
        holder.setText(R.id.tv_has_share_num, item.hasShareCount + "/5");

        if (item.hasShareCount >= 5) {
            holder.setBackgroundResource(R.id.tv_share_device_btn, R.drawable.btn_accept_add_request_shape_gray);
            holder.setTextColor(R.id.tv_share_device_btn, Color.parseColor("#504b9fd5"));
            holder.setEnabled(R.id.tv_share_device_btn, false);
        } else {
            holder.setBackgroundResource(R.id.tv_share_device_btn, R.drawable.btn_accept_add_request_shape);
            holder.setTextColor(R.id.tv_share_device_btn, Color.parseColor("#4b9fd5"));
            holder.setEnabled(R.id.tv_share_device_btn, true);
        }

        holder.setOnClickListener(R.id.tv_share_device_btn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onShare(holder, viewType, layoutPosition, item);
                }
            }
        });
    }

    @Override
    protected IMulItemViewType<DeviceBean> offerMultiItemViewType() {
        return new IMulItemViewType<DeviceBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, DeviceBean deviceBean) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_mine_share_device_items;
            }
        };
    }

}
