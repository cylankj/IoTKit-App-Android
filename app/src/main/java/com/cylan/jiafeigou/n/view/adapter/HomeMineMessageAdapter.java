package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.MineMessageBean;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class HomeMineMessageAdapter extends SuperAdapter<MineMessageBean> {

    public boolean isShowCheck;
    public boolean checkAll;

    public OnDeleteCheckChangeListener listener;

    public interface OnDeleteCheckChangeListener {
        public void deleteCheck(boolean isCheck, MineMessageBean item);
    }

    public void setOnDeleteCheckChangeListener(OnDeleteCheckChangeListener listener) {
        this.listener = listener;
    }

    public HomeMineMessageAdapter(Context context, List<MineMessageBean> items, IMulItemViewType<MineMessageBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, MineMessageBean item) {
        //处理消息时间
        holder.setText(R.id.item_time, parseTime(item.getTime()));

        if (isShowCheck) {
            holder.setVisibility(R.id.delete_check, View.VISIBLE);
        } else {
            holder.setVisibility(R.id.delete_check, View.GONE);
        }

        CheckBox deleteCheck = holder.getView(R.id.delete_check);
        deleteCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listener != null) {
                    listener.deleteCheck(isChecked, item);
                }
            }
        });

        if (checkAll) {
            deleteCheck.setChecked(true);
        } else {
            deleteCheck.setChecked(false);
        }

        //处理消息显示
        holder.setText(R.id.tv_device_name,item.getContent());

        if (item.isDone == 1){
            switch (item.type){
                case 601:
                    holder.setText(R.id.mesg_item_content,String.format(ContextUtils.getContext().getString(R.string.MSG_REBIND), item.getName()));
                    break;
                case 603:
                    holder.setText(R.id.mesg_item_content,"该设备已分享");
                    break;
                case 604:
                    holder.setText(R.id.mesg_item_content,"亲友分享了改设备");
                    break;
            }
        }else {
            switch (item.type){
                case 601:
                    holder.setText(R.id.mesg_item_content, "该设备已被解绑");
                    break;
                case 603:
                    holder.setText(R.id.mesg_item_content,ContextUtils.getContext().getString(R.string.Tap1_shareDevice_canceledshare));
                    break;
                case 604:
                    holder.setText(R.id.mesg_item_content,"亲友取消了分享该设备");
                    break;
            }
        }

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
//                return R.layout.fragment_mine_message_system_items;
                return R.layout.fragment_mine_message_items;
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
