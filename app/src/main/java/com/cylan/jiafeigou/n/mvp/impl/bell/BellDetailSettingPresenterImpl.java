package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.util.Pair;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellDetailContract;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class BellDetailSettingPresenterImpl extends BasePresenter<BellDetailContract.View>
        implements BellDetailContract.Presenter {
    private BeanBellInfo beanBellInfo;

    @Override
    public void onSetContentView() {
        super.onSetContentView();
        mView.onShowProperty(mSourceManager.getJFGDevice(mUUID));
    }


    @Override
    public BeanBellInfo getBellInfo() {
        return beanBellInfo;
    }

    @Override
    public void updateInfoReq(String uuid, Object value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    AppLogger.i("save start: " + id + " " + value);
                    BaseValue baseValue = new BaseValue();
                    baseValue.setId(id);
                    baseValue.setVersion(System.currentTimeMillis());
                    baseValue.setValue(o);
                    GlobalDataProxy.getInstance().update(uuid, baseValue, true);
                    AppLogger.i("save end: " + id + " " + value);
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }
//    @Override
//    public void saveBellInfo(BeanBellInfo info, int id) {
//        this.beanBellInfo = info;
//        Observable.just(new Pair<>(info, id))
//                .subscribeOn(Schedulers.io())
//                .subscribe(beanCamInfoIntegerPair -> {
//                    int id1 = beanCamInfoIntegerPair.second;
//                    RxEvent.JFGAttributeUpdate update = new RxEvent.JFGAttributeUpdate();
//                    update.uuid = beanBellInfo.deviceBase.uuid;
//                    if (id1 == DpMsgMap.ID_2000003_BASE_ALIAS)
//                        update.o = beanCamInfoIntegerPair.first.deviceBase.alias;
//                    else update.o = beanCamInfoIntegerPair.first.getObject(id1);
//                    update.msgId = id1;
//                    update.version = System.currentTimeMillis();
//                    RxBus.getCacheInstance().post(update);
//                    if (id1 == DpMsgMap.ID_2000003_BASE_ALIAS) {
//                        try {
//                            JfgCmdInsurance.getCmd().setAliasByCid(beanBellInfo.deviceBase.uuid,
//                                    beanBellInfo.deviceBase.alias);
//                        } catch (JfgException e) {
//                            e.printStackTrace();
//                        }
//                        AppLogger.i("setDevice alias: " + new Gson().toJson(beanBellInfo));
//                        return;
//                    }
//                    try {
//                        JfgCmdInsurance.getCmd().robotSetData(beanBellInfo.deviceBase.uuid,
//                                DpUtils.getList(id1,
//                                        beanCamInfoIntegerPair.first.getByte(id1)
//                                        , System.currentTimeMillis()));
//                    } catch (JfgException e) {
//                        e.printStackTrace();
//                    }
//                    AppLogger.i("setDevice camInfo: " + new Gson().toJson(beanBellInfo));
//                });
//    }
}
