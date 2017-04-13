package com.cylan.jiafeigou.n.mvp.impl.setting;

/**
 * Created by cylan-hunt on 16-12-3.
 */

import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.n.base.BaseApplication;
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
    public <T extends DataPoint> void updateInfoReq(T value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        BaseApplication.getAppComponent().getSourceManager().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }
}
