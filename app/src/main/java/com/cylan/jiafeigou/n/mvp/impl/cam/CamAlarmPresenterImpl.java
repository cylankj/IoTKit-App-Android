package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.media.MediaPlayer;

import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamWarnContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class CamAlarmPresenterImpl extends AbstractPresenter<CamWarnContract.View> implements
        CamWarnContract.Presenter {
    private MediaPlayer mediaPlayer;

    public CamAlarmPresenterImpl(CamWarnContract.View view) {
        super(view);
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
