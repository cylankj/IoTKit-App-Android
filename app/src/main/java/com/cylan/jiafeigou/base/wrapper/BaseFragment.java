package com.cylan.jiafeigou.base.wrapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.dagger.Injectable;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.ActivityBackInterceptor;
import com.cylan.jiafeigou.support.log.AppLogger;
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
    protected final BehaviorSubject<FragmentEvent> lifecycleSubject = BehaviorSubject.create();
    protected int resultCode = RESULT_CANCELED;
    protected Intent resultData = null;

    @Inject
    public final void setPresenter(P presenter) {
        this.presenter = presenter;
        this.presenter.uuid(uuid);
        if (presenter instanceof LifecycleAdapter) {
            this.lifecycleAdapter = (LifecycleAdapter) presenter;
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
    public boolean performBackIntercept() {
        return false;
    }

    @Override
    public final Activity activity() {
        return getActivity();
    }

    @Nonnull
    @Override
    public Observable<FragmentEvent> lifecycle() {
        return lifecycleSubject.asObservable();
    }

    @Nonnull
    @Override
    public <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycleAndroid.bindFragment(lifecycleSubject);
    }

    @Nonnull
    @Override
    public <T> LifecycleTransformer<T> bindUntilEvent(@Nonnull FragmentEvent event) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleSubject.onNext(FragmentEvent.CREATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = null;
        if (getContentViewID() != -1) {
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
    public boolean supportInject() {
        return true;
    }

    private final void injectDagger() {
        if (supportInject()) {
            try {
                AndroidSupportInjection.inject(this);
            } catch (Exception e) {
                e.printStackTrace();
                AppLogger.w("Dagger 注入失败了,如果不需要 Dagger 注入,重写 supportInject 方法并返回 FALSE");
            }
        }
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
        injectDagger();
        super.onAttach(context);
        lifecycleSubject.onNext(FragmentEvent.ATTACH);
    }


    @Override
    public void onDestroyView() {
        lifecycleSubject.onNext(FragmentEvent.DESTROY_VIEW);
        /**
         *需要在 onDestroyView 之前移除对 backEvent 的监听,在这个方法调用后 View 不存在了
         * 继续对 UI 界面操作可能会出错
         * */
        final FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).removeActivityBackInterceptor(this);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        lifecycleSubject.onNext(FragmentEvent.DESTROY);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        lifecycleSubject.onNext(FragmentEvent.DETACH);
        super.onDetach();
        performActivityResult();

        if (callBack != null) {
            callBack.callBack(cache);
        }
    }

    private void performActivityResult() {
        int resultCode;
        Intent resultData;
        synchronized (this) {
            resultCode = this.resultCode;
            resultData = this.resultData;
        }
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).onActivityResult(getTargetRequestCode(), resultCode, resultData);
        } else if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, resultData);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewAndListener();
        lifecycleSubject.onNext(FragmentEvent.CREATE_VIEW);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (lifecycleAdapter != null) {
            lifecycleAdapter.start();
        }
        lifecycleSubject.onNext(FragmentEvent.START);
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
    }


    protected int getContentViewID() {
        return -1;
    }

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