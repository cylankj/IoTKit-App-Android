package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by hunt on 16-5-24.
 */

public class HomePageListAdapter extends SuperAdapter<DeviceBean> {

    private final static int[] msgContentRes = {R.string.receive_new_news,
            R.string.receive_new_news,
            R.string.receive_new_news,
            R.string.receive_new_news};
    private DeviceItemClickListener deviceItemClickListener;
    private DeviceItemLongClickListener deviceItemLongClickListener;

    private static final SimpleDateFormat format_0 = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat format_1 = new SimpleDateFormat("yy/M/d", Locale.getDefault());

    private static final Date date = new Date();

    public HomePageListAdapter(Context context, List<DeviceBean> items, IMulItemViewType<DeviceBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    public void setDeviceItemClickListener(DeviceItemClickListener deviceItemClickListener) {
        this.deviceItemClickListener = deviceItemClickListener;
    }

    public void setDeviceItemLongClickListener(DeviceItemLongClickListener deviceItemLongClickListener) {
        this.deviceItemLongClickListener = deviceItemLongClickListener;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, DeviceBean item) {
        holder.setOnClickListener(R.id.rLayout_device_item, deviceItemClickListener);
        holder.setOnLongClickListener(R.id.rLayout_device_item, deviceItemLongClickListener);
        handleState(holder, item);
    }


    private String getMessageContent(DeviceBean bean) {
        final int deviceType = bean.pid;
        final int msgCount = bean.msgCount;
        return String.format(Locale.getDefault(),
                getContext().getString(msgContentRes[0]), msgCount);
    }

    private String convertTime(DeviceBean bean) {
        final long timeInterval = System.currentTimeMillis() - bean.msgTime;
        if (timeInterval <= 5 * 60 * 1000) {
            return getContext().getString(R.string.JUST_NOW);
        } else if (timeInterval <= 24 * 60 * 1000) {
            date.setTime(bean.msgTime);
            return format_0.format(date);
        } else {
            date.setTime(bean.msgTime);
            return format_1.format(date);
        }
    }

    /**
     * 通过cid查找index
     *
     * @param cid
     * @return
     */
    public DeviceBean findTarget(final String cid) {
        if (getCount() == 0 || TextUtils.isEmpty(cid))
            return null;
        ArrayList<DeviceBean> arrayList = new ArrayList<>(getList());
        for (DeviceBean bean : arrayList) {
            if (TextUtils.equals(bean.uuid, cid)) {
                return bean;
            }
        }
        return null;
    }

    private void setItemState(SuperViewHolder holder, DeviceBean bean) {
        final int share = bean.isShared;
        final int isProtected = bean.isProtectedMode;
        final int netState = bean.netType;

    }

    private void handleState(SuperViewHolder holder, DeviceBean bean) {
        final int onLineState = bean.netType;
        final int deviceType = bean.pid;
        int iconRes = onLineState != 0 ? JConstant.onLineIconMap.get(deviceType)
                : JConstant.offLineIconMap.get(deviceType);
        //昵称
        holder.setText(R.id.tv_device_alias, TextUtils.isEmpty(bean.alias) ? bean.uuid : bean.alias);
        //图标
        holder.setBackgroundResource(R.id.img_device_icon, iconRes);
        //消息数
        holder.setText(R.id.tv_device_msg_count, getMessageContent(bean));
        //时间
        holder.setText(R.id.tv_device_msg_time, convertTime(bean));
        //右下角状态
        setItemState(holder, bean);
    }

    @Override
    protected IMulItemViewType<DeviceBean> offerMultiItemViewType() {
        return new IMulItemViewType<DeviceBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, DeviceBean DeviceBean) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.layout_item_home_page_list;
            }
        };
    }

    public interface DeviceItemClickListener extends View.OnClickListener {

    }

    public interface DeviceItemLongClickListener extends View.OnLongClickListener {

    }
}
