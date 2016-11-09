package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.MineShareDeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/24
 * 描述：
 */
public class ChooseShareDeviceAdapter extends SuperAdapter<DeviceBean> {

    private OnCheckClickListener listener;

    public interface OnCheckClickListener{
        void onCheckClick(DeviceBean item);
    }

    public void setOnCheckClickListener(OnCheckClickListener listener){
        this.listener = listener;
    }

    public ChooseShareDeviceAdapter(Context context, List<DeviceBean> items, IMulItemViewType<DeviceBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, final DeviceBean item) {
        //TODO 已分享的人数
        holder.setText(R.id.tv_share_device_number, "2/5");
        holder.setText(R.id.tv_device_name,item.alias);
        CheckBox checkBox = holder.getView(R.id.cbx_share_isCheck);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.isChooseFlag = isChecked ? 1:0;
                if (listener != null){
                    listener.onCheckClick(item);
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
            public int getItemViewType(int position, DeviceBean bean) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_relative_friend_share_device_items;
            }
        };
    }
}

