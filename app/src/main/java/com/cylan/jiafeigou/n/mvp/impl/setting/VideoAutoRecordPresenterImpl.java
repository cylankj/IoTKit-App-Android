package com.cylan.jiafeigou.n.mvp.impl.setting;

/**
 * Created by cylan-hunt on 16-12-3.
 */

import android.util.Pair;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.setting.VideoAutoRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class VideoAutoRecordPresenterImpl extends AbstractPresenter<VideoAutoRecordContract.View>
        implements VideoAutoRecordContract.Presenter {
    private BeanCamInfo beanCamInfo;

    public VideoAutoRecordPresenterImpl(VideoAutoRecordContract.View view,
                                        BeanCamInfo beanCamInfo) {
        super(view);
        this.beanCamInfo = beanCamInfo;
    }

    @Override
    public void saveCamInfoBean(BeanCamInfo beanCamInfo, int id) {
        this.beanCamInfo = beanCamInfo;
        Observable.just(new Pair<>(beanCamInfo, id))
                .filter(new RxHelper.Filter<Pair<BeanCamInfo, Integer>>("", id > 0))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pair<BeanCamInfo, Integer>>() {
                    @Override
                    public void call(Pair<BeanCamInfo, Integer> beanCamInfoIntegerPair) {
                        int id = beanCamInfoIntegerPair.second;
                        RxEvent.JFGAttributeUpdate update = new RxEvent.JFGAttributeUpdate();
                        update.uuid = beanCamInfoIntegerPair.first.deviceBase.uuid;
                        update.o = beanCamInfoIntegerPair.first.getObject(id);
                        update.msgId = id;
                        update.version = System.currentTimeMillis();
                        RxBus.getCacheInstance().post(update);
                        try {
                            JfgCmdInsurance.getCmd().robotSetData(beanCamInfoIntegerPair.first.deviceBase.uuid,
                                    DpUtils.getList(id,
                                            beanCamInfoIntegerPair.first.getByte(id)
                                            , System.currentTimeMillis()));
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
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
