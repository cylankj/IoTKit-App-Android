package com.cylan.jiafeigou.widget

import android.content.Context
import android.graphics.Rect
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.View

/**
 * Created by yanzhendong on 2017/10/12.
 */
class CameraNoMessageCenterBehavior : CoordinatorLayout.Behavior<View> {
    private var offset: Int = 0

    constructor() {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)



    override fun onLayoutChild(parent: CoordinatorLayout?, child: View?, layoutDirection: Int): Boolean {
        val dependencies = parent!!.getDependencies(child!!)
        val dependency = dependencies[0]
        val container = Rect(0, dependency.measuredHeight, parent.measuredWidth, parent.measuredHeight)
        val outRect = Rect()
        GravityCompat.apply(Gravity.CENTER, child.measuredWidth, child.measuredHeight, container, outRect, layoutDirection)
        child.layout(outRect.left, outRect.top, outRect.right, outRect.bottom)
        offset = 0
        offsetViewNeeded(child, dependency)
        return true
    }

    private fun offsetViewNeeded(child: View?, dependency: View?) {
        val layoutParams = dependency!!.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior
        if (behavior is AppBarLayout.Behavior) {
            val ablBehavior = behavior as AppBarLayout.Behavior?
            val topAndBottomOffset = ablBehavior!!.topAndBottomOffset
            val detail = topAndBottomOffset - this.offset
            this.offset = topAndBottomOffset
            ViewCompat.offsetTopAndBottom(child, detail / 2)
        }
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
        offsetViewNeeded(child, dependency)
        return true
    }

}
