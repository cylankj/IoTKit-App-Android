package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.wrapper.BaseViewablePresenter;

/**
 * Created by yanzhendong on 2017/3/8.
 */

public class PanoramaPresenter extends BaseViewablePresenter<PanoramaCameraContact.View> implements PanoramaCameraContact.Presenter {
    @Override
    public void makePhotograph() {
//        Observable.just((JFGCameraDevice) mSourceManager.getJFGDevice(mUUID))
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io())
//                .filter(dev -> {
//                    if (dev == null)
//                        return dev != null;
//                })
    }

    @Override
    public void startMakeLongVideo() {

    }

    @Override
    public void stopMakeMakeLongVideo() {

    }

    @Override
    public void startMakeShortVideo() {

    }

    @Override
    public void stopMakeShortVideo() {

    }
}
