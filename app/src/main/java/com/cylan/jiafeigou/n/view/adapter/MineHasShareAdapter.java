package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/10/26
 * 描述：
 */
public class MineHasShareAdapter extends SuperAdapter<RelAndFriendBean> {

    private OnCancleShareListenter listenter;

    public interface OnCancleShareListenter{
        void onCancleShare(RelAndFriendBean item);
    }

    public void setOnCancleShareListenter(OnCancleShareListenter listenter){
        this.listenter = listenter;
    }


    public MineHasShareAdapter(Context context, List<RelAndFriendBean> items, IMulItemViewType<RelAndFriendBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, final RelAndFriendBean item) {
        holder.setText(R.id.tv_username,item.alias);
        holder.setText(R.id.tv_friend_account,item.account);
        holder.setOnClickListener(R.id.tv_btn_cancle_share, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listenter != null){
                    listenter.onCancleShare(item);
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
            public int getItemViewType(int position, RelAndFriendBean account) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_has_share_to_friend_items;
            }
        };
    }
}
