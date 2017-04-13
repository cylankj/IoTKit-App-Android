package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskException;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMediaContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.CamWarnGlideURL;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;

import java.io.File;
import java.util.concurrent.ExecutionException;

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
    public void unCollect(int index, long version) {
        long targetTime = version / 1000 + (index + 1);
        Observable.just(version)
                .observeOn(Schedulers.io())
                .map(ver -> new DPEntity()
                        .setUuid("")
                        .setVersion(targetTime)
                        .setAction(DBAction.DELETED)
                        .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG))
                .flatMap(task -> BaseApplication.getAppComponent().getTaskDispatcher().perform(task))
                .map(ret -> new DPEntity()
                        .setUuid(uuid)
                        .setVersion(targetTime)
                        .setAction(DBAction.DELETED)
                        .setMsgId(511))
                .flatMap(task -> BaseApplication.getAppComponent().getTaskDispatcher().perform(task))
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null)
                .subscribe(result -> {
                    if (result.getResultCode() == 0 && mView.getCurrentIndex() == index) {//成功了
                        AppLogger.d("取消收藏成功");
                        mView.onItemCollectionCheckRsp(false);
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
            Device device = BaseApplication.getAppComponent().getSourceManager().getJFGDevice(uuid);
            item.place = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
            item.fileName = version / 1000 + "_" + (index + 1) + ".jpg";
            item.time = (int) (version / 1000) + index + 1;//
            FutureTarget<File> future = Glide.with(ContextUtils.getContext())
                    .load(new JFGGlideURL(uuid, item.fileName))
                    .downloadOnly(100, 100);
            String filePath = null;
            try {
                filePath = future.get().getAbsolutePath();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            IDPEntity entity = new DPEntity()
                    .setUuid(uuid)
                    .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG)
                    .setVersion(System.currentTimeMillis())
                    .setOption(new DBOption.SingleSharedOption(1, 1, filePath))
                    .setAction(DBAction.SHARED)
                    .setAccount(BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount())
                    .setBytes(item.toBytes());
            subscriber.onNext(entity);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.io())
                .flatMap(entity -> BaseApplication.getAppComponent().getTaskDispatcher().perform(entity))
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
                .flatMap(entity -> BaseApplication.getAppComponent().getTaskDispatcher().perform(entity))
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
                }, AppLogger::e);
        addSubscription(subscription, "checkCollection" + index);
    }

    @Override
    public void stop() {
        super.stop();
    }
}
