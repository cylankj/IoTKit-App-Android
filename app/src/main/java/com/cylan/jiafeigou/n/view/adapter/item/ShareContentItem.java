package com.cylan.jiafeigou.n.view.adapter.item;

import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.databinding.ItemShareContentBinding;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

/**
 * Created by yanzhendong on 2017/5/26.
 */
public class ShareContentItem extends AbstractItem<ShareContentItem, AbstractBindingViewHolder<ItemShareContentBinding>> {
    public DpMsgDefine.DPShareItem shareItem;
    private UnShareListener unshareListener;

    public ShareContentItem(DpMsgDefine.DPShareItem shareItem) {
        this.shareItem = shareItem;
    }

    @Override
    public AbstractBindingViewHolder<ItemShareContentBinding> getViewHolder(View v) {
        ItemShareContentBinding contentBinding = ItemShareContentBinding.bind(v);
        return new AbstractBindingViewHolder<>(contentBinding);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_share_content;
    }

    @Override
    public void bindView(AbstractBindingViewHolder<ItemShareContentBinding> holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        ItemShareContentBinding viewDataBinding = holder.getViewDataBinding();
        viewDataBinding.setSharedContentItem(this);
        viewDataBinding.setUnShareListener(unshareListener);
    }

    public ShareContentItem withUnShareListener(UnShareListener unshareListener) {
        this.unshareListener = unshareListener;
        return this;
    }

    public interface UnShareListener {
        void unShare(ShareContentItem shareContentItem, int position);
    }
}
