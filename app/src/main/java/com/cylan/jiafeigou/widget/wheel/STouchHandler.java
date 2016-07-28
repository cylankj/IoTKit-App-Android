package com.cylan.jiafeigou.widget.wheel;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.OvershootInterpolator;
import android.widget.OverScroller;

import com.cylan.jiafeigou.BuildConfig;

/**
 * Created by cylan-hunt on 16-6-18.
 */

public class STouchHandler extends GestureDetector.SimpleOnGestureListener {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private int scrollState = SuperWheel.SCROLL_STATE_IDLE;
    private SuperWheel superWheel;
    private GestureDetectorCompat gestureDetectorCompat;
    private OverScroller scroller;
    private int mTouchSlop;

    /**
     * 判定为拖动的最小移动像素数
     */

    public STouchHandler(SuperWheel SuperWheel) {
        this.superWheel = SuperWheel;
        Context context = SuperWheel.getContext();
        scroller = new OverScroller(context, new OvershootInterpolator(), 0.5f, 0);
        gestureDetectorCompat = new GestureDetectorCompat(context, this);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    private float mLastMotionX;
    private float mInitialMotionX;
    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;
    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 0;

    private int moveDirection = -1;//-1 nothing ,0:left ,1:right

    private boolean isActionUp = true;

    boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        final int mask = event.getAction() & MotionEventCompat.ACTION_MASK;

        // Always take care of the touch gesture being complete.
        if (mask == MotionEvent.ACTION_CANCEL || mask == MotionEvent.ACTION_UP) {
            superWheel.getParent().requestDisallowInterceptTouchEvent(false);
            // Release the drag.
            mActivePointerId = INVALID_POINTER;
            if (scrollState != SuperWheel.SCROLL_STATE_SETTLING) {
                if (scroller.isFinished())
                    updateScrollStateIfRequired(SuperWheel.SCROLL_STATE_IDLE);
                moveDirection = -1;
            }
            isActionUp = true;
            return false;
        }
        switch (mask) {
            case MotionEvent.ACTION_DOWN:
                superWheel.getParent().requestDisallowInterceptTouchEvent(true);
                isActionUp = false;
                if (!scroller.isFinished())
                    scroller.abortAnimation();
                moveDirection = -1;
                       /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionX = mInitialMotionX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(event, activePointerId);
                final float x = MotionEventCompat.getX(event, pointerIndex);
                final float dx = x - mLastMotionX;
                final float xDiff = Math.abs(dx);
                if (xDiff > mTouchSlop) {
                    mLastMotionX = dx > 0 ? mInitialMotionX + mTouchSlop :
                            mInitialMotionX - mTouchSlop;
                    moveDirection = dx > 0 ? DIRECTION_LEFT : DIRECTION_RIGHT;
                    if (DEBUG)
                        Log.d(SuperWheel.TAG, "moveeeeeee:   " + (dx > 0));
                }
                updateScrollStateIfRequired(SuperWheel.SCROLL_STATE_DRAGGING);
                break;
        }

        return true;
    }

    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            superWheel.scrollTo(scroller.getCurrX(), scroller.getCurrY());
            superWheel.invalidate();
        } else {
            if (isActionUp && scroller.isFinished()) {
                updateScrollStateIfRequired(SuperWheel.SCROLL_STATE_IDLE);
            }
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (Math.abs(distanceX) >= mTouchSlop) {
            if (distanceX > 0.0f) {
                distanceX = distanceX - mTouchSlop;
            } else distanceX = distanceX + mTouchSlop;
        }
        superWheel.scrollBy((int) distanceX, 0);
        if (DEBUG) {
            Log.d(SuperWheel.TAG, "onScroll...: " + distanceX + " " + superWheel.getScrollX());
        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (DEBUG) {
            Log.d("SuperWheel", "fling:  " + superWheel.getScrollX() + " " + superWheel.getMaxScrollX());
        }
        scroller.fling(superWheel.getScrollX(), 0,
                (int) -velocityX, 0,
                -superWheel.getMaxScrollX(), 0,
                0, 0);
        superWheel.invalidate();
        return true;
    }


    private void updateScrollStateIfRequired(int newState) {
        if (newState == SuperWheel.SCROLL_STATE_IDLE) {
            superWheel.autoSettle("SCROLL_STATE_IDLE", moveDirection);
        } else if (newState == SuperWheel.SCROLL_STATE_DRAGGING) {
            superWheel.updateScrollX();
        }
    }

}