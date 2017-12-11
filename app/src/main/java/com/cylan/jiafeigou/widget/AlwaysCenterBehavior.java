package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import java.util.List;

/**
 * Created by yanzhendong on 2017/10/12.
 */
@SuppressWarnings("unused")
public class AlwaysCenterBehavior extends CoordinatorLayout.Behavior {
    private int offset;

    public AlwaysCenterBehavior() {
    }

    public AlwaysCenterBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout || "CAMERA_MESSAGE_HEADER".equals(dependency.getTag());
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        List<View> dependencies = parent.getDependencies(child);
        View dependency = dependencies.get(0);
        final Rect container = new Rect(0, dependency.getMeasuredHeight(), parent.getMeasuredWidth(), parent.getMeasuredHeight());
        final Rect outRect = new Rect();
        GravityCompat.apply(Gravity.CENTER, child.getMeasuredWidth(), child.getMeasuredHeight(), container, outRect, layoutDirection);
        child.layout(outRect.left, outRect.top, outRect.right, outRect.bottom);
        offset = 0;
        offsetViewNeeded(child, dependency);
        return true;
    }

    private void offsetViewNeeded(View child, View dependency) {
        final CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior instanceof AppBarLayout.Behavior) {
            AppBarLayout.Behavior ablBehavior = (AppBarLayout.Behavior) behavior;
            int topAndBottomOffset = ablBehavior.getTopAndBottomOffset();
            int detail = topAndBottomOffset - this.offset;
            this.offset = topAndBottomOffset;
            ViewCompat.offsetTopAndBottom(child, detail / 2);
        }
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        offsetViewNeeded(child, dependency);
        return true;
    }

}
