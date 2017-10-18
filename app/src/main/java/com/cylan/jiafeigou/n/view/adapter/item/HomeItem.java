package com.cylan.jiafeigou.n.view.adapter.item;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.Attributes;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.ImageViewTip;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by hds on 17-5-4.
 */

public class HomeItem extends AbstractItem<HomeItem, HomeItem.ViewHolder> {

    //    private Device mDevice;
    private String uuid;
    private Context mContext;

    public HomeItem withUUID(String uuid, Device device) {
        this.uuid = uuid;
//        mDevice = device;
        return this;
    }

    public HomeItem withUUID(String uuid) {
        this.uuid = uuid;
//        mDevice = device;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        HomeItem item = (HomeItem) o;

        return uuid != null ? uuid.equals(item.uuid) : item.uuid == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        return result;
    }

    @Override
    public long getIdentifier() {
        Device device = getDevice();
        String uuid = device.uuid == null ? "" : device.uuid;
        return uuid.hashCode();
    }

    public Device getDevice() {
        return DataSourceManager.getInstance().getDevice(uuid);
    }

    public String getUUid() {
        return uuid;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        this.mContext = holder.imgDeviceIcon.getContext();
        handleState(holder);

        // TODO: 2017/8/21 需要 RxJava2的支持 ,直接将界面上的属性 bind 到 ObjectBox 中 ,可以简化逻辑
        //            Box<PropertyItem> itemBox = BaseApplication.getPropertyItemBox();
//
//            BaseApplication.getDeviceBox().query().build().subscribe().
//
//            String uuid = String.valueOf(device.getUuid());
//            long[] properties = new long[]{
//                    CacheHolderKt.msgIdKey(uuid, 201),
//                    CacheHolderKt.msgIdKey(uuid, 206),
//                    CacheHolderKt.msgIdKey(uuid, 508),
//                    CacheHolderKt.msgIdKey(uuid, 501),
    }


