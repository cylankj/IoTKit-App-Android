package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
    private static final int MAX_TYPE = 3;
    private String uuid;
    /**
     * 0： 正常，1:编辑
     */
    private boolean editMode;
    //图片Container的总体宽度,可能有3条,可能有2条.
    private final int pic_container_width;//宽度是固定的，需要调整高度。
    private Map<Integer, Integer> selectedMap = new HashMap<>();

    private boolean isSharedDevice = false;


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
        return status.hasSdcard && status.err == 0;
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
        if (!editMode) selectedMap.clear();
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
                    notifyItemChanged(i);
            }
        }
    }

    public void markItemSelected(int position) {
        if (!editMode)
            return;
        if (selectedMap.containsKey(position)) {
            selectedMap.remove(position);
        } else {
            selectedMap.put(position, position);
        }
        notifyItemChanged(position);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, CamMessageBean item) {
        switch (viewType) {
            case 0:
                handleTextContentLayout(holder, item);
                break;
            case 1:
                handlePicsLayout(holder, item);
                break;
//            case 3:
//                handlerBellLayout(holder, item);
//            break;
            default:
                return;
        }
        if (onClickListener != null)
            holder.setOnClickListener(R.id.lLayout_cam_msg_container, onClickListener);
        if (onClickListener != null)
            holder.setOnClickListener(R.id.tv_cam_message_item_delete, onClickListener);
        //设置删除可见性,共享设备不可删除消息
        if (isSharedDevice) {
            holder.setVisibility(R.id.tv_cam_message_item_delete, View.INVISIBLE);
        }

        holder.setVisibility(R.id.fl_item_time_line, isEditMode() ? View.INVISIBLE : View.VISIBLE);
        holder.setVisibility(R.id.rbtn_item_check, isEditMode() ? View.VISIBLE : View.INVISIBLE);
        holder.setVisibility(R.id.fLayout_cam_message_item_bottom, !isEditMode() ? View.VISIBLE : View.INVISIBLE);
        if (isEditMode())
            holder.setChecked(R.id.rbtn_item_check, selectedMap.containsKey(layoutPosition));
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

    private boolean textShowSdBtn(CamMessageBean item) {
        //考虑这个bean的条件.
        if (item.sdcardSummary != null) {
            if (!item.sdcardSummary.hasSdcard) return false;
        }
        DpMsgDefine.DPSdStatus status = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid).$(204, new DpMsgDefine.DPSdStatus());
        return status.hasSdcard && status.err != 0 && !isSharedDevice;
    }

    /**
     * 来自一个全局的通知消息
     */
    public void notifySdcardStatus(boolean status, int position) {
        DpMsgDefine.DPSdStatus nowStatus = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid).$(204, new DpMsgDefine.DPSdStatus());
        updateItemFrom(position);
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
        if (onClickListener != null)
            holder.setOnClickListener(R.id.tv_jump_next, onClickListener);
    }

    private void handlePicsLayout(SuperViewHolder holder,
                                  CamMessageBean item) {
//        if (!isEditMode()) {
        int count = 0;
        if (item.alarmMsg != null) {
            count = MiscUtils.getCount(item.alarmMsg.fileIndex);
        } else if (item.bellCallRecord != null && item.bellCallRecord.fileIndex != -1) {
            count = MiscUtils.getCount(item.bellCallRecord.fileIndex);
        }
        count = Math.max(count, 1);//最小为1
        ViewGroup.LayoutParams containerLp = holder.getView(R.id.lLayout_cam_msg_container).getLayoutParams();
        containerLp.height = getLayoutHeight(count);
        holder.getView(R.id.lLayout_cam_msg_container).setLayoutParams(containerLp);
        //根据图片总数,设置view的Gone属性
        for (int i = 2; i >= 0; i--) {
            View child = holder.getView(R.id.imgV_cam_message_pic_0 + i);
            child.setVisibility(count - 1 >= i ? View.VISIBLE : View.GONE);
            if (count - 1 >= i) {
                ViewGroup.LayoutParams lp = child.getLayoutParams();
                lp.width = getPicWidth(count);
                lp.height = getPicHeight(count);
                child.setLayoutParams(lp);
            }
        }
        for (int index = 1; index <= count; index++) {
            Glide.with(getContext())
                    .load(MiscUtils.getCamWarnUrl(uuid, item, index))
                    .placeholder(R.drawable.wonderful_pic_place_holder)
                    .override(pic_container_width / count, pic_container_width / count)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .listener(loadListener)
                    .into((ImageView) holder.getView(R.id.imgV_cam_message_pic_0 + index - 1));
        }


//        }
        holder.setText(R.id.tv_cam_message_item_date, getFinalTimeContent(item));
        Log.d(TAG, "handlePicsLayout: " + (System.currentTimeMillis() - item.version));
        holder.setVisibility(R.id.tv_jump_next, showLiveBtn(item.version) ? View.VISIBLE : View.GONE);
        holder.setOnClickListener(R.id.tv_jump_next, onClickListener);
        holder.setOnClickListener(R.id.imgV_cam_message_pic_0, onClickListener);
        holder.setOnClickListener(R.id.imgV_cam_message_pic_1, onClickListener);
        holder.setOnClickListener(R.id.imgV_cam_message_pic_2, onClickListener);
//        holder.setEnabled(R.id.tv_jump_next, online());
    }


    private int getLayoutHeight(int count) {
        float static_height = getDimens(R.dimen.x88);
        return (int) (static_height + (count == 1 ? getDimens(R.dimen.y120)
                : (count == 2 ? getDimens(R.dimen.x153)
                : getDimens(R.dimen.x100))));
    }

    private int getPicWidth(int count) {
        return count == 1 ? getDimens(R.dimen.x161)
                : (count == 2 ? getDimens(R.dimen.x153)
                : getDimens(R.dimen.x100));
    }

    private int getPicHeight(int count) {
        return count == 1 ? getDimens(R.dimen.x120)
                : (count == 2 ? getDimens(R.dimen.x153)
                : getDimens(R.dimen.x100));
    }

    private int getDimens(int id) {
        return (int) getContext().getResources().getDimension(id);
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
            return tContent + getContext().getString(R.string.MSG_WARNING);
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
        if (bean.id != DpMsgMap.ID_222_SDCARD_SUMMARY || bean.sdcardSummary == null)
            return "";
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
                if (camMessageBean.viewType == 2) return 2;
//                return camMessageBean.alarmMsg != null && MiscUtils.getCount(camMessageBean.alarmMsg.fileIndex) > 0 && camMessageBean.sdcardSummary == null ? 1 : camMessageBean.bellCallRecord == null ? 0 : 3;
                if (camMessageBean.bellCallRecord != null) return 1;
                return camMessageBean.alarmMsg != null && MiscUtils.getCount(camMessageBean.alarmMsg.fileIndex) > 0 && camMessageBean.sdcardSummary == null ? 1 : 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                switch (viewType) {
                    case 0:
                        return R.layout.layout_item_cam_msg_list_0;
                    case 1:
                        return R.layout.layout_item_cam_msg_list_1;
                    case 3:
                        return R.layout.layout_item_cam_msg_list_1;
                    default:
                        return R.layout.simple_load_more_layout;
                }
            }
        };
    }

    public boolean hasFooter() {
        int count = getCount();
        return count > 0 && getItem(count - 1).viewType == 2;
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
}
