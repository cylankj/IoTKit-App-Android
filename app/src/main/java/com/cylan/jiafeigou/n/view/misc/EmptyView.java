package com.cylan.jiafeigou.n.view.misc;

import android.content.Context;
import android.support.annotation.WorkerThread;
import android.view.LayoutInflater;
import android.view.View;

import com.nineoldandroids.animation.AnimatorSet;

import java.lang.ref.WeakReference;

/**
 * Created by cylan-hunt on 16-8-2.
 */
public abstract class EmptyView implements IEmptyView {
    /**
     * 加载的布局Id
     */
    private final int layoutId;
    private Context context;

    protected AnimatorSet showAnimation = new AnimatorSet();
    protected AnimatorSet hideAnimation = new AnimatorSet();
    private WeakReference<View> emptyViewRef;

    public EmptyView(Context context, final int layoutId) {
        this.layoutId = layoutId;
        this.context = context;
        loadView();
    }

    @WorkerThread
    private void loadView() {
        View view = LayoutInflater.from(context)
                .inflate(layoutId, null);
        if (view == null) {
        } else {
            emptyViewRef = new WeakReference<>(view);
        }
    }

    /**
     * 返回emptyView对象
     *
     * @return
     */
    public View reInit() {
        if (emptyViewRef == null || emptyViewRef.get() == null) {
            loadView();
        }
        return emptyViewRef.get();
    }

    /**
     * @param show：显示状态
     */
    @Override
    public void show(boolean show) {
        if (show) {
            View view = reInit();
            if (view != null && !view.isShown()) {
                if (showAnimation != null) {
                    showAnimation.start();
                    return;
                }
                view.setVisibility(View.VISIBLE);
            }
        } else {
            if (emptyViewRef == null || emptyViewRef.get() == null)
                return;
            View view = emptyViewRef.get();
            if (view.isShown()) {
                if (hideAnimation != null) {
                    hideAnimation.start();
                    return;
                }
                view.setVisibility(View.GONE);
            }
        }
    }
}
