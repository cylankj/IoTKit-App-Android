package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.util.Pair;

import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamWarnContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class CamAlarmPresenterImpl extends AbstractPresenter<CamWarnContract.View> implements
        CamWarnContract.Presenter {
    private BeanCamInfo beanCamInfo;

    public CamAlarmPresenterImpl(CamWarnContract.View view, BeanCamInfo info) {
        super(view);
        view.setPresenter(this);
        this.beanCamInfo = info;
    }

    @Override
    public void saveCamInfoBean(BeanCamInfo info, int id) {
        beanCamInfo = info;
        Observable.just(new Pair<>(beanCamInfo, id))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pair<BeanCamInfo, Integer>>() {
                    @Override
                    public void call(Pair<BeanCamInfo, Integer> beanCamInfoIntegerPair) {
                        int id = beanCamInfoIntegerPair.second;
                        RxEvent.JFGAttributeUpdate update = new RxEvent.JFGAttributeUpdate();
                        update.uuid = beanCamInfo.deviceBase.uuid;
                        update.o = beanCamInfoIntegerPair.first.getObject(id);
                        update.msgId = id;
                        update.version = System.currentTimeMillis();
                        RxBus.getCacheInstance().post(update);
                        JfgCmdInsurance.getCmd().robotSetData(beanCamInfo.deviceBase.uuid,
                                DpUtils.getList(id,
                                        beanCamInfoIntegerPair.first.getByte(id)
                                        , System.currentTimeMillis()));
                        AppLogger.i("save bean Cam info");
                    }
                });
    }

    @Override
    public BeanCamInfo getBeanCamInfo() {
        return beanCamInfo;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
