package com.cylan.jiafeigou.widget

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R

/**
 * Created by yzd on 17-12-9.
 */
class CameraMessageAppBarBehavior(context: Context, attrs: AttributeSet) : AppBarLayout.Behavior(context, attrs) {

    init {
        setDragCallback(object : DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })
    }

    override fun onLayoutChild(parent: CoordinatorLayout?, abl: AppBarLayout, layoutDirection: Int): Boolean {
        val onLayoutChild = super.onLayoutChild(parent, abl, layoutDirection)
        val layoutParams = abl.layoutParams
        if (layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            topAndBottomOffset = 0
        }
        return onLayoutChild
    }


    override fun onMeasureChild(parent: CoordinatorLayout?, child: AppBarLayout, parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int): Boolean {
        val layoutParams = child.layoutParams
        return if (layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed)
        } else {
            child.measure(parentWidthMeasureSpec, View.MeasureSpec.makeMeasureSpec(child.context.resources.getDimensionPixelSize(R.dimen.y245), View.MeasureSpec.AT_MOST))
            true
        }
    }

    override fun onStartNestedScroll(parent: CoordinatorLayout, child: AppBarLayout, directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
        super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type)
        return true
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (child.layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {

            return
        }
        if (target.id == R.id.face_header) {
            if ((!target.canScrollVertically(1) && dy > 0)) {
                super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
//                        || (!target.canScrollVertically(1) && dy < 0)
            } else if (dy < 0) {
                super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
            }
        } else {
            if (dy < 0 && !target.canScrollHorizontally(-1)) {
                super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
            } else {
                super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
            }
        }
    }

}