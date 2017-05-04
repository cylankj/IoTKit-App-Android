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

    public DisableAppBarLayoutBehavior() {
        super();
    }

    public DisableAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        return canDragChecker != null && canDragChecker.canDrag() && super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    public void setCanDragChecker(CanDragChecker canDragChecker) {
        this.canDragChecker = canDragChecker;
    }

    private CanDragChecker canDragChecker;

    public interface CanDragChecker {
        boolean canDrag();
    }
}