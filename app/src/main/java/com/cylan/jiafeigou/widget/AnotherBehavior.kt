package com.cylan.jiafeigou.widget

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.cylan.jiafeigou.R

/**
 * Created by yanzhendong on 2017/12/8.
 */
class AnotherBehavior(context: Context, attrs: AttributeSet?) : CoordinatorLayout.Behavior<View>(context, attrs) {
    private var headerOffset: Int = 0
    override fun layoutDependsOn(parent: CoordinatorLayout?, child: View?, dependency: View): Boolean {
        return dependency.id == R.id.fLayout_message_face
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        child.layout(0, 0, parent.measuredWidth, child.measuredHeight)
        child.offsetTopAndBottom(-headerOffset)
        return true
    }

    override fun onMeasureChild(parent: CoordinatorLayout?, child: View, parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int): Boolean {
        val heightSpec = View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.AT_MOST)
        child.measure(parentWidthMeasureSpec, heightSpec)
        return true
    }


    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val view = coordinatorLayout.getDependencies(child)[0]
//        if (target == child) {
//        if (child.bottom > 0 && dy > 0) {
//            val offset = Math.min(child.bottom, dy)
//            consumed[1] = offset
//            headerOffset -= offset
//            child.offsetTopAndBottom(-offset)
//        } else

        if (child.top < 0 && dy < 0) {
            val offset = Math.max(child.top, dy)
            headerOffset += offset
            consumed[1] = offset
            child.offsetTopAndBottom(-offset)
        }
//        } else {
//            if (view.top < 0 && dy < 0) {
//                val offset = Math.max(view.top, dy)
//                consumed[1] = offset
//                view.offsetTopAndBottom(-offset)
//            }
//        }
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        //        if (dyUnconsumed < 0 && child.top < 0) {
//            val offset = Math.max(dyUnconsumed, child.top)
//            child.offsetTopAndBottom(-offset)
//        } else

        if (dyUnconsumed > 0 && child.bottom > 0) {
            val offset = Math.min(child.bottom, dyUnconsumed)
            headerOffset += offset
            child.offsetTopAndBottom(-offset)
        }

        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: View, target: View, velocityX: Float, velocityY: Float): Boolean {
        Log.e("AAAAA", "----------------------------onNestedPreFling,velocityY:$velocityY")
        if (target.id != R.id.face_header) {
            if (velocityY > 100) {
                child.offsetTopAndBottom(child.measuredHeight - headerOffset)
                headerOffset = 0
            } else if (velocityY <= 100) {
                child.offsetTopAndBottom(-headerOffset)
                headerOffset = 0
            }
        }
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }
}