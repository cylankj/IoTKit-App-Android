package com.cylan.jiafeigou.base.module;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingListener;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.support.v4.widget.SwipeRefreshLayout;

/**
 * Created by yanzhendong on 2017/5/31.
 */
@InverseBindingMethods({@InverseBindingMethod(type = SwipeRefreshLayout.class, attribute = "app:refreshing", event = "app:refreshingAttrChanged", method = "isRefreshing")})
public class JFGBindingAdapter {

    @BindingAdapter({"app:refreshingAttrChanged"})
    public static void bindingRefresh(SwipeRefreshLayout layout, InverseBindingListener listener) {
    }


}
