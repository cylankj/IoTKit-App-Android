package com.cylan.jiafeigou.n.view.adapter.item;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

/**
 * Created by yanzhendong on 2017/5/26.
 */

public class AbstractBindingViewHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder {
    protected T viewDataBinding;

    public AbstractBindingViewHolder(T viewDataBinding) {
        super(viewDataBinding.getRoot());
        this.viewDataBinding = viewDataBinding;
    }

    public T getViewDataBinding() {
        return viewDataBinding;
    }
}
