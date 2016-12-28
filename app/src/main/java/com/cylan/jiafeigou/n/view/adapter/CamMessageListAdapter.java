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
import com.cylan.jiafeigou.cache.pool.GlobalDataPool;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;
import com.cylan.utils.DensityUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-7-13.
 */
public class CamMessageListAdapter extends SuperAdapter<CamMessageBean> {

    /**
     * 一张图片，两张图片，三张图片，只有文字。
     */
    private static final int MAX_TYPE = 4;
    private String uuid;
    /**
     * 0： 正常，1:编辑
     */
    private boolean editMode;
    //图片Container的总体宽度,可能有3条,可能有2条.
    private final int pic_container_width;
    private final int pic_container_height;
    private Map<Integer, Integer> selectedMap = new HashMap<>();
    private Map<Integer, Integer> loadFailedMap = new HashMap<>();
    private boolean hasStatus;
    private boolean deviceOnlineState;

    public CamMessageListAdapter(String uiid, Context context, List<CamMessageBean> items, IMulItemViewType<CamMessageBean> mulItemViewType) {
        super(context, items, mulItemViewType);
        //这个40是根据layout中的marginStart,marginEnd距离提取出来,如果需要修改,参考这个layout
        pic_container_width = Resources.getSystem().getDisplayMetrics().widthPixels - DensityUtils.dip2px(40);
        pic_container_height = DensityUtils.dip2px(225 - 48 - 36 - 5);
        this.uuid = uiid;
        fetchSdcardStatus();
    }

    private void fetchSdcardStatus() {
        DpMsgDefine.SdStatus status = GlobalDataPool.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE);
        this.hasStatus |= status != null && status.hasSdcard;
        DpMsgDefine.MsgNet net = GlobalDataPool.getInstance().getValue(this.uuid, DpMsgMap.ID_201_NET);
        deviceOnlineState = net != null && net.net != 0;
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
        updateItemFrom(lastPosition);
    }

    /**
     * 收集已经选中的
     *
     * @return
     */
    public ArrayList<CamMessageBean> getSelectedItems() {
        ArrayList<CamMessageBean> list = new ArrayList<>();
        for (int index : selectedMap.keySet()) {
            list.add(getList().get(index));
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
        }
        if (onClickListener != null)
            holder.setOnClickListener(R.id.lLayout_cam_msg_container, onClickListener);
        if (onClickListener != null)
            holder.setOnClickListener(R.id.tv_cam_message_item_delete, onClickListener);
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
        return System.currentTimeMillis() - time >= 30 * 60 * 1000L && this.hasStatus;
    }

    /**
     * 来自一个全局的通知消息
     */
    public void notifySdcardStatus(boolean status, int position) {
        this.hasStatus = status;
        updateItemFrom(position);
    }

    public void notifyDeviceOnlineState(boolean online, int position) {
        this.deviceOnlineState = online;
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
        holder.setText(R.id.tv_cam_message_item_date, getFinalTimeContent(item));
        holder.setText(R.id.tv_cam_message_list_content, getFinalSdcardContent(item));
    }

    private void handlePicsLayout(SuperViewHolder holder,
                                  CamMessageBean item) {
//        if (!isEditMode()) {
        final int count = item.urlList.size();
        //根据图片总数,设置view的Gone属性
        for (int i = 2; i >= 0; i--) {
            holder.setVisibility(R.id.imgV_cam_message_pic_0 + i,
                    count - 1 >= i ? View.VISIBLE : View.GONE);
        }
        for (int i = 0; i < item.urlList.size(); i++) {
            Glide.with(getContext())
                    .load(item.urlList.get(i))
                    .placeholder(R.drawable.wonderful_pic_place_holder)
                    .override(pic_container_width / count, pic_container_height)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .listener(loadListener)
                    .into((ImageView) holder.getView(R.id.imgV_cam_message_pic_0 + i));
        }
//        }
        holder.setText(R.id.tv_cam_message_item_date, getFinalTimeContent(item));
        holder.setVisibility(R.id.tv_to_live, showLiveBtn(item.time) ? View.VISIBLE : View.INVISIBLE);
        holder.setOnClickListener(R.id.tv_to_live, onClickListener);
        holder.setEnabled(R.id.tv_to_live, deviceOnlineState);
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
        String tContent = TimeUtils.getHH_MM(bean.time);
        if (id == DpMsgMap.ID_505_CAMERA_ALARM_MSG) {
            return tContent + " 有新的发现";
        }
        return tContent;
    }

    /**
     * sd卡内容
     *
     * @param bean
     * @return
     */
    private String getFinalSdcardContent(CamMessageBean bean) {
        if (bean.id != DpMsgMap.ID_204_SDCARD_STORAGE || bean.content == null)
            return "";
        DpMsgDefine.SdcardSummary sdStatus = bean.content;
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
                return camMessageBean.viewType;
            }

            @Override
            public int getLayoutId(int viewType) {
                switch (viewType) {
                    case 0:
                        return R.layout.layout_item_cam_msg_list_0;
                    case 1:
                        return R.layout.layout_item_cam_msg_list_1;
                    default:
                        return R.layout.layout_wonderful_empty;
                }
            }
        };
    }

    private RequestListener<String, GlideDrawable> loadListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e,
                                   String model,
                                   Target<GlideDrawable> target,
                                   boolean isFirstResource) {
            int position = getPositionByModel(model);
            loadFailedMap.put(position, position);//标记load失败的position
            Log.d("onException", "onException: " + position);
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            int position = getPositionByModel(model);
            loadFailedMap.remove(position);
            Log.d("onResourceReady", "onResourceReady: " + position);
            return false;
        }
    };
    private static final String TAG_BLANK = "";
    private static final String TAG0 = ".jpg?";
    private static final String TAG1 = "_1";
    private static final String TAG2 = "_2";
    private static final String TAG3 = "_3";

    private int getPositionByModel(String model) {
        try {
            int index0 = model.indexOf(TAG0);
            int index1 = model.indexOf(this.uuid);
            String time = model.substring(index1, index0)
                    .replace(uuid + File.separator, "")
                    .replace(TAG1, TAG_BLANK)
                    .replace(TAG2, TAG_BLANK)
                    .replace(TAG3, TAG_BLANK);
            if (TextUtils.isDigitsOnly(time)) {
                long finalTime = Long.parseLong(time);
                CamMessageBean bean = new CamMessageBean();
                bean.id = DpMsgMap.ID_505_CAMERA_ALARM_MSG;
                bean.version = finalTime * 1000L;
                bean.time = finalTime * 1000L;
                return getList().indexOf(bean);
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }
}
