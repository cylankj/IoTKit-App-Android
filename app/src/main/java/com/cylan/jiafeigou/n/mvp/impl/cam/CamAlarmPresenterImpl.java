package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.media.MediaPlayer;

import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamWarnContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.IOException;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class CamAlarmPresenterImpl extends AbstractPresenter<CamWarnContract.View> implements
        CamWarnContract.Presenter {
    private String uuid;
    private MediaPlayer mediaPlayer;

    public CamAlarmPresenterImpl(CamWarnContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
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
    public void playSound(int id) {
        Observable.just(id)
                .subscribeOn(Schedulers.io())
                .subscribe(integer -> {
                    try {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                        }
                        mediaPlayer = MediaPlayer.create(getView().getContext(), integer);
                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }, throwable -> AppLogger.e("play sound err:" + throwable.getLocalizedMessage()));
    }

}
