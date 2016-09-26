package com.cylan.jiafeigou.n.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;

import java.util.ArrayList;


public class RelativesAndFriendsAdapter extends RecyclerView.Adapter<RelativesAndFriendsAdapter.RequestAndFriends> {
    private ArrayList<SuggestionChatInfoBean> messages;

    public RelativesAndFriendsAdapter(ArrayList<SuggestionChatInfoBean> messages) {
        this.messages = messages;
    }

    private ItemClickListener itemClickListener;
    private ItemLongClickLisenter itemLongClickLisenter;

    public interface ItemClickListener {
        void onClick(View view, int position);
    }

    public interface ItemLongClickLisenter{
        void onLongClick(View view, int position);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemLongClickLisenter(ItemLongClickLisenter longClickLisenter){
        this.itemLongClickLisenter = longClickLisenter;
    }

    @Override
    public RequestAndFriends onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_mine_relativesandfriends_list_items, parent, false);
        return new RequestAndFriends(view);
    }

    @Override
    public void onBindViewHolder(final RequestAndFriends holder, final int position) {
        SuggestionChatInfoBean message = messages.get(position);
        //处理消息显示
        holder.tv_username.setText(message.getName());
        holder.tv_add_message.setText(message.getContent());
        //条目点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onClick(holder.itemView, position);
                }
            }
        });

        //条目长按删除
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(itemLongClickLisenter != null){
                    itemLongClickLisenter.onLongClick(holder.itemView,position);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class RequestAndFriends extends RecyclerView.ViewHolder {

        public final TextView tv_username;
        public final TextView tv_add_message;
        public final View line;

        public RequestAndFriends(View itemView) {
            super(itemView);
            tv_username = (TextView) itemView.findViewById(R.id.tv_username);
            tv_add_message = (TextView) itemView.findViewById(R.id.tv_add_message);
            line = itemView.findViewById(R.id.view_line);
        }
    }

    public ArrayList<SuggestionChatInfoBean> getList() {
        return messages;
    }

}
