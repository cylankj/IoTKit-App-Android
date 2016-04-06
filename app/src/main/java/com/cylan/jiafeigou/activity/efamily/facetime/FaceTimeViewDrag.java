package com.cylan.jiafeigou.activity.efamily.facetime;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;

/**
 * Created by yangc on 2015/12/8.
 *
 */
public class FaceTimeViewDrag extends RelativeLayout {

    private ViewDragHelper mViewDragHelper;

    private View mFrontCamera;
    private View mRemoteCamera;

    public FaceTimeViewDrag(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View view, int i) {
                return view == mFrontCamera;
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return getMeasuredWidth()-child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return getMeasuredHeight()-child.getMeasuredHeight();
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                int leftBound = getPaddingLeft();
                int rightBound = getWidth() - mFrontCamera.getWidth() - leftBound;
                int newLeft = Math.min(Math.max(left, leftBound), rightBound);
                return newLeft;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                int topBount = getPaddingTop();
                int buttom = getHeight() - mFrontCamera.getHeight() - topBount;
                int newTop = Math.min(Math.max(top, topBount), buttom);
                return newTop;
            }
        });
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true))
            invalidate();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mFrontCamera = findViewById(R.id.face_local_view);
        mRemoteCamera = findViewById(R.id.face_remote_view);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP){
            mViewDragHelper.cancel();
            return false;
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
}
