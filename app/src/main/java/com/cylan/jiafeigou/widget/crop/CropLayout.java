package com.cylan.jiafeigou.widget.crop;

import android.content.Context;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hds on 17-11-15.
 */

public class CropLayout extends FrameLayout {
    private static final String TAG = "CropLayout";

    private int l = 0, t = 0, r = 0, b = 0;
    private int mTouchSlop;
    private int currentCorner = -1;
    private float preTouchX = -1;
    private boolean canBeExpand = false;

    private Shaper shapper;
    private ViewDragHelper dragHelper;
    private float mCornerRadius;

    public CropLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public CropLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CropLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        shapper = (Shaper) getChildAt(0);
    }

    private void init() {
        final ViewConfiguration vc = ViewConfiguration.get(getContext());
        mTouchSlop = vc.getScaledTouchSlop() * 3;
        mCornerRadius = getContext().getResources().getDimensionPixelSize(R.dimen.y30);
        dragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                shapper = (Shaper) child;
                return true;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                LayoutParams lp = (LayoutParams) releasedChild.getLayoutParams();
                lp.gravity = Gravity.NO_GRAVITY;
                lp.setMargins(releasedChild.getLeft(), releasedChild.getTop(), 0, 0);
                releasedChild.setLayoutParams(lp);
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                final int leftBound = getPaddingLeft();
                final int rightBound = getWidth() - getChildView().getWidth();
                return Math.min(Math.max(left, leftBound), rightBound);
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                final int topBound = getPaddingTop();
                final int bottomBound = getHeight() - getChildView().getHeight() - getChildView().getPaddingBottom();
                return Math.min(Math.max(top, topBound), bottomBound);
            }

        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        ensureShapper(x, y);
        if (shapper != null && handleExpand(x, y, event)) {
            //处理 缩放
            return true;
        }
        dragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    /**
     * 手指在四个角
     *
     * @param x
     * @param y
     * @return
     */
    private int inCorner(float x, float y) {
        RectF rectF = shapper.getCornerRects();
        rectF.left = shapper.getShaper().getLeft();
        rectF.top = shapper.getShaper().getTop();
        rectF.right = shapper.getShaper().getRight();
        rectF.bottom = shapper.getShaper().getBottom();
        //left top
        float leftCorner = Math.abs(x - rectF.left);
        float rightCorner = Math.abs(x - rectF.right);
        float topCorner = Math.abs(y - rectF.top);
        float bottomCorner = Math.abs(y - rectF.bottom);

        if (leftCorner < mCornerRadius && topCorner < mCornerRadius) {
            Log.d(TAG, "0  corner");
            return 0;
        } else if (rightCorner < mCornerRadius && topCorner < mCornerRadius) {
            Log.d(TAG, "1  corner");
            return 1;
        } else if (rightCorner < mCornerRadius && bottomCorner < mCornerRadius) {
            Log.d(TAG, "2  corner");
            return 2;
        } else if (leftCorner < mCornerRadius && bottomCorner < mCornerRadius) {
            Log.d(TAG, "3  corner");
            return 3;
        } else if (leftCorner < mCornerRadius) {
            Log.d(TAG, "4  left edge");
            return 4;
        } else if (topCorner < mCornerRadius) {
            Log.d(TAG, "5  top edge");
            return 5;
        } else if (rightCorner < mCornerRadius) {
            Log.d(TAG, "6  right edge");
            return 6;
        } else if (bottomCorner < mCornerRadius) {
            Log.d(TAG, "7  bottom edge");
            return 7;
        } else {
            return -1;
        }
    }


    private void setCanBeExpand(boolean canBeExpand) {
        this.canBeExpand = canBeExpand;
    }

    private boolean handleExpand(float x, float y, MotionEvent ev) {
        final int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            currentCorner = -1;
            if ((currentCorner = inCorner(x, y)) == -1) {
                return false;
            } else {
                setCanBeExpand(true);
            }
        }
        if (currentCorner == -1) return false;
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                if (canBeExpand) {
                    final float rawX = ev.getX();
                    final float rawY = ev.getY();
                    if (preTouchX != -1) {
                        //hit
                        Log.d(TAG, "rawX:" + rawX + ",rawY:" + rawY);
                        layoutChild(currentCorner, rawX, rawY);
                    }
                    preTouchX = rawX;
                    return true;
                } else return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setCanBeExpand(false);
                preTouchX = -1;
                break;
            default:
                break;
        }
        return false;
    }


    private void layoutChild(int currentCorner, float rawX, float rawY) {
        View childView = getChildView();
        if (childView == null) {
            return;
        }
        int minimumWidth = childView.getMinimumWidth();
        int minimumHeight = childView.getMinimumHeight();
        int childViewLeft = childView.getLeft();
        int childViewTop = childView.getTop();
        int childViewRight = childView.getRight();
        int childViewBottom = childView.getBottom();
        synchronized (this) {
            if (currentCorner == 0) {
                l = (int) Math.min(Math.max(0, rawX), childViewRight - minimumWidth);
                t = (int) Math.min(Math.max(0, rawY), childViewBottom - minimumHeight);
                r = childViewRight;
                b = childViewBottom;
            } else if (currentCorner == 1) {
                l = childViewLeft;
                t = (int) Math.min(Math.max(0, rawY), childViewBottom - minimumHeight);
                r = (int) Math.max(Math.min(getRight(), rawX), childViewLeft + minimumWidth);
                b = childViewBottom;
            } else if (currentCorner == 2) {
                l = childViewLeft;
                t = childViewTop;
                r = (int) Math.max(Math.min(getRight(), rawX), childViewLeft + minimumWidth);
                b = (int) Math.max(Math.min(getBottom(), rawY), childViewTop + minimumHeight);
            } else if (currentCorner == 3) {
                l = (int) Math.min(Math.max(0, rawX), childViewRight - minimumWidth);
                t = childViewTop;
                r = childViewRight;
                b = (int) Math.max(Math.min(getBottom(), rawY), childViewTop + minimumHeight);
            } else if (currentCorner == 4) {
                //left edge
                l = (int) Math.min(Math.max(0, rawX), childViewRight - minimumWidth);
                t = childViewTop;
                r = childViewRight;
                b = childViewBottom;
            } else if (currentCorner == 5) {
                //top edge
                l = childViewLeft;
                t = (int) Math.min(Math.max(0, rawY), childViewBottom - minimumHeight);
                r = childViewRight;
                b = childViewBottom;
            } else if (currentCorner == 6) {
                //right edge
                l = childViewLeft;
                t = childViewTop;
                r = (int) Math.max(Math.min(rawX, getRight()), childViewLeft + minimumWidth);
                b = childViewBottom;
            } else if (currentCorner == 7) {
                //bottom edge
                l = childViewLeft;
                t = childViewTop;
                r = childViewRight;
                b = (int) Math.max(Math.min(rawY, getBottom()), childViewTop + minimumHeight);
            }
            if (l >= r || b <= t) return;
            l = Math.max(0, l);
            t = Math.max(0, t);
            r = Math.min(getWidth(), r);
            b = Math.min(getHeight(), b);

            int w = r - l, h = b - t;
            Log.d(TAG, "index:" + currentCorner + String.format(",l:%s,t:%s,vR:%s,vB:%s", l, t, getRight() - getChildView().getRight(), getBottom() - getChildView().getBottom()));
            LayoutParams lp = (LayoutParams) getChildView().getLayoutParams();
            lp.width = w;
            lp.height = h;
            lp.gravity = Gravity.NO_GRAVITY;
            lp.setMargins(l, t, 0, 0);
            getChildView().layout(l, t, r, b);
            Log.d(TAG, "getChildView:" + shapper.getId());
            getChildView().requestLayout();
            if (sizeUpdateListener != null) {
                sizeUpdateListener.update(shapper, w, h);
            }
        }
    }

    private View getChildView() {
        return shapper.getShaper();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            ViewGroup v = (ViewGroup) getChildAt(i);
            FrameLayout.LayoutParams lp = (LayoutParams) v.getLayoutParams();
            Log.d(TAG, "onLayout:" + v.getTag() + "," + lp.gravity);
        }
    }

    private void ensureShapper(float x, float y) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            int l = v.getLeft();
            int t = v.getTop();
            int r = v.getRight();
            int b = v.getBottom();
            if (l < x && r > x && t < y && b > y) {
                shapper = (Shaper) v;
                break;
            }
        }
        if (shapper != null) {
            Log.d(TAG, "ensureShapper:" + ((View) shapper).getTag());
        }
    }

    public void setSizeUpdateListener(SizeUpdateListener sizeUpdateListener) {
        this.sizeUpdateListener = sizeUpdateListener;
    }

    private SizeUpdateListener sizeUpdateListener;

    public interface SizeUpdateListener {
        void update(Shaper shaper, int w, int h);
    }

    public List<float[]> getMotionArea() {
        List<float[]> result = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof Shaper) {
                float[] floats = new float[4];
                if (view.getVisibility() == VISIBLE) {
                    float width = getMeasuredWidth();
                    float height = getMeasuredHeight();
                    floats[0] = Math.min((float) view.getLeft() / width, 1.0f);//最大1.0f
                    floats[1] = Math.min((float) view.getTop() / height, 1.0f);
                    floats[2] = Math.min((float) view.getRight() / width, 1.0f);
                    floats[3] = Math.min((float) view.getBottom() / height, 1.0f);
                    AppLogger.w("区域侦测:container width:" + width + ", area width:" + view.getWidth() + ",container height:" + height + ",area height:" + view.getHeight() + ",left:" + floats[0] + ",top:" + floats[1] + ",right:" + floats[2] + ",bottom:" + floats[3]);
                }
                result.add(floats);
            }
        }
        if (result.size() == 0) {
            result.add(new float[]{0.0f, 0.0f, 1.0f, 1.0f});
        }
        return result;
    }

    public boolean hasShaper() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof Shaper) {
                return true;
            }
        }
        return false;
    }

    public Shaper getShapper() {
        return shapper;
    }
}
