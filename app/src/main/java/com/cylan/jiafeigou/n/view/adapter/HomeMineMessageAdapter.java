package com.cylan.jiafeigou.n.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class HomeMineMessageAdapter extends RecyclerView.Adapter<HomeMineMessageAdapter.ChatViewHolder> {
    private ArrayList<SuggestionChatInfoBean> messages;
    public HomeMineMessageAdapter(ArrayList<SuggestionChatInfoBean> messages){
        this.messages = messages;
    }
    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType==0){
            //发送的消息布局
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_home_mine_message_items,parent,false);
        }else {
            //接收的消息布局
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_home_mine_message_items,parent,false);
        }
        return new ChatViewHolder(view);
    }
    //发送消息  0  接收receive  1
    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getType() == 0){
            return 0;
        }else {
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        SuggestionChatInfoBean message = messages.get(position);
        //处理消息时间
       holder.chat_item_time.setText(parseTime(message.getTime()));
        if(position==0||compareTime(messages.get(position-1).getTime(),messages.get(position).getTime())){
            holder.chat_item_time.setVisibility(View.VISIBLE);
        }else {
            holder.chat_item_time.setVisibility(View.GONE);
        }
        //处理消息显示
        holder.chat_item_msg.setText(message.getContent());

        //TODO

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder{

        private final TextView chat_item_time;
        private final TextView chat_item_msg;

        public ChatViewHolder(View itemView) {
            super(itemView);
            chat_item_time = (TextView) itemView.findViewById(R.id.message_item_time);
            chat_item_msg = (TextView) itemView.findViewById(R.id.message_item_msg);
        }
    }

    public ArrayList<SuggestionChatInfoBean> getList(){
        return messages;
    }

    public String parseTime(String times){
        long timem = Long.parseLong(times);
        Date time = new Date(timem);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(time);
        return dateString;
    }

    public boolean compareTime(String preStrTime,String nowStrTime){
        long preTime = Long.parseLong(preStrTime);
        long nowTime = Long.parseLong(nowStrTime);
        return (nowTime - preTime >= 20000)? true:false;
    }
}
