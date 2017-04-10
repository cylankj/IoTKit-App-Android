package com.cylan.jiafeigou.widget.bell;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;


/**
 * Created by cylan-hunt on 16-8-9.
 */
public class DragLayout extends FrameLayout {

    private static final String TAG = "DragLayout";
    private ViewDragHelper viewDragHelper;
    private View draggedView;
    private LLView leftView;
    private LLView rightView;


    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public DragLayout(Context context, AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Resources.Theme theme = context.getTheme();
        /*
         * Look the appearance up without checking first if it exists because
         * almost every TextView has one and it greatly simplifies the logic
         * to be able to parse the appearance first and then let specific tags
         * for this View override it.
         */
        TypedArray a = theme.obtainStyledAttributes(attrs,
                R.styleable.LLViewStyle, defStyleAttr, defStyleAttr);
        a.recycle();
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
                settleView(child, left);
                final int leftBound = getPaddingLeft();
                final int rightBound = getWidth() - draggedView.getWidth();
                return Math.min(Math.max(left, leftBound), rightBound);
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                final int topBound = getPaddingTop();
                final int bottomBound = getHeight() - draggedView.getHeight() - draggedView.getPaddingBottom();
                return Math.min(Math.max(top, topBound), bottomBound);
            }

        });

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 3)
            throw new IllegalArgumentException("only three views");
        View v = getChildAt(2);
        if (v instanceof ImageView) {
            draggedView = v;
        }
        v = getChildAt(0);
        if (v instanceof LLView) {
            leftView = (LLView) v;
        }
        v = getChildAt(1);
        if (v instanceof LLView) {
            rightView = (LLView) v;
        }
    }

    public void doRingAnimation() {
        if (draggedView != null) {
//            draggedView.animate()
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        adjustAnimationView();
    }

    private void settleView(View child, final int left) {
        final boolean isLeftSide = getMeasuredWidth() / 2 > left + child.getMeasuredWidth() / 2;
        final int pivotX = getMeasuredWidth() / 2 - child.getMeasuredWidth() / 2;
        final int c = Math.abs(left - pivotX);
        float currentRatio = c * 1.0f / pivotX;
        if (currentRatio > 1.0f)
            return;
        if (isLeftSide) {
            leftView.setbORadiusRatio(currentRatio);
        } else {
            rightView.setbORadiusRatio(currentRatio);
        }
    }

    private void adjustAnimationView() {
        final int width = draggedView.getMeasuredWidth();
        final int height = draggedView.getMeasuredHeight();
        ViewGroup.LayoutParams p = leftView.getLayoutParams();
        p.width = width;
        p.height = height;
        leftView.requestLayout();
        p = rightView.getLayoutParams();
        p.width = width;
        p.height = height;
        rightView.requestLayout();
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            settleView(draggedView, draggedView.getLeft());
            ViewCompat.postInvalidateOnAnimation(this);
        }
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
                final int result = needScroll();
                if (result == -1) {
                    smoothScrollTo();
                } else {
                    if (onDragReleaseListener != null)
                        onDragReleaseListener.onRelease(result);
                }
                break;
        }
        viewDragHelper.processTouchEvent(ev);
        return true;
    }

    /**
     * 判断松开时的区域
     *
     * @return
     */
    private int needScroll() {
        if (draggedView.getLeft() <= getMeasuredWidth() / 2 - draggedView.getMeasuredWidth()
                && draggedView.getTop() <= getMeasuredHeight() / 2 + draggedView.getMeasuredHeight() * 3 / 2
                && draggedView.getTop() >= getMeasuredHeight() / 2 - draggedView.getMeasuredHeight() * 3 / 2)
            return 0;
        if (draggedView.getRight() >= getMeasuredWidth() / 2 + draggedView.getMeasuredWidth()
                && draggedView.getTop() <= getMeasuredHeight() / 2 + draggedView.getMeasuredHeight() * 3 / 2
                && draggedView.getTop() >= getMeasuredHeight() / 2 - draggedView.getMeasuredHeight() * 3 / 2)
            return 1;
        return -1;
    }

    /**
     * 松手后，滑动回来.
     */
    private void smoothScrollTo() {
        final int x = getMeasuredWidth() / 2 - draggedView.getMeasuredWidth() / 2;
        int y = getMeasuredHeight() / 2 - draggedView.getMeasuredHeight() / 2;
        if (viewDragHelper.smoothSlideViewTo(draggedView, x, y)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    private OnDragReleaseListener onDragReleaseListener;

    public void setOnDragReleaseListener(OnDragReleaseListener onDragReleaseListener) {
        this.onDragReleaseListener = onDragReleaseListener;
    }

    public interface OnDragReleaseListener {
        /**
         * @param side ：0 ：leFt  1：right
         */
        void onRelease(int side);
    }
}
