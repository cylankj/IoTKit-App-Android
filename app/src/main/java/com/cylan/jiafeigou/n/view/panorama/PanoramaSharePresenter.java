package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

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
        Subscription subscribe = Observable.just("/long/" + Security.getVId() + "/" + sourceManager.getAccount().getAccount() + "/wonder/" + uuid + "/" + fileName)
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

    private DpMsgDefine.DPShareItem convert(PanoramaAlbumContact.PanoramaItem item) {
        DpMsgDefine.DPShareItem shareItem = new DpMsgDefine.DPShareItem();
        shareItem.cid = uuid;
        shareItem.msgType = item.type;
//        shareItem.desc
        return null;
    }

    @Override
    public void share() {

    }
}
