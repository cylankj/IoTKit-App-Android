package com.cylan.jiafeigou.n.mvp.impl.setting;

/**
 * Created by cylan-hunt on 16-12-3.
 */

import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.n.mvp.contract.setting.VideoAutoRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Observable;
import rx.schedulers.Schedulers;


public class VideoAutoRecordPresenterImpl extends AbstractPresenter<VideoAutoRecordContract.View>
        implements VideoAutoRecordContract.Presenter {
    //    private BeanCamInfo beanCamInfo;
    private String uuid;

    public VideoAutoRecordPresenterImpl(VideoAutoRecordContract.View view,
                                        String uuid) {
        super(view);
        this.uuid = uuid;
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
}