    private void setItemState(ViewHolder holder, String uuid, DpMsgDefine.DPNet net) {
        //0 net type 网络类型
        int resIdNet = JConstant.getNetTypeRes(net != null ? net.net : -1);
        Device device = getDevice();
        if (JFGRules.isPan720(device.pid) && JFGRules.isAPDirect(getUUid(), getDevice().$(202, ""))) {
            holder.setVisibility(R.id.img_device_state_net, VISIBLE);
            holder.setImageResource(R.id.img_device_state_net, R.drawable.home_icon_ap);
            Object state = DataSourceManager.getInstance().getDeviceState(uuid);
            //有录像状态
            holder.setImageResource(R.id.img_device_state_record, R.drawable.home_icon_recording);
            holder.setVisibility(R.id.img_device_state_record, state == null ? GONE : VISIBLE);
        } else if (resIdNet != -1) {
            holder.setVisibility(R.id.img_device_state_net, VISIBLE);
            holder.setImageResource(R.id.img_device_state_net, resIdNet);
            Object state = DataSourceManager.getInstance().getDeviceState(uuid);
            //有录像状态
            holder.setImageResource(R.id.img_device_state_record, R.drawable.home_icon_recording);
            holder.setVisibility(R.id.img_device_state_record, state == null ? GONE : VISIBLE);
        } else {
            holder.setVisibility(R.id.img_device_state_net, GONE);
            holder.setImageResource(R.id.img_device_state_record, R.drawable.home_icon_recording);
            holder.setVisibility(R.id.img_device_state_record, GONE);
        }

        //1 已分享的设备,此设备分享给别的账号.
        if (device != null && !isPrimaryAccount(device.shareAccount)) {
            holder.setVisibility(R.id.img_device_state_share, GONE);
        } else {
            if (BaseApplication.getAppComponent().getSourceManager().isDeviceSharedTo(uuid)) {
                holder.setVisibility(R.id.img_device_state_share, VISIBLE);
                holder.setImageResource(R.id.img_device_state_share, R.drawable.home_icon_net_link);
            } else {
                holder.setVisibility(R.id.img_device_state_share, GONE);
            }
        }
        //2 电量
        if (device != null && JFGRules.isDeviceOnline(net) && JFGRules.showBattery(device.pid, false)) {//设备在线才显示电量
            int battery = device.$(206, 0);
            if (battery <= 20 && (JFGRules.isBell(device.pid) || JFGRules.isFreeCam(device.pid))) {//门铃和freeCam 电量低于20%在线显示
                holder.setVisibility(R.id.img_device_state_battery, VISIBLE);
                holder.setImageResource(R.id.img_device_state_battery, R.drawable.home_icon_net_battery);
            } else if (battery <= 50 && JFGRules.is3GCam(device.pid)) {//3G狗 低于50%在线显示
                holder.setVisibility(R.id.img_device_state_battery, VISIBLE);
                holder.setImageResource(R.id.img_device_state_battery, R.drawable.home_icon_net_battery);
            } else {
                holder.setVisibility(R.id.img_device_state_battery, GONE);
                holder.setImageResource(R.id.img_device_state_battery, android.R.color.transparent);
            }
        } else {
            holder.setImageResource(R.id.img_device_state_battery, android.R.color.transparent);
            holder.setVisibility(R.id.img_device_state_battery, GONE);
        }
        //3 延时摄影

        //4 安全防护
        DpMsgDefine.DPStandby isStandBY = device.$(508, new DpMsgDefine.DPStandby());
        boolean safe = device.$(501, false);
        if (!isStandBY.standby && safe && JFGRules.isCamera(device.pid) && isPrimaryAccount(device.shareAccount) && JFGRules.isDeviceOnline(net)) {
            holder.setVisibility(R.id.img_device_state_safe, VISIBLE);
            holder.setImageResource(R.id.img_device_state_safe, R.drawable.home_icon_net_security);
        } else {
            holder.setVisibility(R.id.img_device_state_safe, GONE);
        }
        //5 安全待机
        holder.setVisibility(R.id.img_device_state_standby, isStandBY.standby ? VISIBLE : GONE);
        if (isStandBY.standby && net != null && net.net > 0) {
            holder.setImageResource(R.id.img_device_state_standby, R.drawable.home_icon_net_standby);
        }
        //6.OS:84,有线模式已经连接。
        final boolean showWiredIcon = JFGRules.hasProperty(device.pid, Attributes.WIREDMODE);
        if (showWiredIcon) {
            boolean wiredModeEnable = device.$(225, 0) == 1;
            boolean wiredModeOnline = device.$(226, 0) == 1;
            AppLogger.e("缺图标");
            holder.setVisibility(R.id.img_device_wired, wiredModeEnable && wiredModeOnline ? VISIBLE : GONE);
            holder.setImageResource(R.id.img_device_wired, R.drawable.home_icon_wired);
        } else {
            holder.setVisibility(R.id.img_device_wired, GONE);
        }
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

    private void handleState(ViewHolder holder) {
        Device device = getDevice();
        final String uuid = device.uuid;
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        final String alias = device.alias;
        final String shareAccount = device.shareAccount;
        final boolean deviceOnline = JFGRules.isDeviceOnline(net);
        Log.d("handleState", "handleState: " + uuid + " " + net + "," + deviceOnline);
        int online = JConstant.getOnlineIcon(device.pid);
        int offline = JConstant.getOfflineIcon(device.pid);
        boolean apMode = JFGRules.isAPDirect(getUUid(), getDevice().$(202, ""));

        int iconRes = (deviceOnline && NetUtils.getJfgNetType(holder.imgDeviceState3.getContext()) > 0) || (JFGRules.isPan720(device.pid) && apMode) ? online : offline;
        //昵称
        holder.setText(R.id.tv_device_alias, getAlias(uuid, alias));
        if (!isPrimaryAccount(shareAccount)) {
            holder.setVisibility(R.id.tv_device_share_tag, VISIBLE);
        } else {
            holder.setVisibility(R.id.tv_device_share_tag, GONE);
        }
        //图标
        holder.setImageResource(R.id.img_device_icon, iconRes);
        handleMsgCountAndTime(holder, uuid, device);
        //右下角状态
        setItemState(holder, uuid, net);
    }

    private String getAlias(String uuid, String alias) {
        if (TextUtils.equals(alias, uuid)) {
            return uuid;
        }
        return MiscUtils.getBeautifulString(TextUtils.isEmpty(alias) ? uuid : alias, 8);
    }

    private void handleMsgCountAndTime(ViewHolder holder, String uuid, Device mDevice) {
        //被分享用户,不显示 消息数
        Context context = holder.tvDeviceAlias.getContext();
        Pair<DPEntity, Integer> pair = handleUnreadCount(mDevice);
        DPEntity entity = pair == null ? null : pair.first;
        Log.d("HomePageListAdapter", "HomePageListAdapter: 未读消息:" + pair);
        boolean isPrimaryDevice = isPrimaryAccount(mDevice.shareAccount);
        boolean show = needShowUnread(mDevice, isPrimaryDevice);
        //消息数,狗日的门铃的分享设备需要显示.

        String warnContent = getLastWarnContent(pair, mDevice.pid, uuid);
        holder.setText(R.id.tv_device_msg_count, !show ? "" : warnContent);
        //时间
        long time = entity != null && entity.getValue(0) > 0 ? entity.getVersion() : 0;
        if (time != 0) {//服务器返回的数据是错的,过滤掉
            holder.setText(R.id.tv_device_msg_time, !show ? "" : TimeUtils.getHomeItemTime(context, time));
        }
        ((ImageViewTip) holder.getView(R.id.img_device_icon)).setShowDot(show && entity != null && entity.getValue(0) > 0);
    }

    /**
     * 设备状态应该在缓存中
     *
     * @param uuid
     * @return
     */
    private String getPanOnlineMode(final String uuid) {
        boolean serverOnline = BaseApplication.getAppComponent().getSourceManager().isOnline();
        Device device = getDevice();
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        if (JFGRules.isAPDirect(device.uuid, getDevice().$(202, ""))) {
            return mContext.getString(R.string.Tap1_OutdoorMode);
        } else if (/*serverOnline &&*** #114473***/ net.net == 1) {
            //wifi在线
            return mContext.getString(R.string.DEVICE_WIFI_ONLINE);
        }
        return mContext.getString(R.string.OFF_LINE);//离线
    }

    /**
     * 显示未读消息的条件
     *
     * @param mDevice
     * @param isPrimaryDevice
     * @return
     */
    private boolean needShowUnread(Device mDevice, boolean isPrimaryDevice) {
        if (JFGRules.isPan720(mDevice.pid)) {
            return true;//分享的720 设备享有所有的权限
        }
        if (JFGRules.isCamera(mDevice.pid)) {
            return isPrimaryDevice;//摄像头,分享设备不显示.
        }
        if (JFGRules.isBell(mDevice.pid)) {
            return true;//门铃要显示
        }
        return false;
    }

    private boolean isPrimaryAccount(String share) {
        return TextUtils.isEmpty(share);
    }

    private Pair<DPEntity, Integer> handleUnreadCount(Device mDevice) {
        if (JFGRules.isCamera(mDevice.pid)) {
            return new Pair<>(MiscUtils.getMaxVersionEntity(mDevice.getProperty(1001), mDevice.getProperty(1002), mDevice.getProperty(1003)),
                    mDevice.$(1001, 0) + mDevice.$(1002, 0) + mDevice.$(1003, 0));
        } else if (JFGRules.isBell(mDevice.pid)) {
            return new Pair<>(MiscUtils.getMaxVersionEntity(mDevice.getProperty(1004), mDevice.getProperty(1005)),
                    mDevice.$(1004, 0) + mDevice.$(1005, 0));
        }
        return null;
    }

    private String getLastWarnContent(Pair<DPEntity, Integer> pair, int pid, String uuid) {
        try {
            if (pair == null) {
                return "";
            }
            DPEntity entity = pair.first;
            final int msgCount = pair.second;
            long msgTime = msgCount == 0 ? 0 : (entity != null ? entity.getVersion() : 0);
            if (msgCount == 0) {
                return mContext.getString(R.string.Tap1_NoMessages);
            }
            if (JFGRules.isCamera(pid)) {
                return String.format(Locale.getDefault(), "[%s]" + mContext.getString(R.string.MSG_WARNING), msgCount > 99 ? "99+" : msgCount);
            }
            if (JFGRules.isBell(pid)) {
                long localTime = PreferencesUtils.getLong(JConstant.KEY_BELL_LAST_ENTER_TIME_PREFIX + uuid, 0);
//                List<DPEntity> entities = BaseApplication.getAppComponent().getDBHelper().queryDPMsg(uuid, 401, Long.MAX_VALUE, Integer.MAX_VALUE);
//                int unreadCount = 0;
//                DpMsgDefine.DPBellCallRecord record = new DpMsgDefine.DPBellCallRecord();
//                record.isOK = -1;
//                for (DPEntity dpEntity : entities) {
//                    DpMsgDefine.DPBellCallRecord value = dpEntity.getValue(record);
//                    if (value.isOK == 0 && value.time * 1000L > localTime) ++unreadCount;
//                }
//                if (unreadCount == 0) {
//                    return getContext().getString(R.string.Tap1_NoMessages);
//                }
                if (localTime > msgTime) {
                    return mContext.getString(R.string.Tap1_NoMessages);
                }
                return String.format(Locale.getDefault(), "[%s]" + mContext.getString(R.string.someone_call), msgCount > 99 ? "99+" : msgCount);
            }
        } catch (Exception e) {
        }
        return "";
    }


    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        // TODO: 2017/8/21 解除对数据的订阅  需要 RxJava2 的支持
    }

