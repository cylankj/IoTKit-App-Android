package com.cylan.jiafeigou.widget

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import com.cylan.jiafeigou.R

/**
 * Created by yanzhendong on 2017/12/8.
 */
class CameraMessageBehavior(context: Context, attrs: AttributeSet?) : CoordinatorLayout.Behavior<View>(context, attrs) {
    override fun layoutDependsOn(parent: CoordinatorLayout?, child: View?, dependency: View): Boolean {
        return dependency.id == R.id.rLayout_cam_message_list_top
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        val view = parent.getDependencies(child)[0]
        child.layout(0, view.bottom, child.measuredWidth, child.measuredHeight + view.bottom)
        return true
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        child.layout(0, dependency.bottom, child.measuredWidth, child.measuredHeight + dependency.bottom)
        return true
    }
}
    
