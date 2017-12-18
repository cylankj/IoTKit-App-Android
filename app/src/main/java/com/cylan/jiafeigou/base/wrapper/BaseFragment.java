package com.cylan.jiafeigou.base.wrapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.dagger.Injectable;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.ActivityBackInterceptor;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.view.LifecycleAdapter;
import com.cylan.jiafeigou.view.PresenterAdapter;
import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.LifecycleTransformer;
import com.trello.rxlifecycle.RxLifecycle;
import com.trello.rxlifecycle.android.FragmentEvent;
import com.trello.rxlifecycle.android.RxLifecycleAndroid;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import static android.app.Activity.RESULT_CANCELED;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseFragment<P extends JFGPresenter> extends Fragment implements JFGView, ActivityBackInterceptor
        , PresenterAdapter<P>, LifecycleProvider<FragmentEvent>, Injectable {
    protected String uuid;
    protected P presenter;
    protected LifecycleAdapter lifecycleAdapter;
    protected BehaviorSubject<FragmentEvent> lifecycleSubject;
    protected int resultCode = RESULT_CANCELED;
    protected Intent resultData = null;
    protected boolean isVisible;
    protected boolean isPrepared;
    protected Unbinder unbinder;
    //可以被重复使用的之前创建的 View ,子类可以决定是否使用 CacheView
//    protected View mCachedRootView;

    @Inject
    public final void setPresenter(P presenter) {
        this.presenter = presenter;
        this.presenter.uuid(uuid);
        if (presenter instanceof LifecycleAdapter) {
            lifecycleAdapter = (LifecycleAdapter) presenter;
        }
    }

    @Override
    public final String uuid() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            uuid = arguments.getString(JConstant.KEY_DEVICE_ITEM_UUID);
        }
        return uuid;
    }

    @Override
    @CallSuper
    public boolean performBackIntercept(boolean willExit) {
        return false;
    }

    @Override
    public final Activity activity() {
        return getActivity();
    }

    @Nonnull
    @Override
    public final Observable<FragmentEvent> lifecycle() {
        return lifecycleSubject.asObservable();
    }

    @Nonnull
    @Override
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycleAndroid.bindFragment(lifecycleSubject);
    }

    @Nonnull
    @Override
    public final <T> LifecycleTransformer<T> bindUntilEvent(@Nonnull FragmentEvent event) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleSubject.onNext(FragmentEvent.CREATE);
    }

    @Override
    public boolean useDaggerInject() {
        return true;
    }

    @Override
    public boolean useButterKnifeInject() {
        return true;
    }

    private final void injectDagger() {
        if (useDaggerInject()) {
            try {
                AndroidSupportInjection.inject(this);
            } catch (Exception e) {
                e.printStackTrace();
                AppLogger.w("Dagger 注入失败了,如果不需要 Dagger 注入,重写 useDaggerInject 方法并返回 FALSE");
            }
        }
    }

    private final void injectButterKinfe(View view) {
        if (useButterKnifeInject()) {
            unbinder = ButterKnife.bind(this, view);
        }
    }

    @Override
    public void onAttach(Context context) {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            uuid = arguments.getString(JConstant.KEY_DEVICE_ITEM_UUID);
        }
        lifecycleSubject = BehaviorSubject.create();
        injectDagger();
        final FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).addActivityBackInterceptor(this);
        }

        super.onAttach(context);
        lifecycleSubject.onNext(FragmentEvent.ATTACH);
    }

    @CallSuper
    protected void lazyLoad() {
    }

    @Override
    @CallSuper
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible = isVisibleToUser) {
            onVisible();
        } else {
            onInvisible();
        }
    }

    @CallSuper
    protected void onVisible() {
        if (isPrepared) {
            lazyLoad();
        }
    }

    @CallSuper
    protected void onInvisible() {

    }

    @Override
    public void onDestroyView() {
        lifecycleSubject.onNext(FragmentEvent.DESTROY_VIEW);

//        mCachedRootView = getView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        lifecycleSubject.onNext(FragmentEvent.DESTROY);
        if (lifecycleAdapter != null) {
            lifecycleAdapter.destroy();
        }
        super.onDestroy();

//        if (unbinder != null) {
//            unbinder.unbind();
//            unbinder = null;
//        }
    }

    @Override
    public void onDetach() {
        lifecycleSubject.onNext(FragmentEvent.DETACH);
        super.onDetach();
        performActivityResult();
        if (callBack != null) {
            callBack.callBack(cache);
        }
        if (presenter != null && presenter.isSubscribed()) {
            presenter.unsubscribe();
        }
        final FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).removeActivityBackInterceptor(this);
        }

        if (presenter != null && presenter.isSubscribed()) {
            presenter.unsubscribe();
        }
//        if (unbinder != null) {
//            unbinder.unbind();
//        }
//        unbinder = null;
//        callBack = null;
//        presenter = null;
//        lifecycleAdapter = null;
//        lifecycleSubject = null;
//        mCachedRootView = null;
    }

    private void performActivityResult() {
        int resultCode;
        Intent resultData;
        synchronized (this) {
            resultCode = this.resultCode;
            resultData = this.resultData;
        }
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, resultData);
        }
//        else if (getActivity() instanceof BaseActivity) {
//            if (getTargetRequestCode() != 0) {
//                ((BaseActivity) getActivity()).onActivityResult(getTargetRequestCode(), resultCode, resultData);
//            }
//        }
    }

    @Override
    @CallSuper
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        injectButterKinfe(view);
        initViewAndListener();
        lifecycleSubject.onNext(FragmentEvent.CREATE_VIEW);
        isPrepared = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.subscribe();
        }
        if (lifecycleAdapter != null) {
            lifecycleAdapter.start();
        }
        lifecycleSubject.onNext(FragmentEvent.START);

        if (isVisible && isPrepared) {
            lazyLoad();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        lifecycleSubject.onNext(FragmentEvent.RESUME);
    }

    @Override
    public void onPause() {
        lifecycleSubject.onNext(FragmentEvent.PAUSE);
        super.onPause();
        if (lifecycleAdapter != null) {
            lifecycleAdapter.pause();
        }
    }

    @Override
    public void onStop() {
        lifecycleSubject.onNext(FragmentEvent.STOP);
        super.onStop();
        if (lifecycleAdapter != null) {
            lifecycleAdapter.stop();
        }
        IMEUtils.hide(getActivity());
    }

    @CallSuper
    protected void initViewAndListener() {

    }

    @Override
    public void onLoginStateChanged(boolean online) {

    }

    public final void setResult(int resultCode) {
        synchronized (this) {
            this.resultCode = resultCode;
            this.resultData = null;
        }
    }

    public final void setResult(int resultCode, Intent data) {
        synchronized (this) {
            this.resultCode = resultCode;
            this.resultData = data;
        }
    }

    /**
     * 使用 setResult 配合 onActivityResult 回调
     */
    @Deprecated
    protected Object cache;
    @Deprecated
    public CallBack callBack;

    @Deprecated
    public void setCache(Object cache) {
        this.cache = cache;
    }

    @Deprecated
    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    @Deprecated
    public interface CallBack {
        void callBack(Object t);
    }

}