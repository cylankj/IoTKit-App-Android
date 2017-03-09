package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/24
 * 描述：
 */
public class ChooseShareDeviceAdapter extends SuperAdapter<DeviceBean> {

    private OnCheckClickListener listener;

    public interface OnCheckClickListener {
        void onCheckClick(DeviceBean item);
    }

    public void setOnCheckClickListener(OnCheckClickListener listener) {
        this.listener = listener;
    }

    public ChooseShareDeviceAdapter(Context context, List<DeviceBean> items, IMulItemViewType<DeviceBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, int layoutPosition, final DeviceBean item) {
        final int deviceType = item.pid;

        int iconRes = JConstant.getOnlineIcon(deviceType);
        //昵称
        holder.setText(R.id.tv_device_name, TextUtils.isEmpty(item.alias) ? item.uuid : item.alias);
        //图标
        holder.setImageDrawable(R.id.iv_device_icon, getContext().getResources().getDrawable(iconRes));
        //已分享数
        final TextView hasShareNum = holder.getView(R.id.tv_share_device_number);
        hasShareNum.setText(item.hasShareCount + "/5");

        CheckBox checkBox = holder.getView(R.id.cbx_share_isCheck);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.isChooseFlag = isChecked ? 1 : 0;
                item.hasShareCount = isChecked ? item.hasShareCount + 1 : item.hasShareCount - 1;
                hasShareNum.setText(item.hasShareCount + "/5");

                if (listener != null) {
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
                return R.layout.fragment_friend_share_device_items;
            }
        };
    }
}

