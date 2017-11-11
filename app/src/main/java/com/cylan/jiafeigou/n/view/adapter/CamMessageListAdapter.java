package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.n.view.cam.item.FaceItem;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-7-13.
 */
public class CamMessageListAdapter extends SuperAdapter<CamMessageBean> {
    private static final String TAG = "CamMessageListAdapter";
    /**
     * 图片，只有文字,加载。
     */
    private static final int MAX_TYPE = 5;
    private String uuid;
    /**
     * 0： 正常，1:编辑
     */
    private boolean editMode;
    //图片Container的总体宽度,可能有3条,可能有2条.
    private final int pic_container_width;//宽度是固定的，需要调整高度。
    private Map<Integer, Integer> selectedMap = new HashMap<>();

    private boolean isSharedDevice = false;
    private DpMsgDefine.DPSdcardSummary summary;
    private boolean status;
    private Map<String, String> personMaps = new HashMap<>();
    private Map<String, List<CamMessageBean>> visitorMaps = new HashMap<>();

    public CamMessageListAdapter(String uiid, Context context, List<CamMessageBean> items, IMulItemViewType<CamMessageBean> mulItemViewType) {
        super(context, items, mulItemViewType);
        //这个40是根据layout中的marginStart,marginEnd距离提取出来,如果需要修改,参考这个layout
        pic_container_width = (int) (Resources.getSystem().getDisplayMetrics().widthPixels
                - getContext().getResources().getDimension(R.dimen.x34));
        this.uuid = uiid;
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        this.isSharedDevice = device != null && device.available() && !TextUtils.isEmpty(device.shareAccount);
    }

    /*
     * 是否有卡,不检查卡的读写失败
     *
     * @return
     */
    private boolean hasSdcard() {
        DpMsgDefine.DPSdStatus status = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid).$(204, new DpMsgDefine.DPSdStatus());

