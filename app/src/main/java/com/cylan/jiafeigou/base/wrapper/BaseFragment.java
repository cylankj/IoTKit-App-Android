package com.cylan.jiafeigou.base.wrapper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.ActivityBackInterceptor;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseFragment<P extends JFGPresenter> extends DaggerSupportFragment implements JFGView, ActivityBackInterceptor {
    protected String uuid;

    protected P presenter;

    @Inject
    public void setPresenter(P presenter) {
        this.presenter = presenter;
        this.presenter.uuid(uuid);
    }

    @Override
    public boolean performBackIntercept() {
        return false;
    }

    @Override
    public Activity activity() {
        return getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = null;
        if (getContentViewID() != -1) {//!=-1 会启动 butterknife ,==-1:自己设置 view, 可以使用 databinding
            contentView = inflater.inflate(getContentViewID(), container, false);
            ButterKnife.bind(this, contentView);
        } else if (getContentRootView() != null) {
            contentView = getContentRootView();
        }
        return contentView;
    }

    protected View getContentRootView() {
        return null;
    }

    @Override
    public void onAttach(Context context) {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            uuid = arguments.getString(JConstant.KEY_DEVICE_ITEM_UUID);
        }
        final FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).addActivityBackInterceptor(this);
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        final FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).removeActivityBackInterceptor(this);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewAndListener();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter || nextAnim == 0) {
            return null;
        }

        final Animation animator = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        animator.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation.setAnimationListener(null);
                onEnterAnimationFinished();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return animator;
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
        }
    }

    protected int getContentViewID() {
        return -1;
    }

    protected void initViewAndListener() {
    }


    protected void onEnterAnimationFinished() {

    }

    @Override
    public void onLoginStateChanged(boolean online) {

    }

    protected Object cache;
    public CallBack callBack;

    public void setCache(Object cache) {
        this.cache = cache;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        void callBack(Object t);
    }

}