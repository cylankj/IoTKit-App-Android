package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMediaContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.CamWarnGlideURL;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-12-28.
 */

public class CamMediaPresenterImpl extends AbstractPresenter<CamMediaContract.View>
        implements CamMediaContract.Presenter {
    private String uuid;

    public CamMediaPresenterImpl(CamMediaContract.View view, String uuid) {
        super(view);
        this.uuid = uuid;
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
        Observable.just(alarmMsg)
                .subscribeOn(Schedulers.newThread())
                .subscribe((DpMsgDefine.DPAlarm alarm) -> {
                    JFGDevice device = GlobalDataProxy.getInstance().fetch(uuid);
                    String alias = device == null ? "" : device.alias;
                    DpMsgDefine.DPWonderItem item = new DpMsgDefine.DPWonderItem();
                    item.cid = uuid;
                    item.msgType = DpMsgDefine.DPWonderItem.TYPE_PIC;
                    item.place = alias;
                    item.fileName = alarm.time + "_" + index + ".jpg";
                    item.time = alarm.time;
                    ArrayList<JFGDPMsg> jfgdpMsgs = new ArrayList<>(1);
                    JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, alarm.time);
                    msg.packValue = item.toBytes();
                    jfgdpMsgs.add(msg);
                    try {
                        JfgCmdInsurance.getCmd().robotSetData(uuid, jfgdpMsgs);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    FutureTarget<File> future = Glide.with(ContextUtils.getContext())
                            .load(bitmapGlideUrl)
                            .downloadOnly(10, 10);
                    try {
                        File cacheFile = future.get();
                        String path = cacheFile.getAbsolutePath();
                        StringBuilder remotePath = new StringBuilder();
                        remotePath.append("/long/")
                                .append(Security.getVId(JFGRules.getTrimPackageName()))
                                .append("/")
                                .append(uuid)
                                .append("/wonder/")
                                .append(alarm.time)
                                .append("_")
                                .append(index)
                                .append(".jpg");
                        Log.d(TAG, "localPath:" + path);
                        Log.d(TAG, "remotePath:" + remotePath);
                        try {
                            JfgCmdInsurance.getCmd().putFileToCloud(remotePath.toString(), path);

                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }, (Throwable th) -> AppLogger.e("" + th.getLocalizedMessage()));

    }

    @Override
    public void stop() {
        super.stop();
    }
}