        return this.status = status.hasSdcard && status.err == 0;
    }

    private boolean online() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        return net != null && net.net > 0;
    }

    public boolean isEditMode() {
        return editMode;
    }

    /**
     * 翻转
     *
     * @param lastVisiblePosition
     */
    public void reverseMode(boolean reverse, final int lastVisiblePosition) {
        this.editMode = reverse;
        if (!editMode) {
            selectedMap.clear();
        }
        updateItemFrom(lastVisiblePosition);
    }

    /**
     * 全选或者反选
     *
     * @param mark
     */
    public void markAllAsSelected(boolean mark, int lastPosition) {
        if (mark) {
            for (int i = 0; i < getCount(); i++) {
                selectedMap.put(i, i);
            }
        } else {
            selectedMap.clear();
        }
        notifyItemRangeChanged(0, getCount());
    }

    /**
     * 收集已经选中的
     *
     * @return
     */
    public ArrayList<CamMessageBean> getSelectedItems() {
        ArrayList<CamMessageBean> list = new ArrayList<>();
        for (int index : selectedMap.keySet()) {
            list.add(getItem(index));
        }
        return list;
    }

    /**
     * 更新部分item
     *
     * @param position
     */
    private void updateItemFrom(int position) {
        synchronized (CamMessageListAdapter.class) {
            for (int i = 0; i < getCount(); i++) {
                if (i <= position)//没必要全部
                {
                    notifyItemChanged(i);
                }
            }
        }
    }

    public boolean markItemSelected(int position) {
        if (!editMode) {
            return false;
        }
        if (selectedMap.containsKey(position)) {
            selectedMap.remove(position);
            notifyItemChanged(position);
            return false;
        } else {
            selectedMap.put(position, position);
            notifyItemChanged(position);
            return true;
        }
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, CamMessageBean item) {

        switch (viewType) {
            case CamMessageBean.ViewType.TEXT:
                handleTextContentLayout(holder, item);
                break;
            case CamMessageBean.ViewType.ONE_PIC:
            case CamMessageBean.ViewType.TWO_PIC:
            case CamMessageBean.ViewType.THREE_PIC:
                handlePicsLayout(holder, item);
            default:
        }
        if (viewType == CamMessageBean.ViewType.FOOT) {
            return;
        }
        if (onClickListener != null) {
            holder.setOnClickListener(R.id.lLayout_cam_msg_container, onClickListener);
            holder.setOnClickListener(R.id.tv_cam_message_item_delete, onClickListener);
        }
        boolean faceFragment = JFGRules.isFaceFragment(DataSourceManager.getInstance().getDevice(uuid).pid);
        boolean sameDay = true;
        if (layoutPosition > 0) {
            CamMessageBean bean = getItem(layoutPosition - 1);
            sameDay = TimeUtils.isSameDay(bean.message.version, item.message.version);
        }
        boolean showDivider = !sameDay && faceFragment;
        holder.setVisibility(R.id.message_time_divider, showDivider ? View.VISIBLE : View.GONE);
        String content = TimeUtils.getSpecifiedDate(item.message.version);
        holder.setText(R.id.time_divider, content);
//        holder.setVisibility(R.id.watcher_text, showDivider ? View.VISIBLE : View.GONE);
//        if (showDivider) {
//            String content = TimeUtils.getTimeSpecial(item.version);
//            holder.setText(R.id.time_divider, content);
//            // TODO: 2017/10/18 显示右边的人数统计,现在不知道从哪获取数据
//
//            holder.setText(R.id.watcher_text, "最近30天来访15次");
//        }

        //设置删除可见性,共享设备不可删除消息
//        Device device = DataSourceManager.getInstance().getDevice(uuid);
        //720 设备享有所有权限
        holder.setVisibility(R.id.tv_cam_message_item_delete, faceFragment ? View.INVISIBLE : View.VISIBLE);
        holder.setOnClickListener(R.id.tv_jump_next, onClickListener);
        holder.setVisibility(R.id.fl_item_time_line, isEditMode() ? View.INVISIBLE : View.VISIBLE);
        holder.setVisibility(R.id.rbtn_item_check, isEditMode() ? View.VISIBLE : View.INVISIBLE);
        holder.setVisibility(R.id.fLayout_cam_message_item_bottom, !isEditMode() ? View.VISIBLE : View.INVISIBLE);
        holder.setChecked(R.id.rbtn_item_check, isEditMode() && selectedMap.containsKey(layoutPosition));
    }

    /**
     * 显示直播按钮
     *
     * @param time
     * @return
     */

    private boolean showLiveBtn(long time) {
        return (System.currentTimeMillis() - time) >= 30 * 60 * 1000L && hasSdcard() && !isSharedDevice;
    }

    private boolean showHistoryButton(CamMessageBean bean) {
        if (isSharedDevice || !hasSdcard() || bean == null) {
            return false;
        }
        switch ((int) bean.message.getMsgId()) {
            case DpMsgMap.ID_401_BELL_CALL_STATE: {
                DpMsgDefine.DPBellCallRecord dpBellCallRecord = (DpMsgDefine.DPBellCallRecord) bean.message;
                return dpBellCallRecord.isRecording == 1;
            }
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                Device device = DataSourceManager.getInstance().getDevice(uuid);
                boolean pan720 = JFGRules.isPan720(device.pid);
                DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) bean.message;
                if (pan720) {
//                return bean.alarmMsg.isRecording == 1;//全部当成图片处理,
                    // TODO: 2017/8/4 当前查看视频不知道怎么处理
                    return false;
                } else {
                    return dpAlarm.isRecording == 1 && (System.currentTimeMillis() - dpAlarm.version) >= 30 * 60 * 1000L;
                }
            }
        }
        return false;
    }

    private boolean textShowSdBtn(CamMessageBean item) {
        //考虑这个bean的条件.
        // TODO: 2017/8/16  不光要看 hasSDCard 还要看 err 是否为0 #118051
        // TODO: 2017/8/16 Android（1.1.0.534）720设备 报警中心界面 提示"检车到新的Micro SD卡，需要先初始化才能存储视频" 右下角没有查看详情 按钮
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
//        DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());
        boolean hasSdcard;
        int err = -1;
        switch ((int) item.message.getMsgId()) {
            case DpMsgMap.ID_222_SDCARD_SUMMARY: {
                DpMsgDefine.DPSdcardSummary sdcardSummary = (DpMsgDefine.DPSdcardSummary) item.message;
                hasSdcard = sdcardSummary.hasSdcard;
                err = sdcardSummary.errCode;
            }
            break;
            default: {
//                Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());
                hasSdcard = status.hasSdcard;
                err = status.err;
            }
        }
        return hasSdcard && err != 0 && (!isSharedDevice || JFGRules.isPan720(device.pid));
    }

    /**
     * 来自一个全局的通知消息
     */
    public void notifySdcardStatus(boolean status, int position) {
        if (this.status != status) {//不一样才 notify
            this.status = status;
            updateItemFrom(position);
        }
    }

    public void notifyDeviceOnlineState(boolean online, int position) {
        updateItemFrom(position);
    }

    /**
     * sd卡状态消息
     *
     * @param holder
     * @param item
     */
    private void handleTextContentLayout(SuperViewHolder holder,
                                         CamMessageBean item) {
        holder.setText(R.id.tv_cam_message_item_date, getFinalTimeContentSD(item));
        holder.setText(R.id.tv_cam_message_list_content, getFinalSdcardContent(item));
        holder.setVisibility(R.id.tv_jump_next, textShowSdBtn(item) ? View.VISIBLE : View.GONE);


    }

    private void handlePicsLayout(SuperViewHolder holder,
                                  CamMessageBean item) {
        int count = 0;

        switch ((int) item.message.getMsgId()) {
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) item.message;
                count = dpAlarm.fileIndex < 1 ? 1 : MiscUtils.getCount(dpAlarm.fileIndex);
            }
            break;
            case DpMsgMap.ID_401_BELL_CALL_STATE: {
                DpMsgDefine.DPBellCallRecord dpBellCallRecord = (DpMsgDefine.DPBellCallRecord) item.message;
                count = dpBellCallRecord.fileIndex < 1 ? 1 : MiscUtils.getCount(dpBellCallRecord.fileIndex);
            }
            break;
        }

        count = Math.max(count, 1);//最小为1
        for (int index = 1; index <= count; index++) {
            int id = index == 1 ? R.id.imgV_cam_message_pic0
                    : index == 2 ? R.id.imgV_cam_message_pic1 :
                    R.id.imgV_cam_message_pic2;
            GlideApp.with(getContext())
                    .load(MiscUtils.getCamWarnUrlV2(uuid, item, index))
                    .placeholder(R.drawable.wonderful_pic_place_holder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into((ImageView) holder.getView(id));
            holder.setOnClickListener(id, onClickListener);
        }
        holder.setText(R.id.tv_cam_message_item_date, getFinalTimeContent(item));
        Log.d(TAG, "handlePicsLayout: " + (System.currentTimeMillis() - item.message.version));
        holder.setVisibility(R.id.tv_jump_next, showHistoryButton(item) ? View.VISIBLE : View.GONE);
    }

    private View.OnClickListener onClickListener;

    public void setOnclickListener(View.OnClickListener onclickListener) {
        this.onClickListener = onclickListener;
    }

    /**
     * 时间
     *
     * @param bean
     * @return
     */
    private String getFinalTimeContent(CamMessageBean bean) {
        String tContent = TimeUtils.getHH_MM(bean.message.getVersion()) + " ";
        switch ((int) bean.message.getMsgId()) {
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) bean.message;
            /*现在人形检测也是用的这个消息,增加了扩展字段,有人形的提示和无人形的提示有区别
            * 1.有人形提示:检测到 XXX
            * 2.无人形提示:有新的发现
            * */
                if (dpAlarm.face_id != null /*&& bean.alarmMsg.humanNum > 0*/) {
                    String faceText = JConstant.getFaceText(dpAlarm.face_id, personMaps, null);

                    return tContent + (TextUtils.isEmpty(faceText) ?
                            getContext().getString(R.string.DETECTED_AI) + " " + getContext().getString(R.string.MESSAGES_FILTER_STRANGER)
                            : getContext().getString(R.string.DETECTED_AI) + " " + faceText);
                } else if (dpAlarm.objects != null && dpAlarm.objects.length > 0) {//有检测数据
                    return tContent + getContext().getString(R.string.DETECTED_AI) + " " + JConstant.getAIText(dpAlarm.objects);
//                return tContent + "检测到" + JConstant.getAIText(bean.alarmMsg.objects);
                } else {//无检测数据
                    return tContent + getContext().getString(R.string.MSG_WARNING);
                }
            }

            case DpMsgMap.ID_401_BELL_CALL_STATE: {
                DpMsgDefine.DPBellCallRecord dpBellCallRecord = (DpMsgDefine.DPBellCallRecord) bean.message;
                return tContent + (dpBellCallRecord.isOK == 1 ? getContext().getString(R.string.DOOR_CALL) : getContext().getString(R.string.DOOR_UNCALL));
            }
        }
        return tContent;
    }


    /**
     * 时间
     *
     * @param bean
     * @return
     */
    private String getFinalTimeContentSD(CamMessageBean bean) {
        String tContent = TimeUtils.getHH_MM(bean.message.getVersion()) + " ";
        switch ((int) bean.message.getMsgId()) {
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {

            }
            case DpMsgMap.ID_401_BELL_CALL_STATE: {
                return tContent + getContext().getString(R.string.MSG_WARNING);
            }
        }
        return tContent;
    }


    /**
     * sd卡内容
     * 1489
     *
     * @param bean
     * @return
     */
    private String getFinalSdcardContent(CamMessageBean bean) {
        switch ((int) bean.message.getMsgId()) {
            case DpMsgMap.ID_222_SDCARD_SUMMARY: {
                DpMsgDefine.DPSdcardSummary sdcardSummary = (DpMsgDefine.DPSdcardSummary) bean.message;
                if (!sdcardSummary.hasSdcard) {
                    return getContext().getString(R.string.MSG_SD_OFF);
                }
                switch (sdcardSummary.errCode) {
                    case 0:
                        return getContext().getString(R.string.MSG_SD_ON);
                    default:
                        return getContext().getString(R.string.MSG_SD_ON_1);
                }
            }
        }
        return "";
    }

    @Override
    protected IMulItemViewType<CamMessageBean> offerMultiItemViewType() {
        return new IMulItemViewType<CamMessageBean>() {
            @Override
            public int getViewTypeCount() {
                return MAX_TYPE;
            }

            @Override
            public int getItemViewType(int position, CamMessageBean camMessageBean) {
                int count = 0;
                switch ((int) camMessageBean.message.getMsgId()) {
                    case DpMsgMap.ID_222_SDCARD_SUMMARY: {
                        return CamMessageBean.ViewType.TEXT;
                    }
                    case DpMsgMap.ID_401_BELL_CALL_STATE: {
                        DpMsgDefine.DPBellCallRecord dpBellCallRecord = (DpMsgDefine.DPBellCallRecord) camMessageBean.message;
                        count = dpBellCallRecord.fileIndex < 1 ? 1 : MiscUtils.getCount(dpBellCallRecord.fileIndex);
                    }
                    break;
                    case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                        DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) camMessageBean.message;
                        count = dpAlarm.fileIndex < 1 ? 1 : MiscUtils.getCount(dpAlarm.fileIndex);
                    }
                    break;
                    default: {
                        return camMessageBean.viewType;
                    }

                }
                if (count == 1) {
                    return CamMessageBean.ViewType.ONE_PIC;
                }
                if (count == 2) {
                    return CamMessageBean.ViewType.TWO_PIC;
                }
                if (count == 3) {
                    return CamMessageBean.ViewType.THREE_PIC;
                }
                return camMessageBean.viewType;
            }

            @Override
            public int getLayoutId(@CamMessageBean.ViewType int viewType) {
                switch (viewType) {
                    case CamMessageBean.ViewType.FOOT:
                        return R.layout.layout_item_cam_load_more;
                    case CamMessageBean.ViewType.TEXT:
                        return R.layout.layout_item_cam_msg_text;
                    case CamMessageBean.ViewType.ONE_PIC:
                        return R.layout.layout_item_cam_msg_1pic;
                    case CamMessageBean.ViewType.TWO_PIC:
                        return R.layout.layout_item_cam_msg_2pic;
                    case CamMessageBean.ViewType.THREE_PIC:
                        return R.layout.layout_item_cam_msg_3pic;
                    default:
                        return R.layout.layout_item_cam_load_more;
                }
            }
        };
    }

    public boolean hasFooter() {
        int count = getCount();
        return count > 0 && getItem(count - 1).viewType == CamMessageBean.ViewType.FOOT;
    }
