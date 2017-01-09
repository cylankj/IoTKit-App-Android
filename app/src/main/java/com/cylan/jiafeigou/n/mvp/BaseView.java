package com.cylan.jiafeigou.n.mvp;

import android.content.Context;

/**
 * Created by hunt on 16-5-14.
 */
public interface BaseView<T> {

    void setPresenter(T presenter);

    Context getContext();
}
