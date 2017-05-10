package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by cylan-hunt on 16-9-28.
 */

public class GreatDragView extends FrameLayout {

    private static final String TAG = "GreatDragView";

    private ViewDragHelper viewDragHelper;

    private CardView draggedView;
    private int finalCount = 0;

    public GreatDragView(Context context) {
        this(context, null);
    }

    public GreatDragView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GreatDragView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDrag();
    }

    private float preDx;

    private void initDrag() {
        viewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child == draggedView;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                Log.d(TAG, "cancel");
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                //根据原型，第一张不能向右滑动。
                try {
                    if (dx > 0 && getChildCount() == 3) return left;
                    float rotationX = (float) dx / (getWidth() - child.getWidth() / 2);
                    if (preDx != dx) {
                        if (dx >= 0 && preDx < dx) {
                            preDx = dx;
//                        Log.d(TAG, "rationX right: " + dx + "  " + rotationX);
                            if (draggedView.getRotation() != rotationX)
                                draggedView.setRotation(-rotationX * 360);
                        } else if (dx < 0 && preDx > dx) {
                            Log.d(TAG, "rationX left: " + dx + "  " + rotationX);
                            preDx = dx;
                            if (draggedView.getRotation() != rotationX)
                                draggedView.setRotation(-rotationX * 360);
                        }
                    }
                    final int leftBound = -draggedView.getWidth();
                    final int rightBound = getWidth() + draggedView.getWidth();
                    return Math.min(Math.max(left, leftBound), rightBound);
                } catch (Exception e) {
                    return 0;
                }
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                final int topBound = -draggedView.getHeight();
                final int bottomBound = getMeasuredHeight();
                return Math.min(Math.max(top, topBound), bottomBound);
            }

        });
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 松手后，滑动回来.
     */
    private void smoothReset() {
        final int x = getMeasuredWidth() / 2 - draggedView.getMeasuredWidth() / 2;
        int y = getMeasuredHeight() / 2 - draggedView.getMeasuredHeight() / 2;
        if (viewDragHelper.smoothSlideViewTo(draggedView, x, y)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * @param right :加一个动画。
     */
    private void smoothSlideOut(boolean right) {
        final int x = right ? 2 * getMeasuredWidth() : -getMeasuredWidth();
        try {
            ObjectAnimator animator = ObjectAnimator.ofFloat(draggedView, "translationX", 0, x);
            animator.addListener(new AnimatorUtils.SimpleAnimationListener() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    removeView(draggedView);
                    post(() -> {
                        draggedView = (CardView) getChildAt(getChildCount() - 1);
//                        Log.d("GreatDragView", "GreatDragView； view： " + draggedView + " count: " + getChildCount());
                        if (viewDisappearListener != null) {
                            viewDisappearListener.onViewDisappear(draggedView, finalCount - getChildCount());
                        }
                    });
                }
            });
            animator.start();
        } catch (Exception e) {
        }

    }

    /**
     * 判断松开时的区域
     *
     * @return
     */
    private int getScrollDirection() {
        if (draggedView == null)
            return -1;
        boolean top = draggedView.getTop() >= -draggedView.getHeight()
                && draggedView.getTop() <= getMeasuredHeight();
        if (draggedView.getLeft() < 0) {
            //left
            if (draggedView.getLeft() >= -draggedView.getMeasuredWidth() / 3 && top)
                return 0;
            else {
                return 1;//left to disappear
            }
        } else if (draggedView.getRight() > getMeasuredWidth()) {
            if (draggedView.getRight() >= (getMeasuredWidth() + 1 / 3 * (float) getMeasuredWidth())) {
                return 2;//right to disappear
            } else return 0;
        }
        return 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if ((action != MotionEvent.ACTION_DOWN)) {
            viewDragHelper.cancel();
            return super.onInterceptTouchEvent(ev);
        }
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                final int result = getScrollDirection();
                Log.d("GreatDragView", "GreatDragView: direction:" + result);
                preDx = 0;
                if (result == 0) {
                    smoothReset();
                    draggedView.setRotation(0);
                } else {
                    smoothSlideOut(result == 2);
                }
                break;
        }
        viewDragHelper.processTouchEvent(ev);
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        finalCount = getChildCount();
        View v = getChildAt(getChildCount() - 1);
        if (v instanceof CardView) {
            draggedView = (CardView) v;
        }
    }

    private ViewDisappearListener viewDisappearListener;

    public void setViewDisappearListener(ViewDisappearListener viewDisappearListener) {
        this.viewDisappearListener = viewDisappearListener;
    }

    public interface ViewDisappearListener {
        void onViewDisappear(View view, int index);
    }

}
