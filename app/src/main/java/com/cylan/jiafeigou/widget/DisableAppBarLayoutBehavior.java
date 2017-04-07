package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by hds on 17-4-6.
 */

public class DisableAppBarLayoutBehavior extends AppBarLayout.ScrollingViewBehavior {
    private boolean mEnabled;

    public DisableAppBarLayoutBehavior() {
        super();
    }

    public DisableAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
        Log.d("setEnabled", "setEnabled:" + enabled);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        return mEnabled && super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }


    public boolean isEnabled() {
        return mEnabled;
    }
}