package com.cylan.jiafeigou.n.mvp.impl.cloud;

import com.cylan.jiafeigou.n.db.CloudLiveDbUtil;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseDbBean;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;
import com.cylan.jiafeigou.support.db.table.TableEntity;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLiveSettingPresenterImp extends AbstractPresenter<CloudLiveSettingContract.View> implements CloudLiveSettingContract.Presenter{

    private Subscription clearDbSub;

    public CloudLiveSettingPresenterImp(CloudLiveSettingContract.View view) {
        super(view);
    }

    @Override
    public void start() {
        if(getView() != null){
            getView().initSomeViewVisible(isHasBeenShareUser());
        }
    }

    @Override
    public void stop() {
        if (clearDbSub != null){
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
        DbManager dbManager = null;
        try {
            dbManager = CloudLiveDbUtil.getInstance().dbManager;

            if(dbManager.findAll(CloudLiveBaseDbBean.class).size() == 0){
                ToastUtil.showToast(getView().getContext(),"记录为空");
                return;
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        getView().showClearRecordProgress();
        final DbManager finalDbManager = dbManager;
        clearDbSub = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .delay(3000, TimeUnit.MILLISECONDS)
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

}
