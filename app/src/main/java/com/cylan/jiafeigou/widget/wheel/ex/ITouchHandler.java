package com.cylan.jiafeigou.widget.wheel.ex;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.OvershootInterpolator;
import android.widget.OverScroller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by cylan-hunt on 16-6-18.
 */

public class ITouchHandler extends GestureDetector.SimpleOnGestureListener {
    /**
     * Indicates that the pager is in an idle, settled state. The current page
     * is fully in view and no animation is in progress.
     */
    public static final int SCROLL_STATE_IDLE = 0;

    /**
     * Indicates that the pager is currently being dragged by the user.
     */
    public static final int SCROLL_STATE_DRAGGING = 1;

    /**
     * Indicates that the pager is in the process of settling to a final position.
     */
    public static final int SCROLL_STATE_SETTLING = 2;

    public static final String TAG = "SuperWheel:";

    private SuperWheelExt superWheel;
    private GestureDetectorCompat gestureDetectorCompat;
    private OverScroller scroller;
    private int mTouchSlop;

    /**
     * 判定为拖动的最小移动像素数
     */

    public ITouchHandler(SuperWheelExt superWheel) {
        this.superWheel = superWheel;
        Context context = superWheel.getContext();
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

//    public static final int DIRECTION_LEFT = 1;
//    public static final int DIRECTION_RIGHT = 0;

    private
    @MoveDirection
    int moveDirection = -1;//-1 nothing ,0:left ,1:right

    private boolean isActionUp = false;
    private boolean isTouchDonw = false;
    private
    @DragOrFling
    int dragOrFling = -1;//-1 nothing ,0:drag ,1:fling

    boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        final int mask = event.getAction() & MotionEventCompat.ACTION_MASK;

        // Always take care of the touch gesture being complete.
        if (mask == MotionEvent.ACTION_CANCEL || mask == MotionEvent.ACTION_UP) {
            // Release the drag.
            isActionUp = true;
            isTouchDonw = false;
            mActivePointerId = INVALID_POINTER;
            boolean isFinish = scroller.isFinished();
            if (SuperWheelExt.DEBUG)
                Log.d(TAG, "onTouchEvent: up: " + "," + isFinish);
            if (isFinish) {
                moveDirection = MoveDirection.NONE;
                updateScrollStateIfRequired(SCROLL_STATE_IDLE);
            }
            if (SuperWheelExt.DEBUG)
                Log.d(TAG, "onTouchEvent: up: " + "," + isFinish);
            return true;
        }
        switch (mask) {
            case MotionEvent.ACTION_DOWN:
                isActionUp = false;
                isTouchDonw = true;
                if (!scroller.isFinished())
                    scroller.abortAnimation();
                moveDirection = MoveDirection.NONE;
                dragOrFling = DragOrFling.NONE;
                       /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionX = mInitialMotionX = event.getX();
                if (SuperWheelExt.DEBUG)
                    Log.d(TAG, "onTouchEvent: ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                isTouchDonw = true;
                if (superWheel.getParent() != null) {
                    superWheel.getParent().requestDisallowInterceptTouchEvent(true);
                }
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }
                if (SuperWheelExt.DEBUG)
                    Log.d(TAG, "onTouchEvent: ACTION_MOVE");
                final int pointerIndex = MotionEventCompat.findPointerIndex(event, activePointerId);
                final float x = MotionEventCompat.getX(event, pointerIndex);
                final float dx = x - mLastMotionX;
                final float xDiff = Math.abs(dx);
                if (xDiff > mTouchSlop) {
                    mLastMotionX = dx > 0 ? mInitialMotionX + mTouchSlop :
                            mInitialMotionX - mTouchSlop;
                    moveDirection = dx > 0 ? MoveDirection.LEFT : MoveDirection.RIGHT;
                }
                dragOrFling = DragOrFling.DRAGGING;
                updateScrollStateIfRequired(SCROLL_STATE_DRAGGING);
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
                isActionUp = false;
                updateScrollStateIfRequired(SCROLL_STATE_IDLE);
                if (SuperWheelExt.DEBUG)
                    Log.d(TAG, "computeScroll finish");
            }
        }
    }

    public boolean isFinished() {
        return scroller == null || scroller.isFinished();
    }

    public void startSmoothScroll(int startX, int dx) {
        scroller.startScroll(startX, 0, dx, 0);
        superWheel.invalidate();
        dragOrFling = DragOrFling.NONE;//清空fling标志.
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (Math.abs(distanceX) >= mTouchSlop) {
            if (distanceX > 0.0f) {
                distanceX = distanceX - mTouchSlop;
            } else distanceX = distanceX + mTouchSlop;
        }
        superWheel.scrollBy((int) distanceX, 0);
        if (SuperWheelExt.DEBUG) {
            Log.d(TAG, "onScroll...: " + distanceX + " " + superWheel.getScrollX());
        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (SuperWheelExt.DEBUG) {
            Log.d(TAG, "fling:  " + superWheel.getScrollX() + " " + superWheel.getMaxScrollX());
        }
        dragOrFling = DragOrFling.FLING;
        scroller.fling(superWheel.getScrollX(), 0,
                (int) -velocityX, 0,
                -superWheel.getMaxScrollX(), 0,
                0, 0);
        superWheel.invalidate();
        return true;
    }


    private void updateScrollStateIfRequired(int newState) {
        Log.d(TAG, "updateScroll:" + dragOrFling + " state:" + newState + " moveDirection:" + moveDirection);
        superWheel.autoSettle(newState, moveDirection);
    }

    @IntDef({
            MoveDirection.NONE,
            MoveDirection.LEFT,
            MoveDirection.RIGHT
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface MoveDirection {
        int NONE = -1;
        int LEFT = 0;
        int RIGHT = 1;
    }

    @IntDef({
            DragOrFling.NONE,
            DragOrFling.DRAGGING,
            DragOrFling.FLING
    })
    //-1 nothing ,0:drag ,1:fling
    @Retention(RetentionPolicy.SOURCE)
    public @interface DragOrFling {
        int NONE = -1;
        int DRAGGING = 0;
        int FLING = 1;
    }

    public boolean isTouchDown() {
        return isTouchDonw;
    }

}