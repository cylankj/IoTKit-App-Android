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

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;

import static android.app.Activity.RESULT_CANCELED;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseFragment<P extends JFGPresenter> extends Fragment implements JFGView, ActivityBackInterceptor, PresenterAdapter<P>, Injectable {
    protected String uuid;

    protected P presenter;

    protected LifecycleAdapter lifecycleAdapter;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    @Override
    public void onDetach() {
        super.onDetach();

        final FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).removeActivityBackInterceptor(this);
        }

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
    }

    @Override
    public void onStart() {
        super.onStart();
        if (lifecycleAdapter != null) {
            lifecycleAdapter.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (lifecycleAdapter != null) {
            lifecycleAdapter.pause();
        }
    }

    @Override
    public void onStop() {
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