package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamWarnContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class CamAlarmPresenterImpl extends AbstractPresenter<CamWarnContract.View> implements
        CamWarnContract.Presenter {
    private BeanCamInfo beanCamInfo;
    private String uuid;

    public CamAlarmPresenterImpl(CamWarnContract.View view, BeanCamInfo info) {
        super(view);
        view.setPresenter(this);
        this.beanCamInfo = info;
        this.uuid = info.deviceBase.uuid;
    }

    @Override
    public void updateInfoReq(Object value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    BaseValue baseValue = new BaseValue();
                    baseValue.setId(id);
                    baseValue.setVersion(System.currentTimeMillis());
                    baseValue.setValue(o);
                    GlobalDataProxy.getInstance().update(uuid, baseValue, true);
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

    @Override
    public BeanCamInfo getBeanCamInfo() {
        return beanCamInfo;
    }

}
