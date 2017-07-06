package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.JFGAccountURL;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/10/26
 * 描述：
 */
public class MineHasShareAdapter extends SuperAdapter<JFGFriendAccount> {

    private OnCancelShareListener listener;

    public interface OnCancelShareListener {
        void onCancelShare(int position, JFGFriendAccount item);
    }

    public void setOnCancelShareListener(OnCancelShareListener listener) {
        this.listener = listener;
    }


    public MineHasShareAdapter(Context context, List<JFGFriendAccount> items, IMulItemViewType<JFGFriendAccount> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, final JFGFriendAccount item) {
        holder.setText(R.id.tv_username, TextUtils.isEmpty(item.markName) ? item.alias : item.markName);
        holder.setText(R.id.tv_friend_account, item.account);
        holder.setOnClickListener(R.id.tv_btn_cancle_share, v -> {
            if (listener != null) {//存在复用的情况,不可取
                listener.onCancelShare(layoutPosition, item);
            }
        });
        //头像
        Glide.with(getContext()).load(new JFGAccountURL(item.account))
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
            public int getItemViewType(int position, JFGFriendAccount account) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_has_share_to_friend_items;
            }
        };
    }
}
