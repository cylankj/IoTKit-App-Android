package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.MineMessageBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HomeMineMessageAdapter extends SuperAdapter<MineMessageBean>{

    public boolean isShowCheck;
    public boolean checkAll;

    public OnDeleteCheckChangeListener listener;

    public interface OnDeleteCheckChangeListener{
        public void deleteCheck(boolean isCheck,MineMessageBean item);
    }

    public void setOnDeleteCheckChangeListener(OnDeleteCheckChangeListener listener){
        this.listener = listener;
    }

    public HomeMineMessageAdapter(Context context, List<MineMessageBean> items, IMulItemViewType<MineMessageBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, MineMessageBean item) {
        //处理消息时间
        holder.setText(R.id.message_item_time,parseTime(item.getTime()));
        if (layoutPosition == 0 | compareTime(getItem(getItemCount() - 1).getTime(), item.getTime())) {
            holder.setVisibility(R.id.message_item_time,View.VISIBLE);
        } else {
            holder.setVisibility(R.id.message_item_time,View.GONE);
        }

        if (isShowCheck){
            holder.setVisibility(R.id.delete_check,View.VISIBLE);
        }else {
            holder.setVisibility(R.id.delete_check,View.GONE);
        }

        CheckBox deleteCheck = holder.getView(R.id.delete_check);
        deleteCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listener != null){
                    listener.deleteCheck(isChecked,item);
                }
            }
        });

        if (checkAll){
            deleteCheck.setChecked(true);
        }else {
            deleteCheck.setChecked(false);
        }

        //处理消息显示
        holder.setText(R.id.message_item_msg,item.getContent());

        //TODO
    }

    @Override
    protected IMulItemViewType<MineMessageBean> offerMultiItemViewType() {
        return new IMulItemViewType<MineMessageBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, MineMessageBean mineMessageBean) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_home_mine_message_items;
            }
        };
    }

    public String parseTime(String times) {
        long timem = Long.parseLong(times);
        Date time = new Date(timem);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(time);
        return dateString;
    }

    public boolean compareTime(String preStrTime, String nowStrTime) {
        long preTime = Long.parseLong(preStrTime);
        long nowTime = Long.parseLong(nowStrTime);
        return (nowTime - preTime >= 20000) ? true : false;
    }
}
