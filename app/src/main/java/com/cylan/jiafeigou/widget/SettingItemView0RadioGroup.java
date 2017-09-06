package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

/**
 * Created by yanzhendong on 2017/8/11.
 */

public class SettingItemView0RadioGroup extends RadioGroup implements ViewGroup.OnHierarchyChangeListener {
    public SettingItemView0RadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init() {
        setOnHierarchyChangeListener(this);
    }

    @Override
    public void onChildViewAdded(View parent, View child) {

    }

    @Override
    public void onChildViewRemoved(View parent, View child) {

    }
}
