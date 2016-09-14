package com.cylan.jiafeigou.n.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class RelativeAndFriendAddFromContactAdapter extends RecyclerView.Adapter<RelativeAndFriendAddFromContactAdapter.AddContactHolder> {

    private ArrayList<SuggestionChatInfoBean> messages;

    public RelativeAndFriendAddFromContactAdapter(ArrayList<SuggestionChatInfoBean> messages) {
        this.messages = messages;
    }

    private onContactItemClickListener listener;

    public interface onContactItemClickListener {
        void onClick(View view, int position);
    }

    ;


    public void setOnContactItemClickListener(onContactItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public AddContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_mine_relativeandfriend_add_from_contact_item, parent, false);
        return new AddContactHolder(view);
    }

    @Override
    public int getItemCount() {
        if (messages != null) {
            return messages.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(final AddContactHolder holder, final int position) {

        SuggestionChatInfoBean bean = messages.get(position);
        holder.tv_contactname.setText(bean.getName());
        holder.tv_contactphone.setText(bean.getContent());
        holder.tv_contactadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(holder.itemView, position);
                }
            }
        });
    }

    public class AddContactHolder extends RecyclerView.ViewHolder {

        private final TextView tv_contactname;
        private final TextView tv_contactphone;
        private final TextView tv_contactadd;

        public AddContactHolder(View itemView) {
            super(itemView);
            tv_contactname = (TextView) itemView.findViewById(R.id.tv_contactname);
            tv_contactphone = (TextView) itemView.findViewById(R.id.tv_contactphone);
            tv_contactadd = (TextView) itemView.findViewById(R.id.tv_contactadd);
        }
    }

    public ArrayList<SuggestionChatInfoBean> getAdapterList() {
        return messages;
    }
}
