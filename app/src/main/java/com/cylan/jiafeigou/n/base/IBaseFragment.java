package com.cylan.jiafeigou.n.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.IMEUtils;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;

/**
 * Created by cylan-hunt on 16-11-11.
 */

public abstract class IBaseFragment<P extends BasePresenter> extends Fragment {

    protected P basePresenter;
    private final String TAG = getClass().getSimpleName();

    protected AlertDialogManager getAlertDialogManager() {
        return AlertDialogManager.getInstance();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AppLogger.d(this.getClass().getSimpleName());
        Log.d(TAG, TAG + ",onAttach");
    }

    public String getUuid() {
        if (getArguments() == null) return "";
        return getArguments().getString(KEY_DEVICE_ITEM_UUID);
    }

    public Device getDevice() {
        return BaseApplication.getAppComponent().getSourceManager().getDevice(getUuid());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnClickListener(v -> IMEUtils.hide(getActivity()));
    }

    @Override
    public void onStart() {
        Log.d(TAG, TAG + ",onStart");
        super.onStart();
        if (basePresenter != null) basePresenter.start();
    }

    @Override
    public void onPause() {
        Log.d(TAG, TAG + ",onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, TAG + ",onStop");
        super.onStop();
        if (basePresenter != null) basePresenter.stop();
        if (getActivity() != null) IMEUtils.hide(getActivity());
    }

    @Override
    public void onDetach() {
        Log.d(TAG, TAG + ",onDetach");
        super.onDetach();
        if (callBack != null) callBack.callBack(cache);
    }

    public void setPresenter(P presenter) {

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