    @Override
    public int getType() {
        return R.id.rLayout_device_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.layout_item_home_page_list;
    }

    public static class ViewHolder extends SuperViewHolder {

        @BindView(R.id.img_device_icon)
        ImageViewTip imgDeviceIcon;
        @BindView(R.id.tv_device_alias)
        TextView tvDeviceAlias;
        @BindView(R.id.tv_device_share_tag)
        TextView tvDeviceShareTag;
        @BindView(R.id.tv_device_msg_count)
        TextView tvDeviceMsgCount;
        @BindView(R.id.tv_device_msg_time)
        TextView tvDeviceMsgTime;
        @BindView(R.id.img_device_state_net)
        ImageView imgDeviceState0;
        @BindView(R.id.img_device_state_share)
        ImageView imgDeviceState1;
        @BindView(R.id.img_device_state_battery)
        ImageView imgDeviceState2;
        @BindView(R.id.img_device_state_safe)
        ImageView imgDeviceState3;
        @BindView(R.id.img_device_state_standby)
        ImageView imgDeviceState4;
        @BindView(R.id.img_device_wired)
        ImageView imgDeviceState5;

        com.cylan.jiafeigou.server.cache.Device device;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindSubscibe(com.cylan.jiafeigou.server.cache.Device device) {
            this.device = device;
            // TODO: 2017/8/21 不支持 RxJava1 暂时不做
//            Box<PropertyItem> itemBox = BaseApplication.getPropertyItemBox();
//
//            BaseApplication.getDeviceBox().query().build().subscribe().
//
//            String uuid = String.valueOf(device.getUuid());
//            long[] properties = new long[]{
//                    CacheHolderKt.msgIdKey(uuid, 201),
//                    CacheHolderKt.msgIdKey(uuid, 206),
//                    CacheHolderKt.msgIdKey(uuid, 508),
//                    CacheHolderKt.msgIdKey(uuid, 501),
//
//
//            };
//            itemBox.query().in(PropertyItem_.__ID_PROPERTY, properties)
        }

        public void unbindSubscibe() {

        }


    }


}