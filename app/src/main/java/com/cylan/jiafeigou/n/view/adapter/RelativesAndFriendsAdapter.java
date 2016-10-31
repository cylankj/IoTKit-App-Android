package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.R;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;


public class RelativesAndFriendsAdapter extends SuperAdapter<JFGFriendAccount> {


    public RelativesAndFriendsAdapter(Context context, List<JFGFriendAccount> items, IMulItemViewType<JFGFriendAccount> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, JFGFriendAccount item) {
        //TODO 如果没有备注名就显示别人昵称或者账号
        holder.setText(R.id.tv_username, (item.markName == null || item.markName.equals("")) ? item.alias : item.markName);
        holder.setText(R.id.tv_add_message, item.account);
    }

    @Override
    protected IMulItemViewType<JFGFriendAccount> offerMultiItemViewType() {
        return new IMulItemViewType<JFGFriendAccount>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, JFGFriendAccount jfgFriendAccount) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_mine_relativesandfriends_list_items;
            }
        };
    }
}
