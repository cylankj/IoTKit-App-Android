package com.cylan.jiafeigou.widget

import android.content.Context
import android.graphics.Rect
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import com.cylan.jiafeigou.R

/**
 * Created by yanzhendong on 2017/10/12.
 */
class CameraMessageCenterBehavior : CoordinatorLayout.Behavior<View> {
    private var dependencyBottom: Int = 0


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: View?, dependency: View): Boolean {
        return dependency.id == R.id.rLayout_cam_message_list_top
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        val dependencies = parent.getDependencies(child)
        val dependency = dependencies[0]
        dependencyBottom = Math.min(dependency.bottom, parent.bottom)
        val container = Rect(0, dependencyBottom, parent.measuredWidth, parent.bottom)
        val outRect = Rect()
        GravityCompat.apply(Gravity.CENTER, child.measuredWidth, child.measuredHeight, container, outRect, layoutDirection)
        outRect.top = Math.max(dependencyBottom, outRect.top)
        child.layout(outRect.left, outRect.top, outRect.right, outRect.bottom)
        offsetViewNeeded(parent, child, dependency)
        return true
    }

    private fun offsetViewNeeded(parent: CoordinatorLayout, child: View, dependency: View) {
        val lastBottom = dependencyBottom
        dependencyBottom = Math.min(dependency.bottom, parent.bottom)
        val offset = dependencyBottom - lastBottom
        ViewCompat.offsetTopAndBottom(child, offset / 2)
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        offsetViewNeeded(parent, child, dependency)
        return true
    }

}
