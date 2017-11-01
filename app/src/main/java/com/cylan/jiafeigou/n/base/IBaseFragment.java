package com.cylan.jiafeigou.n.base;

import android.content.Context;
import android.util.Log;

import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

/**
 * Created by cylan-hunt on 16-11-11.
 */

public abstract class IBaseFragment<P extends BasePresenter> extends BaseFragment<P> {

    private final String TAG = getClass().getSimpleName();

    protected AlertDialogManager getAlertDialogManager() {
        return AlertDialogManager.getInstance();
    }

    @Override
    public boolean useDaggerInject() {
        return false;
    }

    @Override
    public boolean useButterKnifeInject() {
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AppLogger.d(this.getClass().getSimpleName());
        Log.d(TAG, TAG + ",onAttach");
    }

    public Device getDevice() {
        return BaseApplication.getAppComponent().getSourceManager().getDevice(uuid());
    }

    @Override
    public void onStart() {
        Log.d(TAG, TAG + ",start");
        super.onStart();
    }

    @Override
    public void onPause() {
        Log.d(TAG, TAG + ",onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, TAG + ",stop");
        super.onStop();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, TAG + ",onDetach");
        super.onDetach();
    }
}