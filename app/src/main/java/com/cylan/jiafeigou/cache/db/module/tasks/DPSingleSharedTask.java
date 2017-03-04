package com.cylan.jiafeigou.cache.db.module.tasks;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.view.IAction;
import com.cylan.jiafeigou.cache.db.view.IDPAction;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPSingleTask;
import com.cylan.jiafeigou.cache.db.view.IDPState;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JfgCmdInsurance.getCmd;


/**
 * Created by yanzhendong on 2017/3/1.
 */

public class DPSingleSharedTask extends BaseDPTask<BaseDPTaskResult> {
    protected IDPAction.DPSharedAction action;

    @Override
    public <R extends IDPSingleTask<BaseDPTaskResult>> R init(IDPEntity cache) {
        action = IAction.BaseAction.$(cache.getAction(), IDPAction.DPSharedAction.class);
        return super.init(cache);
    }

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        AppLogger.e("正在执行离线收藏");
        return mDPHelper.saveOrUpdate(null, null, singleEntity.getUuid(), singleEntity.getVersion(), singleEntity.getMsgId(), singleEntity.getBytes(), singleEntity.getAction(), IDPState.NOT_CONFIRM.state())
                .map(entity -> {
                    AppLogger.e("离线收藏结果为:" + new Gson().toJson(entity));
                    return new BaseDPTaskResult().setResultCode(200).setResultResponse(entity);
                });
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> req = new ArrayList<>(1);
                JFGDPMsg msg = new JFGDPMsg(singleEntity.getMsgId(), singleEntity.getVersion());
                msg.packValue = singleEntity.getBytes();
                req.add(msg);
                int result = (int) JfgCmdInsurance.getCmd().robotSetData(singleEntity.getUuid() == null ? "" : singleEntity.getUuid(), req);
                AppLogger.d("正在执行分享操作步骤一: robotSetData,seq:" + result);
                subscriber.onNext(result);
                subscriber.onCompleted();
            } catch (Exception e) {
                AppLogger.d("分享操作步骤一操作失败!!!" + e.getMessage());
                e.printStackTrace();
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(this::makeSetDataRspResponse)
                .flatMap(rsp -> {
                    int code = rsp.rets.get(0).ret;
                    if (code == 0) {
                        AppLogger.d("分享操作步骤一执行成功,正在执行步骤二:putFileToCloud");
                        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
                            long result = -1;
                            try {
                                String remotePath = "/long/" +
                                        Security.getVId(JFGRules.getTrimPackageName()) +
                                        "/" +
                                        singleEntity.getUuid() +
                                        "/wonder/" +
                                        singleEntity.getVersion() / 1000 +
                                        "_1.jpg";
                                FutureTarget<File> future = Glide.with(ContextUtils.getContext())
                                        .load(new JFGGlideURL(action.type, action.flag, singleEntity.getVersion() / 1000 + ".jpg", singleEntity.getUuid()))
                                        .downloadOnly(100, 100);
                                result = getCmd().putFileToCloud(remotePath, future.get().getAbsolutePath());
                                subscriber.onNext(result);
                                subscriber.onCompleted();

                            } catch (Exception e) {
                                subscriber.onError(e);
                                e.printStackTrace();
                                AppLogger.d("分享操作步骤二操作失败,错误信息为:" + e.getMessage());
                            }
                        }).flatMap(this::makeHttpDoneResultResponse)
                                .flatMap(ret -> {
                                    AppLogger.d("分享操作步骤二执行成功,正在删除本地数据");
                                    return mDPHelper.deleteDPMsgForce(null, null, singleEntity.getUuid(), singleEntity.getVersion(), singleEntity.getMsgId())//因为无法知道收藏成功的条目的服务器 version, 因此需要将本地记录删除
                                            .map(entity -> new BaseDPTaskResult().setResultCode(ret.ret).setResultResponse(ret.result));
                                });
                    } else {
                        return Observable.just(new BaseDPTaskResult().setResultCode(code));
                    }
                });
    }
}
