package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;
import java.util.Locale;

/**
 * Created by hunt on 16-5-24.
 */

public class HomePageListAdapter extends SuperAdapter<DeviceBean> {

    //    final static int[] deviceIconOnlineRes = {R.drawable.ico_doorbell_online, R.drawable.ico_video_online, R.drawable.ico_efamily_online};
//    final static int[] deviceIconOfflineRes = {R.drawable.ico_doorbell_offline, R.drawable.ico_video_offline, R.drawable.ico_efamily_offline};
    final static int[] deviceIconOnlineRes = {R.drawable.icon_home_doorbell_online, R.drawable.icon_home_camera_online, R.drawable.icon_home_album_online, R.drawable.icon_home_magnetic_online};
    final static int[] deviceIconOfflineRes = {R.drawable.icon_home_doorbell_offline, R.drawable.icon_home_camera_offline, R.drawable.icon_home_album_offline, R.drawable.icon_home_magnetic_offline};
    final static int[] msgContentRes = {R.string.receive_new_news, R.string.receive_new_news, R.string.receive_new_news, R.string.receive_new_news};
    private DeviceItemClickListener deviceItemClickListener;
    private DeviceItemLongClickListener deviceItemLongClickListener;

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
        View view = holder.getView(R.id.rLayout_device_item);
        if (view != null) {
            view.setTag(layoutPosition);
        }
        holder.setOnClickListener(R.id.rLayout_device_item, deviceItemClickListener);
        holder.setOnLongClickListener(R.id.rLayout_device_item, deviceItemLongClickListener);
        handleState(holder, item);
    }

    /**
     * safe mode
     *
     * @param type
     * @return
     */
    private int getDeviceType(int type) {
        return type > 3 ? 3 : type;
    }

    private String getMessageContent(DeviceBean bean) {
        String content = "";
        final int deviceType = bean.deviceType;
        final int msgCount = bean.msgCount;
        return String.format(Locale.getDefault(),
                getContext().getString(msgContentRes[deviceType]), msgCount);
    }

    private String getTime(DeviceBean bean) {
        return "" + bean.msgCount;
    }

    private void setItemState(SuperViewHolder holder, DeviceBean bean) {
        final int share = bean.isShared;
        final int isProtected = bean.isProtectdMode;
        final int netState = bean.netType;

    }

    private void handleState(SuperViewHolder holder, DeviceBean bean) {
        final int onLineState = bean.netType;
        final int deviceType = bean.deviceType;
        int iconRes = onLineState != 0 ? deviceIconOnlineRes[getDeviceType(deviceType)]
                : deviceIconOfflineRes[getDeviceType(deviceType)];
//        int fontColor = onLineState != 0 ? R.color.blue : R.color.red_color;
        //昵称
        holder.setText(R.id.tv_device_alias, TextUtils.isEmpty(bean.alias) ? bean.cid : bean.alias);
//        holder.setTextColor(R.id.tv_device_alias, fontColor);
        //图标
        holder.setBackgroundResource(R.id.img_device_icon, iconRes);
        //消息数
        holder.setText(R.id.tv_device_msg_count, getMessageContent(bean));
//        holder.setTextColor(R.id.tv_device_msg_count, fontColor);
        //时间
        holder.setText(R.id.tv_device_msg_time, getTime(bean));
//        holder.setTextColor(R.id.tv_device_msg_time, fontColor);
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
