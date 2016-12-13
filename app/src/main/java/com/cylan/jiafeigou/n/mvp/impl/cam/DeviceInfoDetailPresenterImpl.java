package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.util.Pair;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class DeviceInfoDetailPresenterImpl extends AbstractPresenter<CamInfoContract.View>
        implements CamInfoContract.Presenter {

    private BeanCamInfo beanCamInfo;

    public DeviceInfoDetailPresenterImpl(CamInfoContract.View view, BeanCamInfo info) {
        super(view);
        this.beanCamInfo = info;
        view.setPresenter(this);
    }

    @Override
    public void saveCamInfoBean(BeanCamInfo info, int id) {
        this.beanCamInfo = info;
        Observable.just(new Pair<>(info, id))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pair<BeanCamInfo, Integer>>() {
                    @Override
                    public void call(Pair<BeanCamInfo, Integer> beanCamInfoIntegerPair) {
                        int id = beanCamInfoIntegerPair.second;
                        RxEvent.JFGAttributeUpdate update = new RxEvent.JFGAttributeUpdate();
                        update.uuid = beanCamInfo.deviceBase.uuid;
                        if (id == DpMsgMap.ID_2000003_BASE_ALIAS)
                            update.o = beanCamInfoIntegerPair.first.deviceBase.alias;
                        else update.o = beanCamInfoIntegerPair.first.getObject(id);
                        update.msgId = id;
                        update.version = System.currentTimeMillis();
                        RxBus.getCacheInstance().post(update);
                        if (id == DpMsgMap.ID_2000003_BASE_ALIAS) {
                            try {
                                JfgCmdInsurance.getCmd().setAliasByCid(beanCamInfo.deviceBase.uuid,
                                        beanCamInfo.deviceBase.alias);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                            AppLogger.i("update alias: " + new Gson().toJson(beanCamInfo));
                            return;
                        }
                        try {
                            JfgCmdInsurance.getCmd().robotSetData(beanCamInfo.deviceBase.uuid,
                                    DpUtils.getList(id,
                                            beanCamInfoIntegerPair.first.getByte(id)
                                            , System.currentTimeMillis()));
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                        AppLogger.i("update camInfo: " + new Gson().toJson(beanCamInfo));
                    }
                });
    }

    @Override
    public BeanCamInfo getBeanCamInfo() {
        if (this.beanCamInfo == null) {
            AppLogger.e("should not happen");
        }
        return beanCamInfo;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
