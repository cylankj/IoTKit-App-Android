package com.cylan.jiafeigou.n.view.cloud;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveLeaveMesBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveVideoTalkBean;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.superadapter.internal.SuperViewHolder;
import com.sina.weibo.sdk.utils.LogUtil;

/**
 * 作者：zsl
 * 创建时间：2016/10/8
 * 描述：
 */
public class LayoutHandler {

    private static final int LEAVE_MESG_TYPE = 0;
    private static final int VIDEO_TALK_TYPE = 1;

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
        LinearLayout.LayoutParams item = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        if(layoutPosition != 0){
            item.setMargins(0, ViewUtils.dp2px(30),0,0);
            holder.itemView.setLayoutParams(item);
        }else {
            item.setMargins(0,ViewUtils.dp2px(11),0,0);
            holder.itemView.setLayoutParams(item);
        }

        switch (viewType){
            case LEAVE_MESG_TYPE:
                CloudLiveLeaveMesBean cc = (CloudLiveLeaveMesBean) o;
                holder.setText(R.id.tv_voice_length,cc.getLeaveMesgLength());
                holder.setText(R.id.tv_time,cc.getLeveMesgTime());
                if(cc.isRead()){
                    holder.setVisibility(R.id.tv_is_read, View.VISIBLE);
                }else {
                    holder.setVisibility(R.id.tv_is_read, View.INVISIBLE);
                }
                break;

            case VIDEO_TALK_TYPE:
                CloudLiveVideoTalkBean videoBean = (CloudLiveVideoTalkBean) o;
                holder.setText(R.id.tv_time,videoBean.getVideoTime());

                if (videoBean.isHasConnet()){
                    holder.setText(R.id.tv_voideo_talk_length,videoBean.getVideoLength());
                }else {
                    holder.setText(R.id.tv_voideo_talk_length,"未接听，点击回拨");
                }
                break;
        }

    }
}
