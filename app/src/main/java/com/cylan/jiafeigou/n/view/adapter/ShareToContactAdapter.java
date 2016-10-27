package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class ShareToContactAdapter extends SuperAdapter<SuggestionChatInfoBean> {

    private onShareLisenter lisenter;

    public interface onShareLisenter {
        void isShare(SuggestionChatInfoBean item);
    }

    public void setOnShareLisenter(onShareLisenter lisenter) {
        this.lisenter = lisenter;
    }

    public ShareToContactAdapter(Context context, List<SuggestionChatInfoBean> items, IMulItemViewType<SuggestionChatInfoBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, final int layoutPosition, final SuggestionChatInfoBean item) {
        holder.setText(R.id.tv_contactname,item.getName());
        holder.setText(R.id.tv_contactphone,item.getContent());
        holder.setOnClickListener(R.id.tv_contactshare, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lisenter != null){
                    lisenter.isShare(item);
                }
            }
        });
    }

    @Override
    protected IMulItemViewType<SuggestionChatInfoBean> offerMultiItemViewType() {
        return new IMulItemViewType<SuggestionChatInfoBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, SuggestionChatInfoBean bean) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_share_to_contact_item;
            }
        };
    }

}
