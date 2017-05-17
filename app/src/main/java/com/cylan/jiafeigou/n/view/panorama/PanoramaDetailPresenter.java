package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.lzy.okserver.download.DownloadManager;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/5/10.
 */

public class PanoramaDetailPresenter extends BasePresenter<PanoramaDetailContact.View> implements PanoramaDetailContact.Presenter {
    @Override
    public void delete(PanoramaAlbumContact.PanoramaItem item, int mode) {
        if (mode == 0) {
            DownloadManager.getInstance().removeTask(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, item.fileName));
            mView.onDeleteResult(0);
        } else {
            Subscription subscribe = BasePanoramaApiHelper.getInstance().delete(1, Collections.singletonList(item.fileName))
                    .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .map(ret -> {
                        DownloadManager.getInstance().removeTask(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, item.fileName));
                        return ret;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {
                        if (ret != null && ret.ret == 0) {//删除成功
                            mView.onDeleteResult(0);
                        } else {
                            mView.onDeleteResult(-1);//本地删除了,设备删除失败
                        }
                    }, e -> {
                        AppLogger.e(e.getMessage());
                    });
            registerSubscription(subscribe);
        }
    }
}
