package com.cylan.jiafeigou.widget;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.cylan.jiafeigou.utils.DensityUtil;

public class PullToZoomListView extends ListView implements AbsListView.OnScrollListener {
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "PullToZoomListView";

    int mActivePointerId = -1;
    private FrameLayout mHeaderContainer;
    private int mHeaderHeight;

    public int getmHeaderHeight() {
        return mHeaderHeight;
    }

    public void setmHeaderHeight(int mHeaderHeight) {
        this.mHeaderHeight = mHeaderHeight;
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(DensityUtil.dip2px(mContext, AbsListView.LayoutParams.FILL_PARENT), mHeaderHeight);
        getHeaderContainer().setLayoutParams(lp);
    }

    private ImageView mHeaderImage;
    float mLastMotionY = -1.0F;
    float mLastScale = -1.0F;
    float mMaxScale = -1.0F;
    private ScalingRunnalable mScalingRunnalable;
    private int mScreenHeight;
    private ImageView mShadow;

    // 鑷畾涔夌殑headerview
    private View headerView;

    private Context mContext;

    private OnScrollStateChangeListener mScrollListener;

    public PullToZoomListView(Context paramContext) {
        super(paramContext);
        mContext = paramContext;
        init(paramContext);
    }

    public PullToZoomListView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        mContext = paramContext;
        init(paramContext);
    }

    public PullToZoomListView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        mContext = paramContext;
        init(paramContext);
    }

    private void endScraling() {
        if (this.mHeaderContainer.getBottom() >= this.mHeaderHeight)
            Log.d("mmm", "endScraling");
        this.mScalingRunnalable.startAnimation(200L);
    }

    private void init(Context paramContext) {

        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((Activity) paramContext).getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        this.mScreenHeight = localDisplayMetrics.heightPixels;
        this.mHeaderContainer = new FrameLayout(paramContext);

        this.mHeaderImage = new ImageView(paramContext);
        int i = localDisplayMetrics.widthPixels;
        setHeaderViewSize(i, (int) (9.0F * (i / 16.0F)));
        this.mShadow = new ImageView(paramContext);
        FrameLayout.LayoutParams localLayoutParams = new FrameLayout.LayoutParams(-1, -2);
        localLayoutParams.gravity = Gravity.CENTER;
        this.mShadow.setLayoutParams(localLayoutParams);
        this.mHeaderContainer.addView(this.mHeaderImage);
        this.mHeaderContainer.addView(this.mShadow);

        // addHeaderView(this.mHeaderContainer);

        this.mScalingRunnalable = new ScalingRunnalable();
        super.setOnScrollListener(this);
    }

    private void onSecondaryPointerUp(MotionEvent paramMotionEvent) {
        int i = (paramMotionEvent.getAction()) >> 8;
        if (paramMotionEvent.getPointerId(i) == this.mActivePointerId)
            if (i != 0) {
                int j = 1;
                this.mLastMotionY = paramMotionEvent.getY(0);
                this.mActivePointerId = paramMotionEvent.getPointerId(0);
                return;
            }
    }

    private void reset() {
        this.mActivePointerId = -1;
        this.mLastMotionY = -1.0F;
        this.mMaxScale = -1.0F;
        this.mLastScale = -1.0F;
    }

    public ImageView getHeaderView() {
        return this.mHeaderImage;
    }

    public FrameLayout getHeaderContainer() {
        return mHeaderContainer;
    }

    public void setHeaderView() {
        addHeaderView(this.mHeaderContainer);
    }


	/*
     * public boolean onInterceptTouchEvent(MotionEvent ev) {
	 *
	 * final int action = ev.getAction() & MotionEvent.ACTION_MASK; float
	 * mInitialMotionX= 0; float mLastMotionX= 0;
	 *
	 * float mInitialMotionY= 0; float mLastMotionY = 0;
	 *
	 * boolean isIntercept=false; switch (action) { case
	 * MotionEvent.ACTION_DOWN:
	 *
	 * mLastMotionY=ev.getY(); break;
	 *
	 * case MotionEvent.ACTION_MOVE: mInitialMotionY = ev.getY();
	 *
	 * if(Math.abs(mInitialMotionY-mLastMotionY)>50) { isIntercept=true; }
	 * break;
	 *
	 * }
	 *
	 * return isIntercept; }
	 */

    protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
        super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
        if (this.mHeaderHeight == 0)
            this.mHeaderHeight = this.mHeaderContainer.getHeight();
    }

    @Override
    public void onScroll(AbsListView paramAbsListView, int paramInt1, int paramInt2, int paramInt3) {
//        Log.d("mmm", "onScroll");
        float f = this.mHeaderHeight - this.mHeaderContainer.getBottom();
//        Log.d("mmm", "f|" + f);
        scroll(f);
        if ((f > 0.0F) && (f < this.mHeaderHeight)) {
//            Log.d("mmm", "1");
            int i = (int) (0.65D * f);
            this.mHeaderImage.scrollTo(0, -i);
        } else if (this.mHeaderImage.getScrollY() != 0) {
//            Log.d("mmm", "2");
            this.mHeaderImage.scrollTo(0, 0);
        }
        int maxHeght = DensityUtil.dip2px(mContext, 197);
        if (mTitleView != null) {
            mTitleView.setAlpha(1.0f - (scrollY() * (-1f) / maxHeght));
        }

    }


    public int scrollY() {
        View c = getChildAt(0);
        if (c == null) {
            return 0;
        }
        return c.getTop();
    }

    public void onScrollStateChanged(AbsListView paramAbsListView, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (paramAbsListView.getLastVisiblePosition() == paramAbsListView.getCount() - 1) {
                if (mScrollListener != null)
                    mScrollListener.onStartLoadMore();
            }
        }
    }

    public boolean onTouchEvent(MotionEvent paramMotionEvent) {
//        Log.d("mmm", "" + (0xFF & paramMotionEvent.getAction()));
        switch (0xFF & paramMotionEvent.getAction()) {
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_DOWN:
                if (!this.mScalingRunnalable.mIsFinished) {
                    this.mScalingRunnalable.abortAnimation();
                }
                this.mLastMotionY = paramMotionEvent.getY();
                this.mActivePointerId = paramMotionEvent.getPointerId(0);
                this.mMaxScale = (this.mScreenHeight / this.mHeaderHeight);
                this.mLastScale = (this.mHeaderContainer.getBottom() / this.mHeaderHeight);
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d("mmm", "mActivePointerId" + mActivePointerId);
                int j = paramMotionEvent.findPointerIndex(this.mActivePointerId);
                if (j == -1) {
//                    Log.e("PullToZoomListView", "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                } else {
                    if (this.mLastMotionY == -1.0F)
                        this.mLastMotionY = paramMotionEvent.getY(j);
                    if (this.mHeaderContainer.getBottom() >= this.mHeaderHeight) {
                        ViewGroup.LayoutParams localLayoutParams = this.mHeaderContainer.getLayoutParams();
                        float f = ((paramMotionEvent.getY(j) - this.mLastMotionY + this.mHeaderContainer.getBottom()) / this.mHeaderHeight - this.mLastScale)
                                / 2.0F + this.mLastScale;
                        if ((this.mLastScale <= 1.0D) && (f < this.mLastScale)) {
                            localLayoutParams.height = this.mHeaderHeight;
                            this.mHeaderContainer.setLayoutParams(localLayoutParams);
                            return super.onTouchEvent(paramMotionEvent);
                        }
                        this.mLastScale = Math.min(Math.max(f, 1.0F), this.mMaxScale);
                        localLayoutParams.height = ((int) (this.mHeaderHeight * this.mLastScale));
                        if (localLayoutParams.height < this.mScreenHeight)
                            this.mHeaderContainer.setLayoutParams(localLayoutParams);
                        this.mLastMotionY = paramMotionEvent.getY(j);
                        return true;
                    }
                    this.mLastMotionY = paramMotionEvent.getY(j);
                }
                break;
            case MotionEvent.ACTION_UP:
                reset();
                endScraling();
                break;
            case MotionEvent.ACTION_CANCEL:
                int i = paramMotionEvent.getActionIndex();
                this.mLastMotionY = paramMotionEvent.getY(i);
                this.mActivePointerId = paramMotionEvent.getPointerId(i);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                onSecondaryPointerUp(paramMotionEvent);

                try {
                    this.mLastMotionY = paramMotionEvent.getY(paramMotionEvent.findPointerIndex(this.mActivePointerId));
                } catch (IllegalArgumentException e) {
                    // TODO: handle exception
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
        }
        return super.onTouchEvent(paramMotionEvent);
    }

    public void setHeaderViewSize(int paramInt1, int paramInt2) {
        Object localObject = this.mHeaderContainer.getLayoutParams();
        if (localObject == null)
            localObject = new AbsListView.LayoutParams(paramInt1, paramInt2);
        ((ViewGroup.LayoutParams) localObject).width = paramInt1;
        ((ViewGroup.LayoutParams) localObject).height = paramInt2;
        this.mHeaderContainer.setLayoutParams((ViewGroup.LayoutParams) localObject);
        this.mHeaderHeight = paramInt2;
    }

    public void setShadow(int paramInt) {
        this.mShadow.setBackgroundResource(paramInt);
    }

    class ScalingRunnalable implements Runnable {
        long mDuration;
        boolean mIsFinished = true;
        float mScale;
        long mStartTime;

        private Interpolator sInterpolator = new Interpolator() {
            public float getInterpolation(float paramAnonymousFloat) {
                float f = paramAnonymousFloat - 1.0F;
                return 1.0F + f * (f * (f * (f * f)));
            }
        };


        ScalingRunnalable() {
        }

        public void abortAnimation() {
            this.mIsFinished = true;
        }

        public boolean isFinished() {
            return this.mIsFinished;
        }

        public void run() {
            float f2;
            ViewGroup.LayoutParams localLayoutParams;
            if ((!this.mIsFinished) && (this.mScale > 1.0D)) {
                float f1 = ((float) SystemClock.currentThreadTimeMillis() - (float) this.mStartTime) / (float) this.mDuration;
                f2 = this.mScale - (this.mScale - 1.0F) * sInterpolator.getInterpolation(f1);
                localLayoutParams = PullToZoomListView.this.mHeaderContainer.getLayoutParams();
                if (f2 > 1.0F) {
//                    Log.d("run", "f2>1.0");
                    localLayoutParams.height = PullToZoomListView.this.mHeaderHeight;
                    localLayoutParams.height = ((int) (f2 * PullToZoomListView.this.mHeaderHeight));
                    PullToZoomListView.this.mHeaderContainer.setLayoutParams(localLayoutParams);
                    PullToZoomListView.this.post(this);
                    return;
                }
                this.mIsFinished = true;
            }
        }

        public void startAnimation(long paramLong) {
            this.mStartTime = SystemClock.currentThreadTimeMillis();
            this.mDuration = paramLong;
            this.mScale = ((float) (PullToZoomListView.this.mHeaderContainer.getBottom()) / PullToZoomListView.this.mHeaderHeight);
            this.mIsFinished = false;
            PullToZoomListView.this.post(this);
        }

        public void stopAnimation() {
            PullToZoomListView.this.removeCallbacks(this);
            this.mIsFinished = true;
        }

    }

    public interface OnScrollStateChangeListener {
        void onStartPull(float fromDegress, float toDegress);

        void onStartRefresh();

        void onStartLoadMore();
    }

    public void setOnScrollStateChangeListener(OnScrollStateChangeListener osscl) {
        mScrollListener = osscl;

    }

    private float fromDegress = 0;

    private void scroll(float f) {
        if (mScrollListener != null) {
            if (f <= 0.0) {
                if (Math.abs(f) > 80) {
                    mScrollListener.onStartPull(fromDegress, f);
                } else if (Math.abs(f) < 20) {
                    mScrollListener.onStartRefresh();
                }
                fromDegress = f;
            }

        }
    }

    public void stopRunnable() {
        if (mScalingRunnalable != null) {
            mScalingRunnalable.stopAnimation();
            mScalingRunnalable = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopRunnable();
    }

    private View mTitleView;

    public void setTitleView(View view) {
        this.mTitleView = view;
    }

}
