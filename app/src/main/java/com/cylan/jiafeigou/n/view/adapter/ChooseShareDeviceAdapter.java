package com.cylan.jiafeigou.n.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.MineShareDeviceBean;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/24
 * 描述：
 */
public class ChooseShareDeviceAdapter extends RecyclerView.Adapter<ChooseShareDeviceAdapter.ChooseDeviceHolder> {

    private ArrayList<MineShareDeviceBean> mData;

    public ChooseShareDeviceAdapter(ArrayList<MineShareDeviceBean> data) {
        this.mData = data;
    }

    @Override
    public ChooseDeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.fragment_relative_friend_share_device_items,null);
        return new ChooseDeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(ChooseDeviceHolder holder, int position) {
        MineShareDeviceBean mineShareDeviceBean = mData.get(position);
        holder.tv_share_number.setText(mineShareDeviceBean.getShareNumber()+"/5");
        holder.tv_device_name.setText(mineShareDeviceBean.getDeviceName());
        holder.iv_isCheck.setImageDrawable(holder.itemView.getResources().getDrawable(mineShareDeviceBean.isCheck() == true ? R.drawable.icon_selected:R.drawable.icon_not_selected));

        switch (mineShareDeviceBean.getDeviceName()){
            case "智能摄像头":
                holder.iv_device_icon.setImageDrawable(holder.itemView.getResources().getDrawable(R.drawable.icon_home_camera_online));
                break;
            case "智能门铃":
                holder.iv_device_icon.setImageDrawable(holder.itemView.getResources().getDrawable(R.drawable.icon_home_doorbell_online));
                break;
            case "云相框":
                holder.iv_device_icon.setImageDrawable(holder.itemView.getResources().getDrawable(R.drawable.icon_home_album_online));
                break;
        }


    }

    @Override
    public int getItemCount() {
        if(mData != null){
            return mData.size();
        }else {
            return 0;
        }
    }

    class ChooseDeviceHolder extends RecyclerView.ViewHolder{
        private final ImageView iv_device_icon;
        private final TextView tv_device_name;
        private final TextView tv_share_number;
        private final ImageView iv_isCheck;

        public ChooseDeviceHolder(View itemView) {
            super(itemView);
            iv_device_icon =  (ImageView) itemView.findViewById(R.id.iv_device_icon);
            tv_device_name =  (TextView) itemView.findViewById(R.id.tv_device_name);
            tv_share_number = (TextView) itemView.findViewById(R.id.tv_share_device_number);
            iv_isCheck = (ImageView) itemView.findViewById(R.id.iv_share_isCheck);
        }
    }
}

