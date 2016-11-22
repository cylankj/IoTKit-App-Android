package com.cylan.jiafeigou.n.mvp.impl.cloud;

import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.db.DataBaseUtil;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseDbBean;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLiveSettingPresenterImp extends AbstractPresenter<CloudLiveSettingContract.View> implements CloudLiveSettingContract.Presenter {

    private Subscription clearDbSub;
    private DbManager dbManager = null;
    private CompositeSubscription subscription;


    public CloudLiveSettingPresenterImp(CloudLiveSettingContract.View view) {
        super(view);
        view.setPresenter(this);
        subscription = new CompositeSubscription();
    }

    @Override
    public void start() {
        if (getView() != null) {
            getView().initSomeViewVisible(isHasBeenShareUser());
        }
        subscription.add(getAccount());
    }

    @Override
    public void stop() {
        if (clearDbSub != null) {
            clearDbSub.unsubscribe();
        }
    }

    @Override
    public boolean isHasBeenShareUser() {
        //TODO 查询用户的设备是否有绑定改云相框
        return false;
    }

    @Override
    public void clearMesgRecord() {
        try {
            if (dbManager.findAll(CloudLiveBaseDbBean.class).size() == 0) {
                ToastUtil.showToast("记录为空");
                return;
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        getView().showClearRecordProgress();
        final DbManager finalDbManager = dbManager;
        clearDbSub = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .delay(2000, TimeUnit.MILLISECONDS)
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        try {
                            finalDbManager.delete(CloudLiveBaseDbBean.class);
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
                        getView().hideClearRecordProgress();
                    }
                });
    }

    /**
     * 获取到用户的账号信息
     */
    @Override
    public Subscription getAccount() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null && getUserInfo instanceof RxEvent.GetUserInfo){
                            dbManager = DataBaseUtil.getInstance(getUserInfo.jfgAccount.getAccount()).dbManager;
                        }
                    }
                });
    }

}