//    private RequestListener<CamWarnGlideURL, GlideDrawable> loadListener = new RequestListener<CamWarnGlideURL, GlideDrawable>() {
//        @Override
//        public boolean onException(Exception e, CamWarnGlideURL model, Target<GlideDrawable> target, boolean isFirstResource) {
//            AppLogger.e(String.format(Locale.getDefault(), "uuid:%s,UriErr:%s,index:%s,e:%s", uuid, model.getTime(), model.getIndex(), MiscUtils.getErr(e)));
//            return false;
//        }
//
//        @Override
//        public boolean onResourceReady(GlideDrawable resource, CamWarnGlideURL model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//            return false;
//        }
//    };

    public void setCurrentSDcardSummary(DpMsgDefine.DPSdcardSummary summary) {
        this.summary = summary;
    }

//    private List<CamMessageBean>

//    public void filterByFaceItemType(String personId) {
//        // TODO: 2017/10/14 null 全部
//        this.faceItemType = personId;
//        notifyDataSetChanged();
//    }

    public void onStrangerInformationReady(List<FaceItem> visitorList) {

    }

    public void onVisitorInformationReady(List<FaceItem> visitorList) {
        if (visitorList != null) {
            for (FaceItem faceItem : visitorList) {
                DpMsgDefine.Visitor visitor = faceItem.getVisitor();
                if (visitor != null && visitor.detailList != null) {
                    for (DpMsgDefine.VisitorDetail detail : visitor.detailList) {
                        personMaps.put(detail.faceId, visitor.personName);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void appendVisitorList(String personId, ArrayList<CamMessageBean> beanArrayList) {
        if (beanArrayList == null) return;
        List<CamMessageBean> beans = visitorMaps.get(personId);
        if (beans != null) {
            beans.addAll(beanArrayList);
        } else {
            visitorMaps.put(personId, new ArrayList<>(beanArrayList));
        }
        addAll(beanArrayList);
    }

    public void insertVisitorList(String personId, ArrayList<CamMessageBean> beans) {
        if (beans == null) return;
        visitorMaps.remove(personId);
        visitorMaps.put(personId, new ArrayList<>(beans));
        clear();
        addAll(beans);
    }

    public boolean showCachedVisitorList(String personId) {
        List<CamMessageBean> list = visitorMaps.get(personId);
        clear();
        if (list != null) {
            addAll(list);
        }
        return list != null && !list.isEmpty();
    }

    public Map<String, List<CamMessageBean>> getCachedItems() {
        return visitorMaps;
    }

    public void restoreCachedItems(Map<String, List<CamMessageBean>> json) {
        this.visitorMaps.putAll(json);
    }
}
