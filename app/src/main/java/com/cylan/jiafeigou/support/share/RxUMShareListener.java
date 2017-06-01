package com.cylan.jiafeigou.support.share;

import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/6/1.
 */

public class RxUMShareListener implements UMShareListener {
    private static Observable rxObserver;
    private static RxUMShareListener instance;

    @Override
    public void onStart(SHARE_MEDIA share_media) {

    }

    @Override
    public void onResult(SHARE_MEDIA share_media) {

    }

    @Override
    public void onError(SHARE_MEDIA share_media, Throwable throwable) {

    }

    @Override
    public void onCancel(SHARE_MEDIA share_media) {

    }

    public Observable with() {
        return null;
    }

    public static RxUMShareListener getInstance() {
        if (instance == null) {
            synchronized (RxUMShareListener.class) {
                if (instance == null) {
                    instance = new RxUMShareListener();
//                    Subscriber subscriber ;
//                    subscriber.onNext();
                }
            }
        }
        return instance;
    }
}
