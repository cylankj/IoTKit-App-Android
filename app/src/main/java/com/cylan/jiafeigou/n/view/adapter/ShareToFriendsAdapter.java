package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.JFGAccountURL;

import java.util.List;


public class ShareToFriendsAdapter extends SuperAdapter<JFGFriendAccount> {

    private OnShareCheckListener listener;

    public interface OnShareCheckListener {
        void onCheck(boolean isCheck, SuperViewHolder holder, JFGFriendAccount item);
    }

    public void setOnShareCheckListener(OnShareCheckListener listener) {
        this.listener = listener;
    }

    public ShareToFriendsAdapter(Context context, List<JFGFriendAccount> items, int mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, final int layoutPosition, final JFGFriendAccount item) {
        //如果没有备注名就显示别人昵称
        holder.setText(R.id.tv_friend_name, (item.markName == null || item.markName.equals("")) ? item.alias : item.markName);
        holder.setText(R.id.tv_friend_account, item.account);
        CheckBox checkBox = (CheckBox) holder.itemView.findViewById(R.id.checkbox_is_share_check);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                item.isCheckFlag = isChecked ? 1 : 2;
                if (listener != null) {
                    listener.onCheck(isChecked, holder, item);
                }
            }
        });
        //头像
        GlideApp.with(getContext()).load(new JFGAccountURL(item.account))
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into((ImageView) holder.getView(R.id.iv_userhead));
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
                return R.layout.fragment_mine_share_to_friend_items;
            }
        };
    }
}
