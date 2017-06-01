package com.cylan.jiafeigou.n.view.panorama;

import android.support.v4.util.Pair;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

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
    public void upload(String fileName, String filePath) {
        Subscription subscribe = Observable.just(getRemoteFilePath(fileName))
                .observeOn(Schedulers.io())
                .map(remote -> {
                    int result = -1;
                    try {
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
        registerSubscription(subscribe);
    }

    private String getRemoteFilePath(String fileName) {
        return "/long/" + Security.getVId() + "/" + sourceManager.getAccount().getAccount() + "/wonder/" + uuid + "/" + fileName;
    }

    private DpMsgDefine.DPShareItem convert(PanoramaAlbumContact.PanoramaItem item) {
        DpMsgDefine.DPShareItem shareItem = new DpMsgDefine.DPShareItem();
        shareItem.cid = uuid;
        shareItem.msgType = item.type;
//        shareItem.desc
        return shareItem;
    }

    @Override
    public void share(PanoramaAlbumContact.PanoramaItem item, String desc) {
        Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                long seq = appCmd.getVideoShareUrl(getRemoteFilePath(item.fileName), desc, 0, item.type);
                AppLogger.e("获取 H5返回码为:" + seq);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.GetVideoShareUrlEvent.class))
                .map(h5 -> {
                    AppLogger.e("当前分享的 H5URL为:" + h5.url);
                    long seq = -1;
                    try {
                        DpMsgDefine.DPShareItem shareItem = new DpMsgDefine.DPShareItem();
                        shareItem.cid = uuid;
                        shareItem.msgType = item.type;
                        shareItem.desc = desc;
                        shareItem.fileName = item.fileName;
                        shareItem.url = h5.url;
                        shareItem.regionType = 0;
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    boolean success = false;
                    if (result != null && result.first != null && result.first.rets != null && result.first.rets.size() > 0) {
                        for (JFGDPMsgRet msgRet : result.first.rets) {
                            if (msgRet.id == 606) {
                                success = msgRet.ret == 0;
                                break;
                            }
                        }
                    }
                    success = success && result.second != null;
                    mView.onShareH5Result(success, success ? result.second : "");
                    AppLogger.d("AAAAA:" + new Gson().toJson(result));
                });

    }
}
