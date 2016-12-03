package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.util.Pair;

import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-12-3.
 */

public class CamSettingImpl<T> extends AbstractPresenter {

    protected BeanCamInfo camInfoBean;

    public CamSettingImpl(Object o) {
        super(o);
    }


    protected BeanCamInfo getCamInfoBean() {
        if (camInfoBean == null)
            camInfoBean = new BeanCamInfo();
        return camInfoBean;
    }

    protected void saveCamInfoBean(final BeanCamInfo camInfoBean, int id) {
        this.camInfoBean = camInfoBean;
        Observable.just(new Pair<>(camInfoBean, id))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pair<BeanCamInfo, Integer>>() {
                    @Override
                    public void call(Pair<BeanCamInfo, Integer> beanCamInfoIntegerPair) {
                        int id = beanCamInfoIntegerPair.second;
                        RxEvent.JFGAttributeUpdate update = new RxEvent.JFGAttributeUpdate();
                        update.uuid = camInfoBean.deviceBase.uuid;
                        update.o = beanCamInfoIntegerPair.first.getObject(id);
                        update.msgId = id;
                        update.version = System.currentTimeMillis();
                        RxBus.getCacheInstance().post(update);
                        JfgCmdInsurance.getCmd().robotSetData(camInfoBean.deviceBase.uuid,
                                DpUtils.getList(id,
                                        beanCamInfoIntegerPair.first.getByte(id)
                                        , System.currentTimeMillis()));
                        AppLogger.i("save bean Cam info");
                    }
                });
    }
}
