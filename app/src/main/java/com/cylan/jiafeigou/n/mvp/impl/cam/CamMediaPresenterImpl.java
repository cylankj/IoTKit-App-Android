package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskException;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMediaContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.CamWarnGlideURL;

import java.io.File;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-12-28.
 */

public class CamMediaPresenterImpl extends AbstractPresenter<CamMediaContract.View>
        implements CamMediaContract.Presenter {

    public CamMediaPresenterImpl(CamMediaContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
    }

    private void save(Bitmap bitmap) {
        Observable.just(bitmap)
                .filter(new RxHelper.Filter<>("", bitmap != null))
                .subscribeOn(Schedulers.newThread())
                .map((Bitmap bMap) -> {
                    String filePath = JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + ".png";
                    return BitmapUtils.saveBitmap2file(bMap, filePath);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Boolean aBoolean) -> {
                    getView().savePicResult(aBoolean);
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

    @Override
    public void saveImage(CamWarnGlideURL glideURL) {
        Glide.with(getView().getContext())//注意contxt
                .load(glideURL)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        Log.d(TAG, "onResourceReady:" + (resource == null));
                        save(resource);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        AppLogger.d("err:" + (e == null ? null : e.getLocalizedMessage()));
                    }
                });
    }

    @Override
    public void unCollect(int index, long ver) {
        Observable.just(ver)
                .observeOn(Schedulers.io())
                .map(version -> new DPEntity()
                        .setUuid("")
                        .setVersion(version)
                        .setAction(DBAction.DELETED)
                        .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG))
                .flatMap(task -> BaseDPTaskDispatcher.getInstance().perform(task))
                .map(ret -> new DPEntity()
                        .setUuid(uuid)
                        .setVersion(ver)
                        .setAction(DBAction.DELETED)
                        .setMsgId(511))
                .flatMap(task -> BaseDPTaskDispatcher.getInstance().perform(task))
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getResultCode() == 0) {//成功了
                        AppLogger.d("取消收藏成功");
                    }
                }, e -> {
                    e.printStackTrace();
                    AppLogger.d(e.getMessage());
                });
    }

    @Override
    public void collect(int index, long version) {
        Observable.create((Observable.OnSubscribe<IDPEntity>) subscriber -> {
            DpMsgDefine.DPWonderItem item = new DpMsgDefine.DPWonderItem();
            item.msgType = DpMsgDefine.DPWonderItem.TYPE_PIC;
            item.cid = uuid;
            Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
            item.place = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
            item.fileName = version / 1000 + "_" + (index + 1) + ".jpg";
            item.time = (int) (version / 1000) + index + 1;//
            IDPEntity entity = new DPEntity()
                    .setUuid(uuid)
                    .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG)
                    .setVersion(System.currentTimeMillis())
                    .setAction(DBAction.SHARED)
                    .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount())
                    .setBytes(item.toBytes());
            subscriber.onNext(entity);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.io())
                .flatMap(entity -> BaseDPTaskDispatcher.getInstance().perform(entity))
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    getView().onCollectingRsp(0);
                }, e -> {
                    if (e != null && e instanceof BaseDPTaskException) {
                        getView().onCollectingRsp(((BaseDPTaskException) e).getErrorCode());
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void checkCollection(long time, int index) {
        Subscription subscription = Observable.just(new DPEntity()
                .setMsgId(511)
                .setUuid(uuid)
                .setAction(DBAction.QUERY)
                .setVersion(time / 1000 + index + 1)
                .setOption(DBOption.SingleQueryOption.ONE_BY_TIME))
                .subscribeOn(Schedulers.io())
                .flatMap(entity -> BaseDPTaskDispatcher.getInstance().perform(entity))
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null)
                .doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()))
                .subscribe(idpTaskResult -> {
                    DpMsgDefine.DPPrimary<Long> version = idpTaskResult.getResultResponse();
                    if (version != null && version.value != null) {
                        int delta = (int) Math.abs(time / 1000 - version.version);
                        mView.onItemCollectionCheckRsp(delta - 1 == index);
                        AppLogger.d("当前index:" + delta);
                    } else {
                        mView.onItemCollectionCheckRsp(false);
                    }
                    AppLogger.d("检查是否被收藏...: " + idpTaskResult.getResultCode());
                });
        addSubscription(subscription, "checkCollection" + index);
    }

    @Override
    public void stop() {
        super.stop();
    }
}
