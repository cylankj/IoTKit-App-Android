package com.cylan.jiafeigou.cache.db.module.tasks;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPSingleTask;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;

import java.io.File;
import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JfgCmdInsurance.getCmd;


/**
 * Created by yanzhendong on 2017/3/1.
 */

public class DPSingleSharedTask extends BaseDPTask<BaseDPTaskResult> {
    protected DBOption.SingleSharedOption option;

    @Override
    public <R extends IDPSingleTask<BaseDPTaskResult>> R init(IDPEntity cache) {
        option = cache.option(DBOption.SingleSharedOption.class);
        return super.init(cache);
    }

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        AppLogger.d("正在执行离线收藏");
        return mDPHelper.saveOrUpdate(entity.getUuid(), entity.getVersion(), entity.getMsgId(), entity.getBytes(), entity.action(), DBState.NOT_CONFIRM, option)
                .map(entity -> {
                    AppLogger.d("离线收藏结果为:" + parser.toJson(entity));
                    return new BaseDPTaskResult().setResultCode(200).setResultResponse(entity);
                });
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> req = new ArrayList<>(1);
                JFGDPMsg msg = new JFGDPMsg(entity.getMsgId(), entity.getVersion());
                msg.packValue = entity.getBytes();
                req.add(msg);
                int result = (int) JfgCmdInsurance.getCmd().robotSetData(entity.getUuid() == null ? "" : entity.getUuid(), req);
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
                                        entity.getUuid() +
                                        "/wonder/" +
                                        entity.getVersion() / 1000 +
                                        "_1.jpg";
                                FutureTarget<File> future = Glide.with(ContextUtils.getContext())
                                        .load(new JFGGlideURL(entity.getUuid(), option.type, option.flag, entity.getVersion() / 1000 + ".jpg", entity.getUuid()))
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
                                    AppLogger.d("分享操作步骤二执行成功,正在更新本地数据Version");
                                    return mDPHelper.findDPMsg(entity.getUuid(), entity.getVersion(), entity.getMsgId()).map(dpEntity -> {
                                        dpEntity.setVersion(rsp.rets.get(0).version);
                                        dpEntity.update();
                                        return new BaseDPTaskResult().setResultCode(ret.ret).setResultResponse(ret.result);
                                    });
                                });
                    } else {
                        return mDPHelper.findDPMsg(entity.getUuid(), entity.getVersion(), entity.getMsgId()).map(dpEntity -> {
                            AppLogger.d("分享失败,正在删除本地数据");
                            dpEntity.delete();
                            return new BaseDPTaskResult().setResultCode(code);
                        });
                    }
                });
    }
}
