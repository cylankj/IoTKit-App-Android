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


public class RelativesAndFriendsAdapter extends RecyclerView.Adapter<RelativesAndFriendsAdapter.RequestAndFriends> {
    private ArrayList<SuggestionChatInfoBean> messages;
    public RelativesAndFriendsAdapter(ArrayList<SuggestionChatInfoBean> messages){
        this.messages = messages;
    }
    @Override
    public RequestAndFriends onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_mine_relativesandfriends_request_add_items,parent,false);

        return new RequestAndFriends(view);
    }
    //发送消息  0  接收receive  1

    @Override
    public void onBindViewHolder(RequestAndFriends holder, int position) {
        SuggestionChatInfoBean message = messages.get(position);
        //处理消息显示
        holder.tv_username.setText(message.getName());
        holder.tv_add_message.setText(message.getContent());
        if(message.isShowAcceptButton){
            holder.tv_accept_request.setVisibility(View.VISIBLE);
            holder.line.setVisibility(View.INVISIBLE);
        }else {
            holder.tv_accept_request.setVisibility(View.INVISIBLE);
            holder.line.setVisibility(View.VISIBLE);
        }

        //TODO
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class RequestAndFriends extends RecyclerView.ViewHolder{

        private final TextView tv_username;
        private final TextView tv_add_message;
        private final TextView tv_accept_request;
        private final View line;

        public RequestAndFriends(View itemView) {
            super(itemView);
            tv_username = (TextView) itemView.findViewById(R.id.tv_username);
            tv_add_message = (TextView) itemView.findViewById(R.id.tv_add_message);
            tv_accept_request = (TextView) itemView.findViewById(R.id.tv_accept_request);
            line = itemView.findViewById(R.id.view_line);
        }
    }

    public ArrayList<SuggestionChatInfoBean> getList(){
        return messages;
    }

}
