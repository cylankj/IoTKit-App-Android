package com.cylan.jiafeigou.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.support.DswLog;

@SuppressLint("NewApi")
public class RefreshListView extends ListView implements OnScrollListener {

    private static final int REFRESH_DONE = -1;
    private static final int PULL_TO_REFRESH = 2;
    private static final int RELEASE_TO_REFRESH = 3;
    private static final int REFRESHING = 4;

    private static final int MSG_OVERTIME = 0;
    private static final int MSG_COMPLETE = 1;

    private static final long DURATION_OVERTIME = 30000;

    private static final String TAG = "PullToRefreshListView";

    private OnRefreshListener mOnRefreshListener;
    private OnScrollListener mOnScrollListener;
    private LayoutInflater mInflater;

    private RelativeLayout mRefreshView;
    private TextView mRefreshViewText;
    private ImageView mRefreshViewImage;
    private ProgressBar mRefreshViewProgress;
    private TextView mRefreshViewLastUpdated;
    private TextView mOverTimeView;

    private int mCurrentScrollState;
    private int mRefreshState;

    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;

    private int mRefreshViewHeight;
    private int mRefreshOriginalTopPadding;
    private int mLastMotionY;
    private int mOffSetDiff;

    private boolean mBounceHack;

    private boolean mRefreshEnabled = true;
    private boolean mOverTimeEnable = false;

    public void setRefreshEnabled(boolean enabled) {
        mRefreshEnabled = enabled;
    }

    public boolean getRefreshEnabled() {
        return mRefreshEnabled;
    }

    public void setOverTime(boolean enable) {
        mOverTimeEnable = enable;
    }

    public RefreshListView(Context context) {
        super(context);
        init(context);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @SuppressLint("NewApi")
    private void init(Context context) {
        mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator linearinterpolator = new LinearInterpolator();
        mFlipAnimation.setInterpolator(linearinterpolator);
        mFlipAnimation.setDuration(300);
        mFlipAnimation.setFillAfter(true);
        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(linearinterpolator);
        mReverseFlipAnimation.setDuration(300);
        mReverseFlipAnimation.setFillAfter(true);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mRefreshView = (RelativeLayout) mInflater.inflate(R.layout.pull_to_refresh_header, this, false);
        mRefreshViewText = (TextView) mRefreshView.findViewById(R.id.pull_to_refresh_text);
        mRefreshViewImage = (ImageView) mRefreshView.findViewById(R.id.pull_to_refresh_image);
        mRefreshViewProgress = (ProgressBar) mRefreshView.findViewById(R.id.pull_to_refresh_progress);
        mRefreshViewLastUpdated = (TextView) mRefreshView.findViewById(R.id.pull_to_refresh_updated_at);
        mOverTimeView = (TextView) mRefreshView.findViewById(R.id.over_time);

        mRefreshViewImage.setMinimumHeight(50);
        mRefreshOriginalTopPadding = mRefreshView.getPaddingTop();

        mRefreshState = REFRESHING;

        addHeaderView(mRefreshView);

        super.setOnScrollListener(this);

        measureView(mRefreshView);
        mRefreshViewHeight = mRefreshView.getMeasuredHeight();
        try {
            setOverScrollMode(AdapterView.OVER_SCROLL_NEVER);
        } catch (NoSuchMethodError e) {
            DswLog.ex(e.toString());
        }
        mOffSetDiff = (int) (mRefreshOriginalTopPadding + 10 * getResources().getDisplayMetrics().density);
        resetHeader();
    }

    // @Override
    // protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
    // boolean clampedY) {
    // android.util.Log.i("jesus", "onOverScrolled " + scrollX + ":"
    // + clampedX + ":" + scrollY + ":" + clampedY);
    //
    // if (scrollY == 0 && clampedY) {
    // if (getFirstVisiblePosition() == 0) {
    // mRefreshViewImage.setVisibility(View.VISIBLE);
    // mRefreshViewLastUpdated.setVisibility(View.VISIBLE);
    // mRefreshViewText.setVisibility(View.VISIBLE);
    // if ((mRefreshView.getBottom() >= mRefreshViewHeight + 20 || mRefreshView
    // .getTop() >= 0) && mRefreshState != RELEASE_TO_REFRESH) {
    // mRefreshViewText.setText(R.string.refresh_release);
    // mRefreshViewImage.clearAnimation();
    // mRefreshViewImage.startAnimation(mFlipAnimation);
    // mRefreshState = RELEASE_TO_REFRESH;
    // } else if (mRefreshView.getBottom() < mRefreshViewHeight + 20
    // && mRefreshState != PULL_TO_REFRESH) {
    // mRefreshViewText.setText(R.string.refresh_pull);
    // if (mRefreshState != REFRESH_DONE) {
    // mRefreshViewImage.clearAnimation();
    // mRefreshViewImage.startAnimation(mReverseFlipAnimation);
    // }
    // mRefreshState = PULL_TO_REFRESH;
    // }
    // }
    // }
    // super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    // }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        mOnScrollListener = l;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    boolean flag = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mRefreshEnabled || mRefreshState == REFRESHING) {
            return super.onTouchEvent(event);
        }
        mBounceHack = false;
        final int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (getFirstVisiblePosition() == 0 && mRefreshState != REFRESHING) {
                    if (mRefreshView.getPaddingTop() >= mOffSetDiff && mRefreshState == RELEASE_TO_REFRESH) {
                        onRefresh();
                        mOverTimeView.setVisibility(View.GONE);
                        Log.e("big", "send refresh");
                        mHandler.sendEmptyMessageDelayed(MSG_OVERTIME, DURATION_OVERTIME);
                    } else if (mRefreshView.getBottom() < mRefreshViewHeight || mRefreshView.getTop() <= 0) {
                        resetHeader();
                        setSelection(0);
                    }
                }
                flag = false;
                break;
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = (int) event.getY();
                int x = (int) event.getX();
                if (getFirstVisiblePosition() == 0 && !flag) {
                    flag = !flag;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (y - mLastMotionY < mOffSetDiff) {
                    break;
                }
                applyHeaderPadding(event);
                break;
        }

