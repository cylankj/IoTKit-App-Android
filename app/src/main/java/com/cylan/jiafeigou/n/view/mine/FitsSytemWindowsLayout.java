package com.cylan.jiafeigou.n.view.mine;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 作者：zsl
 * 创建时间：2016/8/31
 * 描述：
 */
public class FitsSytemWindowsLayout extends RelativeLayout {

    private SoftKeyBoardStateListener softKeyBoardStateListener;

    public FitsSytemWindowsLayout(Context context) {
        super(context);
    }


    public FitsSytemWindowsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FitsSytemWindowsLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        dispatchListenerLow(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 处理低版本键盘弹出
     *
     * @param heightMeasureSpec 高度
     */
    private void dispatchListenerLow(int heightMeasureSpec) {
        if (softKeyBoardStateListener == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return;
        }
        int oldSpec = getMeasuredHeight();

        if (oldSpec <= 0) {
            return;
        }
        int newSpec = MeasureSpec.getSize(heightMeasureSpec);
        int offset = oldSpec - newSpec;
        if (offset > 100) {
            softKeyBoardStateListener.onSoftKeyBoardStateChange(true);
        } else if (offset < 0) {
            softKeyBoardStateListener.onSoftKeyBoardStateChange(false);
        }
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        dispatchListener(insets);
        insets.top = 0;
        return super.fitSystemWindows(insets);
    }

    /**
     * 分发监听
     *
     * @param insets
     */
    private void dispatchListener(Rect insets) {
        if (softKeyBoardStateListener == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        if (insets.top != 0 && insets.bottom != 0) {
            softKeyBoardStateListener.onSoftKeyBoardStateChange(true);
        } else {
            softKeyBoardStateListener.onSoftKeyBoardStateChange(false);
        }

    }

    /**
     * 设置软键盘监听事件
     *
     * @param softKeyBoardStateListener
     */
    public void setSoftKeyBoardListener(SoftKeyBoardStateListener softKeyBoardStateListener) {
        this.softKeyBoardStateListener = softKeyBoardStateListener;
    }

    public interface SoftKeyBoardStateListener {

        public void onSoftKeyBoardStateChange(boolean isOpen);

    }
}
