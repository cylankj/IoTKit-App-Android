package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class DeviceInfoDetailPresenterImpl extends AbstractPresenter<CamInfoContract.View>
        implements CamInfoContract.Presenter {

    //    private BeanCamInfo beanCamInfo;
    private String uuid;

    public DeviceInfoDetailPresenterImpl(CamInfoContract.View view, String uuid) {
        super(view);
        this.uuid = uuid;
        view.setPresenter(this);
    }

    @Override
    public void updateInfoReq(Object value, long id) {
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

    @Override
    public void checkNewSoftVersion() {
        // TODO 检测是否有新的固件
    }
}
