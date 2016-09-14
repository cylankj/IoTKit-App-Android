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
 * 创建时间：2016/9/13
 * 描述：
 */
public class ShareToContactAdapter extends RecyclerView.Adapter<ShareToContactAdapter.ShareToContactHolder> {

    private ArrayList<SuggestionChatInfoBean> data;

    public ShareToContactAdapter(ArrayList<SuggestionChatInfoBean> data) {
        this.data = data;
    }

    private onShareLisenter lisenter;

    public interface onShareLisenter {
        void isChecked(View view, int position);
    }

    public void setOnShareLisenter(onShareLisenter lisenter) {
        this.lisenter = lisenter;
    }

    @Override
    public ShareToContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_share_to_contact_item, parent, false);
        return new ShareToContactHolder(view);
    }

    @Override
    public void onBindViewHolder(final ShareToContactHolder holder, final int position) {
        SuggestionChatInfoBean bean = data.get(position);
        holder.tv_contactname.setText(bean.getName());
        holder.tv_contactphone.setText(bean.getContent());
        holder.tv_contactshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lisenter != null) {
                    lisenter.isChecked(holder.itemView, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (data == null) {
            return 0;
        } else {
            return data.size();
        }
    }

    public class ShareToContactHolder extends RecyclerView.ViewHolder {

        private final TextView tv_contactname;
        private final TextView tv_contactphone;
        private final TextView tv_contactshare;

        public ShareToContactHolder(View itemView) {
            super(itemView);
            tv_contactname = (TextView) itemView.findViewById(R.id.tv_contactname);
            tv_contactphone = (TextView) itemView.findViewById(R.id.tv_contactphone);
            tv_contactshare = (TextView) itemView.findViewById(R.id.tv_contactshare);

        }
    }

    /**
     * desc:获取适配器的list
     *
     * @return
     */
    public ArrayList<SuggestionChatInfoBean> getRcyList() {
        return data;
    }
}
