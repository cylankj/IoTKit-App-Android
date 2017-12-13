package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskException;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMediaContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;

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
        super(view);
    }

    private void save(Bitmap bitmap) {
        Observable.just(bitmap)
                .filter(new RxHelper.Filter<>("", bitmap != null))
                .subscribeOn(Schedulers.io())
                .map((Bitmap bMap) -> {
                    String fileName = System.currentTimeMillis() + ".png";
                    String filePath = JConstant.MEDIA_PATH + File.separator;
                    BitmapUtils.saveBitmap2file(bMap, filePath + fileName);
                    MiscUtils.insertImage(filePath, fileName);
                    return true;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(r -> getView().savePicResult(r), AppLogger::e);
    }

    @Override
    public void saveImage(String url) {
        GlideApp.with(getView().getContext())//注意contxt
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        Log.d(TAG, "onResourceReady:" + (resource == null));
                        save(resource);
                    }
                });
    }

    @Override
    public void unCollect(int index, long v, CamMessageBean bean) {//1492151447 1492151447
        long finalVersion = MiscUtils.getFinalVersion(bean, index + 1);
        Log.d("finalVersion", "unCollect finalVersion:" + finalVersion + ",index:" + index);
        check(finalVersion)
                .filter(v602 -> {
                    if (v602 <= 0) {
                        throw new BaseDPTaskException(-1, "无法查找当前条目对应的 V602 Version");
                    }
                    return true;
                })
                .map(v602 -> new DPEntity()
                        .setUuid("")
                        .setVersion(v602)//错了
                        .setAction(DBAction.DELETED)
                        .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG))
                .flatMap(task -> BaseDPTaskDispatcher.getInstance().perform(task))
                .map(ret -> new DPEntity()
                        .setUuid(uuid)
                        .setVersion(finalVersion)
                        .setAction(DBAction.DELETED)
                        .setMsgId(511))
                .flatMap(task -> BaseDPTaskDispatcher.getInstance().perform(task))
                .doOnError(throwable -> {
                    if (throwable instanceof BaseDPTaskException) {
                        int code = ((BaseDPTaskException) throwable).getErrorCode();
                        if (code == -1) {
                            mView.onItemCollectionCheckRsp(false);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null)
                .subscribe(result -> {
                    if (result.getResultCode() == 0 && mView.getCurrentIndex() == index) {//成功了
                        AppLogger.d("取消收藏成功");
                        mView.onItemCollectionCheckRsp(false);
                    }
                }, AppLogger::e);
    }


    public Observable<Long> check(long v511) {
        return Observable.just(new DPEntity()
                .setMsgId(511)
                .setUuid(uuid)
                .setAction(DBAction.QUERY)
                .setVersion(v511)
                .setOption(DBOption.SingleQueryOption.ONE_BY_TIME))
                .flatMap(entity -> BaseDPTaskDispatcher.getInstance().perform(entity))
                .map(ret -> {
                    DpMsgDefine.DPPrimary<Long> version = ret.getResultResponse();
                    if (version != null) {
                        return version.value;
                    } else {
                        return -1L;
                    }
                });
    }

    @Override
    public void collect(int index, long version, CamMessageBean bean) {
        long finalVersion = MiscUtils.getFinalVersion(bean, index + 1);
        Log.d("finalVersion", "collect finalVersion:" + finalVersion + ",index:" + index);

        Subscription subscribe = Observable.create((Observable.OnSubscribe<File>) subscriber -> {
            GlideApp.with(ContextUtils.getContext())
                    .downloadOnly()
                    .load(MiscUtils.getCamWarnUrlV2(uuid, bean, index + 1))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new SimpleTarget<File>() {
                        @Override
                        public void onResourceReady(File resource, Transition<? super File> transition) {
                            Log.d("CamMediaPresenterImpl", "onResourceReady:" + resource);
                            subscriber.onNext(resource);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            Log.d("CamMediaPresenterImpl", "onLoadFailed");
                            subscriber.onError(new RxEvent.HelperBreaker());
                        }
                    });


        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(file -> {
                    DpMsgDefine.DPWonderItem item = new DpMsgDefine.DPWonderItem();
                    item.msgType = DpMsgDefine.DPWonderItem.TYPE_PIC;
                    item.cid = uuid;
                    Device device = DataSourceManager.getInstance().getDevice(uuid);
                    item.place = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
                    item.fileName = MiscUtils.getFileName(bean, index + 1);
                    item.time = (int) (finalVersion / 1000);
                    return new DPEntity()
                            .setUuid(uuid)
                            .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG)
                            .setVersion(System.currentTimeMillis())
                            .setOption(new DBOption.SingleSharedOption(1, 1, file.getAbsolutePath()))
                            .setAction(DBAction.SHARED)
                            .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount())
                            .setBytes(item.toBytes());
                })
                .flatMap(entity -> BaseDPTaskDispatcher.getInstance().perform(entity))
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    getView().onCollectingRsp(0);
                }, e -> {
                    if (e != null && e instanceof BaseDPTaskException) {
                        getView().onCollectingRsp(((BaseDPTaskException) e).getErrorCode());
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    } else {
                        getView().onCollectingRsp(-1);
                    }
                    if (e != null) {
                        e.printStackTrace();
                    }
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void checkCollection(long time, int index, CamMessageBean bean) {
        long finalVersion = MiscUtils.getFinalVersion(bean, index + 1); //time / 1000 + index + 1) * 1000L;
        Log.d("finalVersion", "check finalVersion:" + finalVersion + ",index:" + index);
        Subscription subscription = Observable.just(new DPEntity()
                .setMsgId(511)
                .setUuid(uuid)
                .setAction(DBAction.QUERY)
                .setVersion(finalVersion)
                .setOption(DBOption.SingleQueryOption.ONE_BY_TIME))
                .subscribeOn(Schedulers.io())
                .flatMap(entity -> BaseDPTaskDispatcher.getInstance().perform(entity))
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null)
                .doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()))
                .subscribe(idpTaskResult -> {
                    DpMsgDefine.DPPrimary<Long> version = idpTaskResult.getResultResponse();
                    if (version != null && version.value != null) {
                        MiscUtils.getDelta(bean, version.version);
                        mView.onItemCollectionCheckRsp(true);
//                        AppLogger.d("当前index:" + delta);
                    } else {
                        mView.onItemCollectionCheckRsp(false);
                    }
                    AppLogger.d("检查是否被收藏...: " + idpTaskResult.getResultCode());
                }, AppLogger::e);
        addSubscription(subscription, "checkCollection" + index);
    }

    @Override
    public void stop() {
        super.stop();
    }
}
