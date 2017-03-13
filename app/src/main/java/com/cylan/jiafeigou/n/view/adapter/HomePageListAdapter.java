package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.ImageViewTip;

import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by hunt on 16-5-24.
 */

public class HomePageListAdapter extends SuperAdapter<Device> {

    private DeviceItemClickListener deviceItemClickListener;
    private DeviceItemLongClickListener deviceItemLongClickListener;

    public HomePageListAdapter(Context context, List<Device> items, IMulItemViewType<Device> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    public void setDeviceItemClickListener(DeviceItemClickListener deviceItemClickListener) {
        this.deviceItemClickListener = deviceItemClickListener;
    }

    public void setDeviceItemLongClickListener(DeviceItemLongClickListener deviceItemLongClickListener) {
        this.deviceItemLongClickListener = deviceItemLongClickListener;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, Device item) {
        holder.setOnClickListener(R.id.rLayout_device_item, deviceItemClickListener);
        holder.setTag(R.id.rLayout_device_item, layoutPosition);
        holder.setOnLongClickListener(R.id.rLayout_device_item, deviceItemLongClickListener);
        handleState(holder, item);
    }


    private void setItemState(SuperViewHolder holder, String uuid, DpMsgDefine.DPNet net) {
        //0 net type 网络类型
        int resIdNet = JConstant.getNetTypeRes(net != null ? net.net : -1);
        if (resIdNet != -1) {
            holder.setVisibility(R.id.img_device_state_0, VISIBLE);
            holder.setImageResource(R.id.img_device_state_0, resIdNet);
        } else holder.setVisibility(R.id.img_device_state_0, GONE);
        //1 已分享的设备,此设备分享给别的账号.
        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
        if (device != null && !TextUtils.isEmpty(device.shareAccount)) {
            holder.setVisibility(R.id.img_device_state_1, GONE);
        } else {
            if (DataSourceManager.getInstance().isDeviceSharedTo(uuid)) {
                holder.setVisibility(R.id.img_device_state_1, VISIBLE);
                holder.setImageResource(R.id.img_device_state_1, R.drawable.home_icon_net_link);
            } else {
                holder.setVisibility(R.id.img_device_state_1, GONE);
            }
        }
        //2 电量
        if (device != null && JFGRules.isBell(device.pid)) {
            int battery = MiscUtils.safeGet(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_206_BATTERY), 0);
            if (battery < 20 && (net != null && net.net >= 1)) {//在线显示
                holder.setVisibility(R.id.img_device_state_2, VISIBLE);
                holder.setImageResource(R.id.img_device_state_2, R.drawable.home_icon_net_battery);
            } else holder.setVisibility(R.id.img_device_state_2, GONE);
        }
        //3 延时摄影

        //4 安全防护
        DpMsgDefine.DPStandby isStandBY = MiscUtils.safeGet_(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG), DpMsgDefine.DPStandby.empty());
        DpMsgDefine.DPPrimary<Boolean> dpSafe = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
        boolean safe = MiscUtils.safeGet(dpSafe, false);
        if (device != null && isStandBY.standby && safe && JFGRules.isCamera(device.pid)) {
            holder.setVisibility(R.id.img_device_state_3, VISIBLE);
            holder.setImageResource(R.id.img_device_state_3, R.drawable.home_icon_net_security);
        } else {
            holder.setVisibility(R.id.img_device_state_3, GONE);
        }
        //5 安全待机
        if (isStandBY.standby) {
            holder.setVisibility(R.id.img_device_state_4, GONE);
            holder.setImageResource(R.id.img_device_state_4, R.drawable.home_icon_net_standby);
        } else holder.setVisibility(R.id.img_device_state_4, GONE);

    }

    /**
     * | net| 特征值|  描述 |
     * |---|---|---|
     * |NET_CONNECT | -1 | #绑定后的连接中 |
     * |NET_OFFLINE |  0 | #不在线 |
     * |NET_WIFI    |  1 | #WIFI网络 |
     * |NET_2G      |  2 | #2G网络 |
     * |NET_3G      |  3 | #3G网络 |
     * |NET_4G      |  4 | #4G网络  |
     * |NET_5G      |  5 | #5G网络  |
     */

    private void handleState(SuperViewHolder holder, Device device) {
        String uuid = device.uuid;
        DpMsgDefine.DPNet net = com.cylan.jiafeigou.base.module.DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_201_NET);
        String alias = device.alias;
        String shareAccount = device.shareAccount;
        final int onLineState = net != null ? net.net : (device.pid == JConstant.OS_MAGNET ? 1 : 0);
