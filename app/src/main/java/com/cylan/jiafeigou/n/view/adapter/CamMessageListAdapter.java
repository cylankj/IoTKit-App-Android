package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.CamWarnGlideURL;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private Map<String, DpMsgDefine.FaceInformation> faceInformationMap = new HashMap<>();
    //null 不过滤
    private String faceItemType = null;


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
            sameDay = TimeUtils.isSameDay(bean.version, item.version);
        }
        boolean showDivider = !sameDay && faceFragment;
        holder.setVisibility(R.id.message_time_divider, showDivider ? View.VISIBLE : View.GONE);
        holder.setVisibility(R.id.watcher_text, showDivider ? View.VISIBLE : View.GONE);
        if (showDivider) {
            String content = TimeUtils.getTimeSpecial(item.version);
            holder.setText(R.id.time_divider, content);
            // TODO: 2017/10/18 显示右边的人数统计,现在不知道从哪获取数据

            holder.setText(R.id.watcher_text, "最近30天来访15次");
        }

        //设置删除可见性,共享设备不可删除消息
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        //720 设备享有所有权限
        holder.setVisibility(R.id.tv_cam_message_item_delete, !isSharedDevice || JFGRules.isPan720(device.pid) ? View.VISIBLE : View.INVISIBLE);
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
        if (isSharedDevice || !hasSdcard()) {
            return false;
        }
        if (bean != null && bean.bellCallRecord != null) {
            return bean.bellCallRecord.isRecording == 1;
        } else if (bean != null && bean.alarmMsg != null) {
            Device device = DataSourceManager.getInstance().getDevice(uuid);
            boolean pan720 = JFGRules.isPan720(device.pid);
            if (pan720) {
//                return bean.alarmMsg.isRecording == 1;//全部当成图片处理,
                // TODO: 2017/8/4 当前查看视频不知道怎么处理
                return false;
            } else {
                return bean.alarmMsg.isRecording == 1 && (System.currentTimeMillis() - bean.alarmMsg.version) >= 30 * 60 * 1000L;
            }
        }
        return false;
    }

    private boolean textShowSdBtn(CamMessageBean item) {
        //考虑这个bean的条件.
        // TODO: 2017/8/16  不光要看 hasSDCard 还要看 err 是否为0 #118051
        // TODO: 2017/8/16 Android（1.1.0.534）720设备 报警中心界面 提示"检车到新的Micro SD卡，需要先初始化才能存储视频" 右下角没有查看详情 按钮
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());
        boolean hasSdcard = false;
        int err = -1;
        if (item.sdcardSummary != null) {
            hasSdcard = item.sdcardSummary.hasSdcard;
            err = item.sdcardSummary.errCode;
        } else if (status != null) {
            hasSdcard = status.hasSdcard;
            err = status.err;
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
        if (item.alarmMsg != null) {
            count = item.alarmMsg.fileIndex < 1 ? 1 : MiscUtils.getCount(item.alarmMsg.fileIndex);
        } else if (item.bellCallRecord != null && item.bellCallRecord.fileIndex != -1) {
            count = item.bellCallRecord.fileIndex < 1 ? 1 : MiscUtils.getCount(item.bellCallRecord.fileIndex);
        }
        count = Math.max(count, 1);//最小为1
        for (int index = 1; index <= count; index++) {
            int id = index == 1 ? R.id.imgV_cam_message_pic0
                    : index == 2 ? R.id.imgV_cam_message_pic1 :
                    R.id.imgV_cam_message_pic2;
            Glide.with(getContext())
                    .load(MiscUtils.getCamWarnUrl(uuid, item, index))
                    .placeholder(R.drawable.wonderful_pic_place_holder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .listener(loadListener)
                    .into((ImageView) holder.getView(id));
            holder.setOnClickListener(id, onClickListener);
        }
        holder.setText(R.id.tv_cam_message_item_date, getFinalTimeContent(item));
        Log.d(TAG, "handlePicsLayout: " + (System.currentTimeMillis() - item.version));
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
        long id = bean.id;
        String tContent = TimeUtils.getHH_MM(bean.version) + " ";
        if (id == DpMsgMap.ID_505_CAMERA_ALARM_MSG) {
            /*现在人形检测也是用的这个消息,增加了扩展字段,有人形的提示和无人形的提示有区别
            * 1.有人形提示:检测到 XXX
            * 2.无人形提示:有新的发现
            * */
            if (bean.alarmMsg.face_id != null && bean.alarmMsg.humanNum > 0) {
                String faceText = JConstant.getFaceText(bean.alarmMsg.face_id, faceInformationMap, null);
                return tContent + (TextUtils.isEmpty(faceText) ? getContext().getString(R.string.MSG_WARNING) : getContext().getString(R.string.DETECTED_AI) + " " + faceText);
            } else if (bean.alarmMsg.objects != null && bean.alarmMsg.objects.length > 0) {//有检测数据
                return tContent + getContext().getString(R.string.DETECTED_AI) + " " + JConstant.getAIText(bean.alarmMsg.objects);
//                return tContent + "检测到" + JConstant.getAIText(bean.alarmMsg.objects);
            } else {//无检测数据
                return tContent + getContext().getString(R.string.MSG_WARNING);
            }

        } else if (id == DpMsgMap.ID_401_BELL_CALL_STATE) {
            return tContent + (bean.bellCallRecord.isOK == 1 ? getContext().getString(R.string.DOOR_CALL) : getContext().getString(R.string.DOOR_UNCALL));
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
        long id = bean.id;
        String tContent = TimeUtils.getHH_MM(bean.version) + " ";
        if (id == DpMsgMap.ID_505_CAMERA_ALARM_MSG || id == DpMsgMap.ID_401_BELL_CALL_STATE) {
            return tContent + getContext().getString(R.string.MSG_WARNING);
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
        if (bean.id != DpMsgMap.ID_222_SDCARD_SUMMARY || bean.sdcardSummary == null) {
            return "";
        }
        DpMsgDefine.DPSdcardSummary sdStatus = bean.sdcardSummary;
        if (!sdStatus.hasSdcard) {
            return getContext().getString(R.string.MSG_SD_OFF);
        }
        switch (sdStatus.errCode) {
            case 0:
                return getContext().getString(R.string.MSG_SD_ON);
            default:
                return getContext().getString(R.string.MSG_SD_ON_1);
        }

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
                if (camMessageBean.bellCallRecord != null) {
                    final int count = camMessageBean.bellCallRecord.fileIndex < 1 ? 1 : MiscUtils.getCount(camMessageBean.bellCallRecord.fileIndex);
                    if (count == 1) {
                        return CamMessageBean.ViewType.ONE_PIC;
                    }
                    if (count == 2) {
                        return CamMessageBean.ViewType.TWO_PIC;
                    }
                    if (count == 3) {
                        return CamMessageBean.ViewType.THREE_PIC;
                    }
                }
                if (camMessageBean.alarmMsg != null) {
                    final int count = camMessageBean.alarmMsg.fileIndex < 1 ? 1 : MiscUtils.getCount(camMessageBean.alarmMsg.fileIndex);
                    if (count == 1) {
                        return CamMessageBean.ViewType.ONE_PIC;
                    }
                    if (count == 2) {
                        return CamMessageBean.ViewType.TWO_PIC;
                    }
                    if (count == 3) {
                        return CamMessageBean.ViewType.THREE_PIC;
                    }
                }
                if (camMessageBean.sdcardSummary != null) {
                    return CamMessageBean.ViewType.TEXT;
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

    private RequestListener<CamWarnGlideURL, GlideDrawable> loadListener = new RequestListener<CamWarnGlideURL, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, CamWarnGlideURL model, Target<GlideDrawable> target, boolean isFirstResource) {
            AppLogger.e(String.format(Locale.getDefault(), "uuid:%s,UriErr:%s,index:%s,e:%s", uuid, model.getTime(), model.getIndex(), MiscUtils.getErr(e)));
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, CamWarnGlideURL model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    };

    public void setCurrentSDcardSummary(DpMsgDefine.DPSdcardSummary summary) {
        this.summary = summary;
    }

    public void appendFaceInformation(Map<String, DpMsgDefine.FaceInformation> informationMap) {
        faceInformationMap.putAll(informationMap);
        notifyDataSetChanged();
    }

//    private List<CamMessageBean>

    public void filterByFaceItemType(String personId) {
        // TODO: 2017/10/14 null 全部
        this.faceItemType = personId;
        notifyDataSetChanged();
    }
}
