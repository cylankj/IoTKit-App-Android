package com.cylan.jiafeigou.widget.wheel;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.cylan.jiafeigou.BuildConfig;

/**
 * Created by cylan-hunt on 16-6-18.
 */

public class TouchHandler extends GestureDetector.SimpleOnGestureListener {
    private final static boolean DEBUG = BuildConfig.DEBUG;
    private int scrollState = WheelView.SCROLL_STATE_IDLE;
    private Context context;
    private WheelView wheelView;
    private GestureDetectorCompat gestureDetectorCompat;
    private int mTouchSlop;

    /**
     * 判定为拖动的最小移动像素数
     */

    public TouchHandler(WheelView wheelView) {
        this.wheelView = wheelView;
        this.context = wheelView.getContext();
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

    private int moveDirection = -1;//-1 nothing ,0:left ,1:right

    boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
//        final int mask = event.getActionMasked();
        final int mask = event.getAction() & MotionEventCompat.ACTION_MASK;

        // Always take care of the touch gesture being complete.
        if (mask == MotionEvent.ACTION_CANCEL || mask == MotionEvent.ACTION_UP) {
            // Release the drag.
            mActivePointerId = INVALID_POINTER;
            if (scrollState != WheelView.SCROLL_STATE_SETTLING) {
                updateScrollStateIfRequired(WheelView.SCROLL_STATE_IDLE);
                moveDirection = -1;
            }
            return false;
        }
        switch (mask) {
            case MotionEvent.ACTION_DOWN:
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
                    // If we don't have activity_cloud_live_mesg_call_out_item valid id, the touch down wasn't on content.
                    break;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(event, activePointerId);
                final float x = MotionEventCompat.getX(event, pointerIndex);
                final float dx = x - mLastMotionX;
                final float xDiff = Math.abs(dx);
                if (xDiff > mTouchSlop) {
                    mLastMotionX = dx > 0 ? mInitialMotionX + mTouchSlop :
                            mInitialMotionX - mTouchSlop;
                    moveDirection = dx > 0 ? 1 : 0;
                    if (DEBUG)
                        Log.d(WheelView.TAG, "moveeeeeee:   " + (dx > 0));
                }
                break;
        }

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (Math.abs(distanceX) >= mTouchSlop) {
            if (distanceX > 0.0f) {
                distanceX = distanceX - mTouchSlop;
            } else distanceX = distanceX + mTouchSlop;
        }
        if (DEBUG)
            Log.d(WheelView.TAG, "onScroll...: " + distanceX);
        wheelView.scrollBy((int) distanceX, 0);
        updateScrollStateIfRequired(WheelView.SCROLL_STATE_DRAGGING);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        updateScrollStateIfRequired(WheelView.SCROLL_STATE_SETTLING);
        return true;
    }


    private void updateScrollStateIfRequired(int newState) {
        if (newState == WheelView.SCROLL_STATE_IDLE) {
            if (DEBUG) {
                Log.d(WheelView.TAG, "updateScrollStateIfRequired: " + newState);
                Log.d(WheelView.TAG, "left: " + wheelView.getLeft());
                Log.d(WheelView.TAG, "scrollX: " + wheelView.getScrollX());
            }
            wheelView.autoSettle("SCROLL_STATE_IDLE", moveDirection);
        }
    }

}