//        final int deviceType = bean.device.pid;
        Log.d("handleState", "handleState: " + uuid + " " + net);
        int online = JConstant.getOnlineIcon(device.pid);
        int offline = JConstant.getOfflineIcon(device.pid);
        int iconRes = (onLineState != 0 && onLineState != -1) ? online : offline;
        //昵称
        holder.setText(R.id.tv_device_alias, getAlias(uuid, alias));
        if (!TextUtils.isEmpty(shareAccount))
            holder.setVisibility(R.id.tv_device_share_tag, VISIBLE);
        else holder.setVisibility(R.id.tv_device_share_tag, GONE);
        //图标
        holder.setBackgroundResource(R.id.img_device_icon, iconRes);
        if (TextUtils.isEmpty(shareAccount))//被分享用户,不显示 消息数
            handleMsgCountTime(holder, uuid, device.pid);
        //右下角状态
        setItemState(holder, uuid, net);
        AppLogger.d(String.format(Locale.getDefault(), "uuid:%s,device.pid:%s", uuid, device.pid));
    }

    private String getAlias(String uuid, String alias) {
        if (TextUtils.equals(alias, uuid)) {
            return uuid;
        }
        return MiscUtils.getBeautifulString(TextUtils.isEmpty(alias) ? uuid : alias, 8);
    }

    private void handleMsgCountTime(SuperViewHolder holder, String uuid, int pid) {
        Pair<Integer, Long> pair = DataSourceManager.getInstance().getUnreadCount(uuid, 505, 512, 222);
        Log.d("HomePageListAdapter", "HomePageListAdapter: 未读消息");
        //消息数
        holder.setText(R.id.tv_device_msg_count, getLastWarnContent(pair, pid, uuid));
        //时间
        holder.setText(R.id.tv_device_msg_time, TimeUtils.getHomeItemTime(getContext(), pair != null && pair.first > 0 ? pair.second : 0));
        ((ImageViewTip) holder.getView(R.id.img_device_icon)).setShowDot(pair != null && pair.first > 0);
    }


    private String getLastWarnContent(Pair<Integer, Long> msgCountPair, int pid, String uuid) {
        final int msgCount = msgCountPair == null ? 0 : msgCountPair.first;
        long msgTime = msgCountPair != null ? msgCountPair.second : 0;
        if (msgCount == 0)
            return getContext().getString(R.string.Tap1_NoMessages);
        if (JFGRules.isCamera(pid)) {
            return String.format(Locale.getDefault(), "[%s]" + getContext().getString(R.string.MSG_WARNING), msgCount > 99 ? "99+" : msgCount);
        }
        if (JFGRules.isBell(pid)) {
            long localTime = PreferencesUtils.getLong(JConstant.KEY_BELL_LAST_ENTER_TIME_PREFIX + uuid, 0);
            if (localTime > msgTime) {
                return getContext().getString(R.string.Tap1_NoMessages);
            }
            return String.format(Locale.getDefault(), "[%s]" + getContext().getString(R.string.someone_call), msgCount > 99 ? "99+" : msgCount);
        }
        return "";
    }

    @Override
    protected IMulItemViewType<Device> offerMultiItemViewType() {
        return new IMulItemViewType<Device>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, Device item) {
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