        return super.onTouchEvent(event);
    }

    private void applyHeaderPadding(MotionEvent ev) {
        if (!flag) {
            return;
        }
        int offset = mRefreshView.getPaddingTop() + mRefreshView.getTop();
        if (getFirstVisiblePosition() == 0) {
            mRefreshViewImage.setVisibility(View.VISIBLE);
            mRefreshViewLastUpdated.setVisibility(View.VISIBLE);
            mRefreshViewText.setVisibility(View.VISIBLE);
            if (offset >= mOffSetDiff && mRefreshState != RELEASE_TO_REFRESH) {
                mRefreshViewProgress.setVisibility(View.GONE);
                mRefreshViewText.setText(R.string.RELEASE_TO_REFRESH);
                mRefreshViewImage.clearAnimation();
                mRefreshViewImage.startAnimation(mFlipAnimation);
                mRefreshState = RELEASE_TO_REFRESH;
            } else if (offset <= mOffSetDiff && mRefreshState != PULL_TO_REFRESH) {
                mRefreshViewProgress.setVisibility(View.GONE);
                mRefreshViewText.setText(R.string.PULL_TO_REFRESH);
                if (mRefreshState != REFRESH_DONE) {
                    mRefreshViewImage.clearAnimation();
                    mRefreshViewImage.startAnimation(mReverseFlipAnimation);
                }
                mRefreshState = PULL_TO_REFRESH;
            }
        } else {
            resetHeader();
        }

        int topPadding = (int) (((ev.getY() - mLastMotionY) - mRefreshViewHeight) / 3);
        topPadding = topPadding < 0 ? mRefreshOriginalTopPadding : topPadding;
        mRefreshView.setPadding(mRefreshView.getPaddingLeft(), topPadding, mRefreshView.getPaddingRight(), mRefreshView.getPaddingBottom());

    }

    private void resetHeaderPadding() {
        mRefreshView.setPadding(mRefreshView.getPaddingLeft(), mRefreshOriginalTopPadding, mRefreshView.getPaddingRight(), mRefreshView.getPaddingBottom());
    }

    private void resetHeader() {
        if (mRefreshState != REFRESH_DONE) {
            mRefreshState = REFRESH_DONE;
            // if (!isVerticalScrollBarEnabled()) {
            // setVerticalScrollBarEnabled(true);
            // }
            resetHeaderPadding();
            mRefreshViewImage.setImageResource(R.drawable.ic_pulldown_refresh);
            mRefreshViewImage.clearAnimation();
            mRefreshViewImage.setVisibility(View.GONE);
            mRefreshViewProgress.setVisibility(View.GONE);
            mRefreshViewText.setVisibility(View.GONE);
            mRefreshViewLastUpdated.setVisibility(View.GONE);
        }
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL && mRefreshState != REFRESHING) {
            /*
			 * if (firstVisibleItem == 0) {
			 * mRefreshViewImage.setVisibility(View.VISIBLE);
			 * mRefreshViewLastUpdated.setVisibility(View.VISIBLE);
			 * mRefreshViewText.setVisibility(View.VISIBLE); if
			 * ((mRefreshView.getBottom() >= mRefreshViewHeight + 20 ||
			 * mRefreshView .getTop() >= 0) && mRefreshState !=
			 * RELEASE_TO_REFRESH) {
			 * mRefreshViewText.setText(R.string.refresh_release);
			 * mRefreshViewImage.clearAnimation();
			 * mRefreshViewImage.startAnimation(mFlipAnimation); mRefreshState =
			 * RELEASE_TO_REFRESH; } else if (mRefreshView.getBottom() <
			 * mRefreshViewHeight + 20 && mRefreshState != PULL_TO_REFRESH) {
			 * mRefreshViewText.setText(R.string.refresh_pull); if
			 * (mRefreshState != REFRESH_DONE) {
			 * mRefreshViewImage.clearAnimation();
			 * mRefreshViewImage.startAnimation(mReverseFlipAnimation); }
			 * mRefreshState = PULL_TO_REFRESH; } } else { resetHeader(); }
			 */
        } else if (mCurrentScrollState == SCROLL_STATE_FLING && firstVisibleItem == 0 && mRefreshState != REFRESHING) {
            setSelection(0);
            mBounceHack = true;
        } else if (mBounceHack && mCurrentScrollState == SCROLL_STATE_FLING) {
            setSelection(0);
        }
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mCurrentScrollState = scrollState;

        if (mCurrentScrollState == SCROLL_STATE_IDLE) {
            mBounceHack = false;
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    public void prepareForRefresh() {
        resetHeaderPadding();

        mRefreshViewImage.setVisibility(View.GONE);
        mRefreshViewImage.setImageDrawable(null);
        mRefreshViewLastUpdated.setVisibility(View.GONE);
        mRefreshViewProgress.setVisibility(View.VISIBLE);
        mRefreshViewText.setVisibility(View.VISIBLE);
        mRefreshViewText.setText(R.string.VIDEO_REFRESHING);

        mRefreshState = REFRESHING;
    }

    public void onRefresh() {
        Log.d(TAG, "onRefresh");
        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }

        prepareForRefresh();
    }

    public void onRefreshComplete() {
        Log.d(TAG, "onRefreshComplete");
        mHandler.removeMessages(MSG_OVERTIME);
        mHandler.sendEmptyMessage(MSG_COMPLETE);
        if (mRefreshView.getBottom() > 0) {
            invalidateViews();
            setSelection(0);
        }
        resetHeader();
    }

    private class OnClickRefreshListener implements OnClickListener {

        public void onClick(View v) {
            if (mRefreshState != REFRESHING) {
                prepareForRefresh();
                onRefresh();
            }
        }

    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_OVERTIME:
                    if (!mOverTimeEnable)
                        return;
                    Log.e("big", "MSG_OVERTIME");
                    //mOverTimeView.setVisibility(View.VISIBLE);
                    if (mRefreshView.getBottom() > 0) {
                        invalidateViews();
                        setSelection(0);
                    }
                    resetHeader();
                    break;
                case MSG_COMPLETE:
                    if (!mOverTimeEnable)
                        return;
                    mOverTimeView.setVisibility(View.GONE);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void setOverTimeViewVisibity(Boolean boo) {
        mOverTimeView.setVisibility(boo ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRefreshView.clearAnimation();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && previouslyFocusedRect != null) {
            final ListAdapter adapter = getAdapter();
            final int count = adapter.getCount();
            switch (direction) {
                case FOCUS_DOWN:
                    for (int i = 0; i < count; i++) {
                        if (!adapter.isEnabled(i)) {
                            continue;
                        }
                        setSelection(i);
                        break;
                    }
                    break;
                case FOCUS_UP:
                    for (int i = count - 1; i >= 0; i--) {
                        if (!adapter.isEnabled(i)) {
                            continue;
                        }
                        setSelection(i);
                        break;
                    }
                    break;
                default:
                    break;
            }
        }
    }
}