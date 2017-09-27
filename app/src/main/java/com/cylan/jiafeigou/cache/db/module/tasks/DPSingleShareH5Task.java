package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/5/27.
 */

public class DPSingleShareH5Task extends BaseDPTask<BaseDPTaskResult> {
    protected DBOption.SingleSharedOption option;
    protected DpMsgDefine.DPShareItem shareItem;

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        return Observable.just(BaseDPTaskResult.SUCCESS);
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            long result = -1;
            try {
                ArrayList<JFGDPMsg> req = new ArrayList<>(1);
                JFGDPMsg msg602 = new JFGDPMsg(606, entity.getVersion());
                msg602.packValue = entity.getBytes();
                req.add(msg602);
                result = appCmd.robotSetDataByTime("", req);
                AppLogger.w("正在执行分享操作步骤一:设置606 DP消息" + result);
            } catch (Exception e) {
                AppLogger.w("分享操作步骤一操作失败!!!" + e.getMessage());
                throw new BaseDPTaskException(-2, "分享步骤一失败");
            }
            subscriber.onNext(result);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.io())
                .flatMap(this::makeSetDataRspResponse)
                .map(rsp -> {
                    long result = -1;
                    int code = rsp.rets.get(0).ret;
                    if (code != 0) throw new BaseDPTaskException(code, "分享步骤一失败");
                    AppLogger.w("分享操作步骤一执行成功,正在执行步骤二:putFileToCloud");
                    try {
                        String remotePath = "/long/" +
                                Security.getVId() +//vid
                                "/" +
                                entity.getAccount() +//account
                                "/wonder/" +
                                shareItem.cid + //cid
                                "/" +
                                shareItem.fileName;
                        result = appCmd.putFileToCloud(remotePath, option.filePath);

                    } catch (Exception e) {
                        AppLogger.d("分享操作步骤二操作失败,错误信息为:" + e.getMessage());
                        throw new BaseDPTaskException(-3, "分享步骤二失败");
                    }
                    AppLogger.w("分享操作步骤二操作seq 为" + result);
                    if (result == -1) throw new BaseDPTaskException(-3, "分享步骤二失败");
                    return result;
                })
                .flatMap(this::makeHttpDoneResultResponse)
                .flatMap(rsp -> {
                    AppLogger.w("putFileToCloud 返回结果码为" + rsp.ret);
                    if (rsp.ret != 200) throw new BaseDPTaskException(rsp.ret, "分享步骤二失败");
                    AppLogger.w("分享操作步骤二执行成功,正在更新本地数据Version" + new Gson().toJson(entity));
                    return dpHelper.findDPMsg(entity.getUuid(), entity.getVersion(), entity.getMsgId())
                            .map(dpEntity -> {
                                AppLogger.w("更新本地数据成功");
                                dpEntity.setState(DBState.SUCCESS);
                                dpEntity.update();
                                BaseDPTaskResult result = new BaseDPTaskResult();
                                result.setResultCode(rsp.ret);
                                result.setResultResponse(rsp);
                                return result;
                            });
                })
                .doOnError(e -> {
                    try {
                        AppLogger.d("分享任务出错了,正在清理本地数据" + e.getMessage());
                        ArrayList<JFGDPMsg> p606 = new ArrayList<>(1);
                        p606.add(new JFGDPMsg(606, entity.getVersion()));
                        appCmd.robotDelData("", p606, 0);
                        dpHelper.findDPMsg(entity.getUuid(), entity.getVersion(), entity.getMsgId()).subscribe(DPEntity::delete, error -> AppLogger.d(error.getMessage()));
                    } catch (JfgException e1) {
                        AppLogger.d(e1.getMessage());
                    }

                });
    }
}
