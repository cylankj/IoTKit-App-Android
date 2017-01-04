package com.cylan.jiafeigou.n.base;

import android.support.v4.app.Fragment;

import com.cylan.jiafeigou.n.mvp.BasePresenter;

/**
 * Created by cylan-hunt on 16-11-11.
 */

public abstract class IBaseFragment<P extends BasePresenter> extends Fragment {

    protected P basePresenter;

    @Override
    public void onStart() {
        super.onStart();
        if (basePresenter != null) basePresenter.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (basePresenter != null) basePresenter.stop();
    }

    public CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        void callBack(Object t);
    }
}