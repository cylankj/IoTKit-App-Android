package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.FriendBean;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class FriendAddFromContactAdapter extends SuperAdapter<FriendBean> {

    private onContactItemClickListener listener;

    public interface onContactItemClickListener {
        void onAddClick(View view, int position, FriendBean item);
    }

    public void setOnContactItemClickListener(onContactItemClickListener listener) {
        this.listener = listener;
    }

    public FriendAddFromContactAdapter(Context context, List<FriendBean> items, IMulItemViewType<FriendBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, final int layoutPosition, final FriendBean item) {
        holder.setText(R.id.tv_contactname, item.alias);
        holder.setText(R.id.tv_contactphone, item.account);

        TextView addBtn = holder.getView(R.id.tv_contactadd);
        if (item.isCheckFlag == 1) {
            addBtn.setText(ContextUtils.getContext().getString(R.string.Tap3_Added));
            addBtn.setEnabled(false);
            addBtn.setTextColor(Color.parseColor("#ADADAD"));
            addBtn.setBackground(null);
        } else {
            addBtn.setText(ContextUtils.getContext().getString(R.string.Button_Add));
            addBtn.setEnabled(true);
            addBtn.setTextColor(Color.parseColor("#4b9fd5"));
            addBtn.setBackground(ContextUtils.getContext().getResources().getDrawable(R.drawable.btn_accept_add_request_shape));
        }
        holder.setOnClickListener(R.id.tv_contactadd, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAddClick(holder.getView(layoutPosition), layoutPosition, item);
                }
            }
        });
    }

    @Override
    protected IMulItemViewType<FriendBean> offerMultiItemViewType() {
        return new IMulItemViewType<FriendBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, FriendBean bean) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_mine_friend_add_from_contact_item;
            }
        };
    }

}
