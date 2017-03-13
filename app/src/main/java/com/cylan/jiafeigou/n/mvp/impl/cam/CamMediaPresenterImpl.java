package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
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
                });
    }

    @Override
    public void collect(int index, DpMsgDefine.DPAlarm alarmMsg, GlideUrl bitmapGlideUrl) {
        Observable.create((Observable.OnSubscribe<IDPEntity>) subscriber -> {
            DpMsgDefine.DPWonderItem item = new DpMsgDefine.DPWonderItem();
            item.msgType = DpMsgDefine.DPWonderItem.TYPE_PIC;
            item.cid = uuid;
            Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
            item.place = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
            item.fileName = alarmMsg.time + "_1.jpg";
            item.time = alarmMsg.time;
            IDPEntity entity = new DPEntity()
                    .setUuid(uuid)
                    .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG)
                    .setVersion((long) alarmMsg.time * 1000L)
                    .setAction(DBAction.SHARED)
                    .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount())
                    .setOption(new DBOption.SingleSharedOption(1, 1))
                    .setBytes(item.toBytes());
            subscriber.onNext(entity);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.io())
                .flatMap(entity -> BaseDPTaskDispatcher.getInstance().perform(entity))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getResultCode() == 200) {
                        getView().savePicResult(true);
//                        ToastUtil.showPositiveToast(getString(R.string.Tap1_BigPic_FavoriteTips));
                    } else if (result.getResultCode() == 1050) {
//                        ToastUtil.showNegativeToast(getString(R.string.DailyGreatTips_Full));
                        getView().onErr(1050);
                    }
                }, e -> AppLogger.e("err: " + e.getLocalizedMessage()));
//        
//        Observable.just(alarmMsg)
//                .subscribeOn(Schedulers.newThread())
//                .subscribe((DpMsgDefine.DPAlarm alarm) -> {
//
//                    FutureTarget<File> future = Glide.with(ContextUtils.getContext())
//                            .load(bitmapGlideUrl)
//                            .downloadOnly(10, 10);
//                    try {
//                        File cacheFile = future.get();
//                        String path = cacheFile.getAbsolutePath();
//                        StringBuilder remotePath = new StringBuilder();
//                        remotePath.append("/long/")
//                                .append(Security.getVId(JFGRules.getTrimPackageName()))
//                                .append("/")
//                                .append(uuid)
//                                .append("/wonder/")
//                                .append(alarm.time)
//                                .append("_")
//                                .append(index)
//                                .append(".jpg");
//                        Log.d(TAG, "localPath:" + path);
//                        Log.d(TAG, "remotePath:" + remotePath);
//                        try {
//                            JfgCmdInsurance.getCmd().putFileToCloud(remotePath.toString(), path);
//                        } catch (JfgException e) {
//                            e.printStackTrace();
//                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    }
//                }, (Throwable th) -> AppLogger.e("" + th.getLocalizedMessage()));

    }

    @Override
    public void stop() {
        super.stop();
    }
}
