package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Animatable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan on 2015/10/8.
 */
public class ViewDrag extends RelativeLayout {

    private static final String TAG = "ViewDrag";

    private ViewDragHelper dragger;
    private View mDragView;
    private View iv_doorbell;
    private View redDotsLayout;
    private View greenDotsLayout;
    private ScaleImageView redDots;
    private ImageView redLighting;
    private ScaleImageView greenDots;
    private ImageView greenLighting;
    private Point point = new Point();
    private Point right = new Point();
    private Point left = new Point();

    private boolean mDragVertical = true;
    private boolean isDraging = false;
    private int distance;

    private static final int DRAG_RIGHT = 0;
    private static final int DRAG_LEFT = 1;
    private static final int NONE = -1;
    private int state = -1;

    private Drag2RightOrLeftListener mListening;

    /**
     * 设置子控件是否可以上下移动
     *
     * @param dragVertical default true
     */
    public void setDragVertical(boolean dragVertical) {
        mDragVertical = dragVertical;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            dragger.cancel();
            return false;
        }
        return dragger.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragger.processTouchEvent(event);
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_UP:
                if (mDragView.getX() >= 0 && distance < 2 * mDragView.getWidth() / 3 && isDraging) {
                    if (mListening != null) {
                        Log.i("ViewDrag", "Drag2Left");
                        mListening.dragLeft();
                    }
                } else if (distance < getWidth() && distance >= getWidth() - 1.5 * mDragView.getWidth()) {
                    if (mListening != null) {
                        Log.i("ViewDrag", "Drag2Right");
                        mListening.dragRight();
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                if (isDraging) {
                    startBellAnim1(false);
                    alphaAnimation(redLighting);
                    alphaAnimation(greenLighting);
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (dragger.continueSettling(true)) {
            invalidate();
        }
    }

    public ViewDrag(Context context, AttributeSet attrs) {
        super(context, attrs);
        dragger = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View view, int i) {
                return view == mDragView;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                if (mDragVertical) {
                    return top;
                }
                return super.clampViewPositionVertical(child, top, dy);
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                final int leftBound = getPaddingLeft();
                final int rightBound = getWidth() - mDragView.getWidth();
                final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
                distance = newLeft;
                float bound = rightBound / 2;
                float scaleRate = (Math.abs(left - bound) / bound) * 2.5f + 1.0f;
                if (newLeft >= 0 && newLeft < rightBound / 2) {
                    redDots.setDragMatrix(scaleRate);
                } else if (newLeft > rightBound / 2 && newLeft <= rightBound) {
                    greenDots.setDragMatrix(scaleRate);
                }
//                Log.d("ViewDrag", "left:" + left + "\tbound:" + bound + "\tscaleRate:" + scaleRate);

                return newLeft;
            }

            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId) {
                dragger.captureChildView(mDragView, pointerId);
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                if (releasedChild == mDragView) {
                    if (distance > 0 && distance < 2 * mDragView.getWidth() / 3) {
                        state = DRAG_LEFT;
                    } else if (distance < getWidth()
                            && distance >= getWidth() - 1.5 * mDragView.getWidth()) {
                        state = DRAG_RIGHT;
                    } else {
                        state = NONE;
                    }
                    switch (state) {
                        case DRAG_LEFT:
                            dragger.settleCapturedViewAt(left.x, left.y);
                            break;
                        case DRAG_RIGHT:
                            dragger.settleCapturedViewAt(right.x, right.y);
                            break;
                        case NONE:
                            dragger.settleCapturedViewAt(point.x, point.y);
                            break;
                        default:
                            dragger.settleCapturedViewAt(point.x, point.y);
                            break;
                    }
                    invalidate();
                }
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            }

            @Override
            public void onViewDragStateChanged(int state) {
                super.onViewDragStateChanged(state);
                switch (state) {
                    case ViewDragHelper.STATE_SETTLING:
                        break;
                    case ViewDragHelper.STATE_DRAGGING:
                        isDraging = true;
                        break;
                    case ViewDragHelper.STATE_IDLE:
                        startBellAnim1(true);
                        redLighting.setVisibility(VISIBLE);
                        greenLighting.setVisibility(VISIBLE);
                        redDots.resetMatrix();
                        greenDots.resetMatrix();
                        isDraging = false;
                        break;
                    default:
                        break;
                }
            }
        });
    dragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDragView = findViewById(R.id.ico_doorbell_layout);
        iv_doorbell = findViewById(R.id.ico_doorbell);
        redDots = (ScaleImageView) findViewById(R.id.bg_red_dots);
        redLighting = (ImageView) findViewById(R.id.red_lighting);
        greenDots = (ScaleImageView) findViewById(R.id.bg_green_dots);
        greenLighting = (ImageView) findViewById(R.id.green_lighting);
        redDotsLayout = findViewById(R.id.red_dots_layout);
        greenDotsLayout = findViewById(R.id.green_dots_layout);

        ((Animatable) redLighting.getDrawable()).start();
        ((Animatable) greenLighting.getDrawable()).start();


        startBellAnim1(true);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        point.x = mDragView.getLeft();
        point.y = mDragView.getTop();
        left.x = redDotsLayout.getLeft();
        left.y = redDotsLayout.getTop() + mDragView.getWidth();
        right.x = greenDotsLayout.getLeft();
        right.y = greenDotsLayout.getTop() + mDragView.getWidth();
    }

    public interface Drag2RightOrLeftListener {
        void dragRight();

        void dragLeft();
    }

    public void setDrag2RightOrLeftListening(Drag2RightOrLeftListener listening) {
        mListening = listening;
    }


    private void startBellAnim1(final boolean isStart) {

        RotateAnimation mRotateAnimation = new RotateAnimation(0f, -30f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.ABSOLUTE, 10);
        mRotateAnimation.setStartOffset(0);
        mRotateAnimation.setDuration(90);
        mRotateAnimation.setRepeatCount(0);
        mRotateAnimation.setInterpolator(new DecelerateInterpolator());
        mRotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isStart)
                    startBellAnim2();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv_doorbell.startAnimation(mRotateAnimation);
    }


    private void startBellAnim2() {

        RotateAnimation mRotateAnimation = new RotateAnimation(0f, 30f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.ABSOLUTE, 10);
        mRotateAnimation.setStartOffset(0);
        mRotateAnimation.setDuration(90);
        mRotateAnimation.setRepeatCount(0);
        mRotateAnimation.setInterpolator(new DecelerateInterpolator());
        mRotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startBellAnim1(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv_doorbell.startAnimation(mRotateAnimation);
    }


    private void alphaAnimation(View view) {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.1f);
        animation.setDuration(50);
        view.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                redLighting.setVisibility(INVISIBLE);
                greenLighting.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

}
