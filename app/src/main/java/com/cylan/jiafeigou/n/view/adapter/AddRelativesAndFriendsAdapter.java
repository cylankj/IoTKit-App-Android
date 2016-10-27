package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;

import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.R;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;


public class AddRelativesAndFriendsAdapter extends SuperAdapter<JFGFriendRequest> {


    private OnAcceptClickLisenter lisenter;

    public interface OnAcceptClickLisenter {
        void onAccept(SuperViewHolder holder, int viewType, int layoutPosition, JFGFriendRequest item);
    }

    public void setOnAcceptClickLisenter(OnAcceptClickLisenter lisenter) {
        this.lisenter = lisenter;
    }

    public AddRelativesAndFriendsAdapter(Context context, List<JFGFriendRequest> items, IMulItemViewType<JFGFriendRequest> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, final int viewType, final int layoutPosition, final JFGFriendRequest item) {
        holder.setText(R.id.tv_username, item.alias);
        holder.setText(R.id.tv_add_message, item.sayHi);


        if (layoutPosition == getItemCount() - 1) {
            holder.setVisibility(R.id.view_line, View.INVISIBLE);
        }

        holder.setOnClickListener(R.id.tv_accept_request, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lisenter != null) {
                    lisenter.onAccept(holder, viewType, layoutPosition, item);
                }
            }
        });
    }


    @Override
    protected IMulItemViewType<JFGFriendRequest> offerMultiItemViewType() {
        return new IMulItemViewType<JFGFriendRequest>() {
            @Override

            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, JFGFriendRequest jfgFriendRequest) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_mine_relativesandfriends_request_add_items;
            }
        };
    }

}
