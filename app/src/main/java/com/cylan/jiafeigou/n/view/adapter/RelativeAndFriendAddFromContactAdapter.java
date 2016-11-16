package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class RelativeAndFriendAddFromContactAdapter extends SuperAdapter<RelAndFriendBean> {

    private onContactItemClickListener listener;

    public interface onContactItemClickListener {
        void onAddClick(View view, int position,RelAndFriendBean item);
    }

    public void setOnContactItemClickListener(onContactItemClickListener listener) {
        this.listener = listener;
    }

    public RelativeAndFriendAddFromContactAdapter(Context context, List<RelAndFriendBean> items, IMulItemViewType<RelAndFriendBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, final int layoutPosition, final RelAndFriendBean item) {
        holder.setText(R.id.tv_contactname,item.alias);
        holder.setText(R.id.tv_contactphone,item.account);

        TextView addBtn = holder.getView(R.id.tv_contactadd);
        if (item.isCheckFlag == 1){
            addBtn.setText("已添加");
            addBtn.setEnabled(false);
            addBtn.setTextColor(Color.parseColor("#ADADAD"));
            addBtn.setBackground(null);
        }
        holder.setOnClickListener(R.id.tv_contactadd, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.onAddClick(holder.getView(layoutPosition),layoutPosition,item);
                }
            }
        });
    }

    @Override
    protected IMulItemViewType<RelAndFriendBean> offerMultiItemViewType() {
        return new IMulItemViewType<RelAndFriendBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, RelAndFriendBean bean) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_mine_relativeandfriend_add_from_contact_item;
            }
        };
    }

}
