package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by yanzhendong on 2017/10/12.
 */

public class AlwaysCenterBehavior extends AppBarLayout.ScrollingViewBehavior {

    private Method findFirstDependencyMethod;
    private Method getLastWindowInsetsMethod;
    private Method getVerticalLayoutGapMethod;
    private Method getOverlapPixelsForOffsetMethod;
    private Field offsetDeltaField;
    private Field verticalLayoutGapField;
    final Rect mTempRect1 = new Rect();
    final Rect mTempRect2 = new Rect();
    private boolean hasLayout = false;

    public AlwaysCenterBehavior() {
        init();
    }

    public AlwaysCenterBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        offsetChildAsNeeded(parent, child, dependency);
        return false;
    }


    @Override
    protected void layoutChild(final CoordinatorLayout parent, final View child,
                               final int layoutDirection) {
        if (hasLayout) return;
        final List<View> dependencies = parent.getDependencies(child);
        try {
            final View header = (View) findFirstDependencyMethod.invoke(this, dependencies);
            if (header != null) {

                final CoordinatorLayout.LayoutParams lp =
                        (CoordinatorLayout.LayoutParams) child.getLayoutParams();
                final Rect available = mTempRect1;

                available.set(parent.getPaddingLeft() + lp.leftMargin,
                        header.getBottom() + lp.topMargin,
                        parent.getWidth() - parent.getPaddingRight() - lp.rightMargin,
                        parent.getHeight()
                                - parent.getPaddingBottom() - lp.bottomMargin);

                final WindowInsetsCompat parentInsets = (WindowInsetsCompat) getLastWindowInsetsMethod.invoke(parent);
                if (parentInsets != null && ViewCompat.getFitsSystemWindows(parent)
                        && !ViewCompat.getFitsSystemWindows(child)) {
                    // If we're set to handle insets but this child isn't, then it has been measured as
                    // if there are no insets. We need to lay it out to match horizontally.
                    // Top and bottom and already handled in the logic above
                    available.left += parentInsets.getSystemWindowInsetLeft();
                    available.right -= parentInsets.getSystemWindowInsetRight();
                }

                final Rect out = mTempRect2;
                GravityCompat.apply(resolveGravity(lp.gravity), child.getMeasuredWidth(),
                        child.getMeasuredHeight(), available, out, layoutDirection);

                final Integer overlap = (Integer) getOverlapPixelsForOffsetMethod.invoke(this, header);

                child.layout(out.left, out.top - overlap, out.right, out.bottom - overlap);
                verticalLayoutGapField.set(this, out.top - header.getBottom());
//                mVerticalLayoutGap = out.top - header.getBottom();
            } else {
                // If we don't have a dependency, let super handle it
                super.layoutChild(parent, child, layoutDirection);
//                mVerticalLayoutGap = 0;
                verticalLayoutGapField.set(this, 0);
            }
            hasLayout = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        try {
            findFirstDependencyMethod = AppBarLayout.ScrollingViewBehavior.class.getDeclaredMethod("findFirstDependency", List.class);
            findFirstDependencyMethod.setAccessible(true);
            getLastWindowInsetsMethod = CoordinatorLayout.class.getDeclaredMethod("getLastWindowInsets");
            getLastWindowInsetsMethod.setAccessible(true);
            Class<?> forName = Class.forName("android.support.design.widget.HeaderScrollingViewBehavior");
            getVerticalLayoutGapMethod = forName.getDeclaredMethod("getVerticalLayoutGap");
            getVerticalLayoutGapMethod.setAccessible(true);
            getOverlapPixelsForOffsetMethod = forName.getDeclaredMethod("getOverlapPixelsForOffset", View.class);
            getOverlapPixelsForOffsetMethod.setAccessible(true);
            offsetDeltaField = AppBarLayout.Behavior.class.getDeclaredField("mOffsetDelta");
            offsetDeltaField.setAccessible(true);
            verticalLayoutGapField = forName.getDeclaredField("mVerticalLayoutGap");
            verticalLayoutGapField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int resolveGravity(int gravity) {
        return gravity == Gravity.NO_GRAVITY ? GravityCompat.START | Gravity.TOP : gravity;
    }


    private void offsetChildAsNeeded(CoordinatorLayout parent, View child, View dependency) {
        final CoordinatorLayout.Behavior behavior =
                ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
        if (behavior instanceof AppBarLayout.Behavior) {
            // Offset the child, pinning it to the bottom the header-dependency, maintaining
            // any vertical gap and overlap
            final AppBarLayout.Behavior ablBehavior = (AppBarLayout.Behavior) behavior;
            try {
                Integer offsetDelta = (Integer) offsetDeltaField.get(ablBehavior);
                Integer VerticalLayoutGap = (Integer) getVerticalLayoutGapMethod.invoke(this);
                Integer OverlapPixelsForOffset = (Integer) getOverlapPixelsForOffsetMethod.invoke(this, dependency);

                ViewCompat.offsetTopAndBottom(child, (dependency.getBottom() - child.getTop())
                        + offsetDelta - ablBehavior.getTopAndBottomOffset() / 2
                        + VerticalLayoutGap
                        - OverlapPixelsForOffset);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
