package com.cylan.jiafeigou.n.view.adapter.item;

import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.databinding.FragmentMineShareToFriendItemsBinding;
import com.cylan.jiafeigou.utils.JFGAccountURL;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

/**
 * Created by yanzhendong on 2017/6/28.
 */

public class ShareFriendItem extends AbstractItem<ShareFriendItem, AbstractBindingViewHolder<FragmentMineShareToFriendItemsBinding>> {
    public JFGFriendAccount friendAccount;

    public ShareFriendItem(JFGFriendAccount account) {
        this.friendAccount = account;
    }

    @Override
    public AbstractBindingViewHolder<FragmentMineShareToFriendItemsBinding> getViewHolder(View v) {
        FragmentMineShareToFriendItemsBinding friendItemsBinding = FragmentMineShareToFriendItemsBinding.bind(v);
        return new AbstractBindingViewHolder<>(friendItemsBinding);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public ShareFriendItem withSetSelected(boolean selected) {
        return super.withSetSelected(selected);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_mine_share_to_friend_items;
    }

    @Override
    public void bindView(AbstractBindingViewHolder<FragmentMineShareToFriendItemsBinding> holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        FragmentMineShareToFriendItemsBinding dataBinding = holder.getViewDataBinding();
        dataBinding.tvFriendName.setText((friendAccount.markName == null || friendAccount.markName.equals("")) ? friendAccount.alias : friendAccount.markName);
        dataBinding.tvFriendAccount.setText(friendAccount.account);
        dataBinding.checkboxIsShareCheck.setChecked(isSelected());
        //头像
        Glide.with(dataBinding.ivUserhead.getContext()).load(new JFGAccountURL(friendAccount.account))
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(dataBinding.ivUserhead);
    }
}
