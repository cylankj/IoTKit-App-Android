package com.cylan.jiafeigou.n.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;

import java.util.ArrayList;


public class AddRelativesAndFriendsAdapter extends RecyclerView.Adapter<AddRelativesAndFriendsAdapter.RequestAndFriends> {
    private ArrayList<SuggestionChatInfoBean> messages;

    public AddRelativesAndFriendsAdapter(ArrayList<SuggestionChatInfoBean> messages) {
        this.messages = messages;
    }

    private ItemClickListener itemClickListener;
    private ItemLongClickLisenter itemLongClickLisenter;
    private ItemOutOfBtnClickListener itemOutOfBtnClickListener;

    public interface ItemClickListener {
        void onClick(View view, int position);
    }

    public interface ItemLongClickLisenter {
        void onItemLongClick(View view, int position);
    }

    public interface ItemOutOfBtnClickListener {
        void onOutBtnClick(View view, int position);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemLongClickLisenter(ItemLongClickLisenter longClickLisenter) {
        this.itemLongClickLisenter = longClickLisenter;
    }

    public void setItemOutOfBtnClickListener(ItemOutOfBtnClickListener itemOutOfBtnClickListener) {
        this.itemOutOfBtnClickListener = itemOutOfBtnClickListener;
    }

    @Override
    public RequestAndFriends onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_mine_relativesandfriends_request_add_items, parent, false);
        return new RequestAndFriends(view);
    }

    @Override
    public void onBindViewHolder(final RequestAndFriends holder, final int position) {
        SuggestionChatInfoBean message = messages.get(position);
        //处理消息显示
        holder.tv_username.setText(message.getName());
        holder.tv_add_message.setText(message.getContent());
        if (message.isShowAcceptButton) {
            holder.tv_accept_request.setVisibility(View.VISIBLE);
            holder.line.setVisibility(View.INVISIBLE);
        } else {
            holder.tv_accept_request.setVisibility(View.INVISIBLE);
            holder.line.setVisibility(View.VISIBLE);
        }
        //条目点击事件
        holder.tv_accept_request.setOnClickListener(new View.OnClickListener() {
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
                if (itemLongClickLisenter != null) {
                    itemLongClickLisenter.onItemLongClick(holder.itemView, position);
                }
                return false;
            }
        });

        //点击按钮之外的地方
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemOutOfBtnClickListener != null) {
                    itemOutOfBtnClickListener.onOutBtnClick(holder.itemView, position);
                }
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
        public final TextView tv_accept_request;
        public final View line;
        private final RelativeLayout rl_outsite_btn;

        public RequestAndFriends(View itemView) {
            super(itemView);
            tv_username = (TextView) itemView.findViewById(R.id.tv_username);
            tv_add_message = (TextView) itemView.findViewById(R.id.tv_add_message);
            tv_accept_request = (TextView) itemView.findViewById(R.id.tv_accept_request);
            line = itemView.findViewById(R.id.view_line);
            rl_outsite_btn = (RelativeLayout) itemView.findViewById(R.id.rl_out_of_accept_btn);
        }
    }

    public ArrayList<SuggestionChatInfoBean> getList() {
        return messages;
    }

}
