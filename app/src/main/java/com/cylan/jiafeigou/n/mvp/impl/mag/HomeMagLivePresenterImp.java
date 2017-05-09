package com.cylan.jiafeigou.n.mvp.impl.mag;

import android.text.TextUtils;

import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.db.DataBaseUtil;
import com.cylan.jiafeigou.n.mvp.contract.mag.HomeMagLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MagBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/19
 * 描述：
 */
public class HomeMagLivePresenterImp extends AbstractPresenter<HomeMagLiveContract.View> implements HomeMagLiveContract.Presenter {

    private boolean isChick = false;
    private DbManager dbManager;
    private CompositeSubscription compositeSubscription;
    private String uuid;

    public HomeMagLivePresenterImp(HomeMagLiveContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
    }

    @Override
    public void start() {
        super.start();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getAccount());
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }

    @Override
    public void clearOpenAndCloseRecord() {
        try {
            if (dbManager.findAll(MagBean.class).size() == 0) {
                getView().showNoMesg();
                return;
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        if (getView() != null) {
            getView().showClearProgress();
        }

        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .delay(2000, TimeUnit.MILLISECONDS)
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        try {
                            dbManager.delete(MagBean.class);
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().hideClearProgress();
                    }
                }, AppLogger::e);
    }

    @Override
    public boolean getNegation() {
        isChick = !isChick;
        return isChick;
    }

    @Override
    public void saveSwitchState(boolean isChick, String key) {
        PreferencesUtils.putBoolean(key, isChick);
    }

    @Override
    public boolean getSwitchState(String key) {
        return PreferencesUtils.getBoolean(key);
    }

    /**
     * 拿到用户对象获取数据库
     */
    @Override
    public Subscription getAccount() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.AccountArrived>() {
                    @Override
                    public void call(RxEvent.AccountArrived getUserInfo) {
                        if (getUserInfo != null) {
                            if (dbManager == null)
                                dbManager = DataBaseUtil.getInstance(getUserInfo.jfgAccount.getAccount()).dbManager;
                        }
                    }
                }, AppLogger::e);
    }


    /**
     * 获取到设备是名字
     *
     * @return
     */
    @Override
    public String getDeviceName() {
        Device jfgDevice = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        if (jfgDevice == null)
            return uuid;
        return TextUtils.isEmpty(jfgDevice.alias) ?
                uuid : jfgDevice.alias;
    }

}
