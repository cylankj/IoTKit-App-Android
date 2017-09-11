package com.cylan.jiafeigou.n.view.panorama;

import android.support.v4.util.Pair;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskException;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/5/27.
 */

public class PanoramaSharePresenter extends BasePresenter<PanoramaShareContact.View> implements PanoramaShareContact.Presenter {
    @Override
    @Deprecated
    public void check(String uuid, int time) {
        Subscription subscribe = Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                JFGDPMsg msg = new JFGDPMsg(511, time);
                params.add(msg);
                long seq = appCmd.robotGetDataByTime(uuid, params, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class).first(rsp -> rsp.seq == ret))
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (ret != null && ret.map != null && ret.map.size() > 0 && ret.map.get(511) != null) {
                        ArrayList<JFGDPMsg> jfgdpMsgs = ret.map.get(511);
                        if (jfgdpMsgs.size() > 0) {
                            JFGDPMsg msg = jfgdpMsgs.get(0);
                            try {
                                Long version = DpUtils.unpackData(msg.packValue, Long.class);
                                if (version != null && version > 0) {
                                    mView.onUploadResult(200);
                                    return;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    mView.onUploadResult(-1);
                }, e -> {
                    AppLogger.e(e.getMessage());
                    mView.onUploadResult(-1);
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP,"PanoramaSharePresenter#check", subscribe);
    }

    @Override
    public void upload(String fileName, String filePath) {
        Subscription subscribe = Observable.just("upload")
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(cmd -> getRemoteFilePath(fileName, true))
                .observeOn(Schedulers.io())
                .map(remote -> {
                    int result = -1;
                    try {
                        AppLogger.d("H5分享的远程路径为:" + remote);
                        result = appCmd.putFileToCloud(remote, filePath);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    AppLogger.d("上传返回码为:" + result);
                    return result;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(JFGMsgHttpResult.class)
                        .filter(ret -> ret.requestId == seq)
                        .first())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    AppLogger.e("上传的结果为:" + new Gson().toJson(result));
                    mView.onUploadResult(result.ret);
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP,"PanoramaSharePresenter#upload", subscribe);
    }

    private String getRemoteFilePath(String fileName, boolean hasPrefix) {
        if (sourceManager.getAccount().getAccount() == null) return "";
        return (hasPrefix ? "/" : "") + "long/" + Security.getVId() + "/" + sourceManager.getAccount().getAccount() + "/wonder/" + uuid + "/" + fileName;
    }

    @Override
    public void share(PanoramaAlbumContact.PanoramaItem item, String desc, String thumbPath) {
        Subscription subscribe = Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                String remoteFilePath = getRemoteFilePath(item.fileName, false);
                long seq = appCmd.getVideoShareUrl(remoteFilePath, desc, DataSourceManager.getInstance().getStorageType(), item.type);
                AppLogger.e("获取 H5返回码为:" + seq + ",remoteFilePath:" + remoteFilePath + ",type" + item.type + ",storageType" + DataSourceManager.getInstance().getStorageType());
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.GetVideoShareUrlEvent.class).first())
                .map(h5 -> {
                    AppLogger.e("当前分享的 H5URL为:" + h5.url);
                    long seq = -1;
                    try {
                        DpMsgDefine.DPShareItem shareItem = new DpMsgDefine.DPShareItem();
                        shareItem.cid = uuid;
                        shareItem.msgType = item.type == PanoramaAlbumContact.PanoramaItem.PANORAMA_ITEM_TYPE.TYPE_PICTURE ? 0 : 1;
                        shareItem.desc = desc;
                        shareItem.fileName = item.fileName;
                        shareItem.url = h5.url;
                        shareItem.regionType = DataSourceManager.getInstance().getStorageType();
                        shareItem.time = item.time;
                        ArrayList<JFGDPMsg> params = new ArrayList<>();
                        JFGDPMsg msg = new JFGDPMsg(606, 0);
                        msg.packValue = shareItem.toBytes();
                        params.add(msg);
                        seq = appCmd.robotSetData("", params);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(e.getMessage());
                    }
                    return new Pair<>(seq, h5.url);
                })
                .flatMap(pair -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class)
                        .first(rsp -> rsp.seq == pair.first)
                        .map(rsp -> new Pair<>(rsp, pair.second))
                        .timeout(30, TimeUnit.SECONDS, Observable.just(new Pair<>(null, pair.second))))
                .map(rsp -> {
                    long result = -1;
                    try {
                        int code = rsp.first.rets.get(0).ret;
                        if (code != 0) throw new BaseDPTaskException(code, "分享失败");
                        JFGDPMsg msg = new JFGDPMsg(511, item.time);
                        msg.packValue = DpUtils.pack(rsp.first.rets.get(0).version);
                        ArrayList<JFGDPMsg> params = new ArrayList<>();
                        params.add(msg);
                        result = appCmd.robotSetDataByTime(uuid, params);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return new Pair<>(result, rsp.second);
                })
                .flatMap(pair -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class)
                        .first(rsp -> rsp.seq == pair.first)
                        .map(rsp -> new Pair<>(rsp, pair.second))
                        .timeout(30, TimeUnit.SECONDS, Observable.just(new Pair<>(null, pair.second))))
                .map(rsp -> {
                    long result = -1;
                    int code = rsp.first.rets.get(0).ret;
                    if (code != 0) throw new BaseDPTaskException(code, "分享步骤一失败");
                    AppLogger.d("分享操作步骤一执行成功,正在执行步骤二:putFileToCloud");
                    try {
                        String remotePath = getRemoteFilePath(item.fileName, true);
                        result = appCmd.putFileToCloud(remotePath, thumbPath);

                    } catch (Exception e) {
                        AppLogger.d("分享操作步骤二操作失败,错误信息为:" + e.getMessage());
                        throw new BaseDPTaskException(-3, "分享步骤二失败");
                    }
                    AppLogger.d("分享操作步骤二操作seq 为" + result);
                    if (result == -1) throw new BaseDPTaskException(-3, "分享步骤二失败");
                    return new Pair<>(result, rsp.second);
                })
                .flatMap(pair -> RxBus.getCacheInstance().toObservable(JFGMsgHttpResult.class)
                        .filter(ret -> ret.requestId == pair.first)
                        .first().map(rsp -> new Pair<>(rsp, pair.second)))

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    boolean success = result != null && result.first.ret == 200 && result.second != null;
                    mView.onShareH5Result(success, success ? result.second : "");
                    AppLogger.d("AAAAA:" + new Gson().toJson(result));
                }, e -> {
                    mView.onShareH5Result(false, "");
                    AppLogger.e(e.getMessage());
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP,"PanoramaSharePresenter#share", subscribe);

    }
}
