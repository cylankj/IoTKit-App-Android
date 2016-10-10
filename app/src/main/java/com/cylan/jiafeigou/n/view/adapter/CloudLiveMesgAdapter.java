package com.cylan.jiafeigou.n.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveMesgBean;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/30
 * 描述：
 */
public class CloudLiveMesgAdapter extends RecyclerView.Adapter<CloudLiveMesgAdapter.MesgHolder> {

    private static final int VOICE_TYPE = 0;
    private static final int VIDEO_TALK_TYPE= 1;
    private static final int SHARE_PIC_TYPE = 2;

    private List<CloudLiveMesgBean> mData;

    public CloudLiveMesgAdapter(List<CloudLiveMesgBean> data) {
        this.mData = data;
    }

    @Override
    public MesgHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType){
            case VOICE_TYPE:
                view = View.inflate(parent.getContext(), R.layout.activity_cloud_live_mesg_voice_item,null);
                break;

            case VIDEO_TALK_TYPE:
                view = View.inflate(parent.getContext(), R.layout.activity_cloud_live_mesg_video_talk_item,null);
                break;

            case SHARE_PIC_TYPE:
                view = View.inflate(parent.getContext(), R.layout.activity_cloud_live_mesg_voice_item,null);
                break;
        }
        return new MesgHolder(view,viewType);
    }

    @Override
    public void onBindViewHolder(MesgHolder holder, int position) {
        LinearLayout.LayoutParams item = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        if(position != 0){
            item.setMargins(0, ViewUtils.dp2px(30),0,0);
            holder.itemView.setLayoutParams(item);
        }else {
            item.setMargins(0,ViewUtils.dp2px(11),0,0);
            holder.itemView.setLayoutParams(item);
        }

        CloudLiveMesgBean cloudLiveMesgBean = mData.get(position);

        switch (cloudLiveMesgBean.itemType){
            case VOICE_TYPE:
                holder.tv_voice_length.setText(cloudLiveMesgBean.voiceLength);
                break;
            case VIDEO_TALK_TYPE:
                holder.tv_video_talk_length.setText("未接听，点击回拨");
                break;
        }

    }

    @Override
    public int getItemCount() {
        if (mData != null){
            return mData.size();
        }else {
            return 0;
        }
    }

    class MesgHolder extends RecyclerView.ViewHolder{

        private TextView tv_voice_length ;
        private ImageView iv_user_icon ;
        private TextView tv_video_talk_length;

        public MesgHolder(View itemView,int viewType) {
            super(itemView);
            switch (viewType){
                case VOICE_TYPE:
                    tv_voice_length = (TextView) itemView.findViewById(R.id.tv_voice_length);
                    iv_user_icon = (ImageView) itemView.findViewById(R.id.iv_user_icon);
                    break;

                case VIDEO_TALK_TYPE:
                    tv_video_talk_length = (TextView) itemView.findViewById(R.id.tv_voideo_talk_length);
                    break;

                case SHARE_PIC_TYPE:

                    break;

            }

        }
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).itemType;
    }

}
