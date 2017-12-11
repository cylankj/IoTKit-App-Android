package com.cylan.jiafeigou.widget

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by yanzhendong on 2017/12/11.
 */
class InterceptSwipeRefreshLayout(context: Context, attrs: AttributeSet) : SwipeRefreshLayout(context, attrs) {

    var interceptListener: InterceptListener? = null

    interface InterceptListener {
        fun shouldIntercept(ev: MotionEvent): Boolean
    }


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return interceptListener?.shouldIntercept(ev) == true && super.onInterceptTouchEvent(ev)
    }
}