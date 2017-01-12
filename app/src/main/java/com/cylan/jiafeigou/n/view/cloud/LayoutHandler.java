package com.cylan.jiafeigou.n.view.cloud;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveCallOutBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveLeaveMesBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveCallInBean;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.superadapter.internal.SuperViewHolder;

/**
 * 作者：zsl
 * 创建时间：2016/10/8
 * 描述：
 */
public class LayoutHandler {

    private static final int LEAVE_MESG_TYPE = 0;
    private static final int VIDEO_CALL_OUT_TYPE = 2;
    private static final int VIDEO_CALL_IN_TYPE = 1;

    private ViewTypeMapCache viewTypeCache;
    private LayoutIdMapCache layoutIdMapCache;

    public void setViewTypeCache(ViewTypeMapCache viewTypeCache) {
        this.viewTypeCache = viewTypeCache;
    }

    public void setLayoutIdMapCache(LayoutIdMapCache layoutIdMapCache) {
        this.layoutIdMapCache = layoutIdMapCache;
    }

    public void handleLayout(SuperViewHolder holder, int viewType, int layoutPosition, CloudLiveBaseBean items) {
        Object o = items.data;
        LinearLayout.LayoutParams item = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (layoutPosition != 0) {
            item.setMargins(0, ViewUtils.dp2px(30), 0, 0);
            holder.itemView.setLayoutParams(item);
        } else {
            item.setMargins(0, ViewUtils.dp2px(11), 0, 0);
            holder.itemView.setLayoutParams(item);
        }

        switch (viewType) {
            case LEAVE_MESG_TYPE:
                CloudLiveLeaveMesBean cc = (CloudLiveLeaveMesBean) o;
                holder.setText(R.id.tv_voice_length, cc.getLeaveMesgLength());
                holder.setText(R.id.tv_time, cc.getLeveMesgTime());
                if (cc.isRead()) {
                    holder.setVisibility(R.id.tv_is_read, View.VISIBLE);
                } else {
                    holder.setVisibility(R.id.tv_is_read, View.INVISIBLE);
                }
                break;

            case VIDEO_CALL_OUT_TYPE:
                CloudLiveCallOutBean callOutvideoBean = (CloudLiveCallOutBean) o;
                holder.setText(R.id.tv_time, callOutvideoBean.getVideoTime());

                if (callOutvideoBean.isHasConnet()) {
                    holder.setText(R.id.tv_voideo_talk_length, String.format(ContextUtils.getContext().getString(R.string.Tap1_iHome_CallDuration), callOutvideoBean.getVideoLength()));
                    holder.setImageDrawable(R.id.iv_call_out_icon,ContextUtils.getContext().getResources().getDrawable(R.drawable.icon_call));
                } else {
                    holder.setText(R.id.tv_voideo_talk_length, "未接通");
                    holder.setImageDrawable(R.id.iv_call_out_icon,ContextUtils.getContext().getResources().getDrawable(R.drawable.icon_missed_call));
                }
                break;

            case VIDEO_CALL_IN_TYPE:
                CloudLiveCallInBean callInvideoBean = (CloudLiveCallInBean) o;
                holder.setText(R.id.tv_call_in_time, callInvideoBean.getVideoTime());

                if (callInvideoBean.isHasConnet()) {
                    holder.setText(R.id.tv_call_in_talk_length, String.format(ContextUtils.getContext().getString(R.string.Tap1_iHome_CallDuration), callInvideoBean.getVideoLength()));
                    holder.setImageDrawable(R.id.iv_call_in_icon,ContextUtils.getContext().getResources().getDrawable(R.drawable.icon_incoming_call));
                } else {
                    holder.setText(R.id.tv_call_in_talk_length, ContextUtils.getContext().getString(R.string.EFAMILY_MISSED_CALL));
                    holder.setImageDrawable(R.id.iv_call_in_icon,ContextUtils.getContext().getResources().getDrawable(R.drawable.icon_missed_call));
                }

                break;
        }

    }
}
