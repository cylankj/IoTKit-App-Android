package com.cylan.jiafeigou.n.mvp;

/**
 * Created by hunt on 16-5-14.
 */
public interface BaseFragmentView<T> extends BaseView<T> {

    boolean isAdded();

    @Override
    String getUuid();
}
