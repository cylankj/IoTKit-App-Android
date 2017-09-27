package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPSingleTask;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;


/**
 * Created by yanzhendong on 2017/3/1.
 */

public class DPSingleSharedTask extends BaseDPTask<BaseDPTaskResult> {
    protected DBOption.SingleSharedOption option;
    protected DpMsgDefine.DPWonderItem wonderItem;

    @Override
    public <R extends IDPSingleTask<BaseDPTaskResult>> R init(IDPEntity cache) throws Exception {
        option = cache.option(DBOption.SingleSharedOption.class);
        this.wonderItem = DpUtils.unpackData(cache.getBytes(), DpMsgDefine.DPWonderItem.class);
        return super.init(cache);
    }

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        if (!sourceManager.isOnline()) {
            return Observable.just(new BaseDPTaskResult().setResultCode(-1).setMessage("当前网络无法发生请求到服务器"));
        }
        AppLogger.w("正在执行离线收藏");
        return dpHelper.saveOrUpdate(entity.getUuid(), entity.getVersion(), entity.getMsgId(), entity.getBytes(), entity.action(), DBState.NOT_CONFIRM, option)
                .flatMap(entity -> {
                    try {
                        AppLogger.w("离线收藏步骤一,保存602消息,结果为:" + parser.toJson(entity));
                        DpMsgDefine.DPWonderItem wonderItem = DpUtils.unpackData(entity.getBytes(), DpMsgDefine.DPWonderItem.class);
                        if (wonderItem != null) {
                            AppLogger.w("离线收藏步骤二,保存511消息");
                            return dpHelper.saveOrUpdate(entity.getUuid(), (long) wonderItem.time, 511, DpUtils.pack(entity.getVersion()), DBAction.SAVED, DBState.SUCCESS, null)
                                    .map(ret -> wonderItem);
                        }
                    } catch (IOException e) {
                        AppLogger.d(e.getMessage());
                    }
                    throw new BaseDPTaskException(-1, "分享条目不完整");
                }).map(ret -> new BaseDPTaskResult().setResultCode(200).setResultResponse(ret));
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            long result = -1;
            try {
                ArrayList<JFGDPMsg> req = new ArrayList<>(1);
                JFGDPMsg msg602 = new JFGDPMsg(602, entity.getVersion());
                msg602.packValue = entity.getBytes();
                req.add(msg602);
                result = appCmd.robotSetDataByTime("", req);
                AppLogger.d("正在执行分享操作步骤一:设置602 DP消息" + result);
            } catch (Exception e) {
                AppLogger.d("分享操作步骤一操作失败!!!" + e.getMessage());
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
                    AppLogger.d("分享操作步骤一执行成功,正在执行步骤二:putFileToCloud");
                    try {
                        String remotePath = "/long/" +
                                Security.getVId() +//vid
                                "/" +
                                entity.getAccount() +//account
                                "/wonder/" +
                                wonderItem.cid + //cid
                                "/" +
                                wonderItem.fileName;
                        result = appCmd.putFileToCloud(remotePath, option.filePath);

                    } catch (Exception e) {
                        AppLogger.d("分享操作步骤二操作失败,错误信息为:" + e.getMessage());
                        throw new BaseDPTaskException(-3, "分享步骤二失败");
                    }
                    AppLogger.d("分享操作步骤二操作seq 为" + result);
                    if (result == -1) throw new BaseDPTaskException(-3, "分享步骤二失败");
                    return result;
                })
                .flatMap(this::makeHttpDoneResultResponse)
                .flatMap(rsp -> {
                    AppLogger.d("putFileToCloud 返回结果码为" + rsp.ret);
                    if (rsp.ret != 200) throw new BaseDPTaskException(rsp.ret, "分享步骤二失败");
                    AppLogger.d("分享操作步骤二执行成功,正在更新本地数据Version" + new Gson().toJson(entity));
                    return dpHelper.findDPMsg(entity.getUuid(), entity.getVersion(), entity.getMsgId())
                            .map(dpEntity -> {
                                AppLogger.d("更新本地数据成功");
                                dpEntity.setState(DBState.SUCCESS);
                                dpEntity.update();
                                return Observable.just(dpEntity);//不能返回null
                            });
                })
                .map(ent -> {
                    long result = -1;
                    try {
                        ArrayList<JFGDPMsg> req = new ArrayList<>(1);

                        JFGDPMsg msg511 = new JFGDPMsg(511, TimeUtils.wrapToLong(wonderItem.time));
                        msg511.packValue = DpUtils.pack(entity.getVersion());
                        req.add(msg511);
                        result = appCmd.robotSetDataByTime(entity.getUuid(), req);
                        AppLogger.d("正在执行分享操作步骤三:设置511 DP消息" + result);
                    } catch (Exception e) {
                        AppLogger.d("分享操作步骤三操作失败!!!" + e.getMessage());
                        throw new BaseDPTaskException(-4, "分享步骤三失败");
                    }
                    AppLogger.d("分享操作步骤三,seq 为:" + result);
                    if (result == -1) throw new BaseDPTaskException(-4, "分享步骤三失败");
                    return result;
                })
                .flatMap(this::makeSetDataRspResponse)
                .map(rsp -> {
                    BaseDPTaskResult result = new BaseDPTaskResult();
                    result.setResultCode(rsp.rets.get(0).ret);
                    result.setResultResponse(rsp);
                    return result;
                })
                .doOnError(e -> {
                    try {
                        AppLogger.d("分享任务出错了,正在清理本地数据" + e.getMessage());
                        ArrayList<JFGDPMsg> p511 = new ArrayList<>(1);
                        ArrayList<JFGDPMsg> p602 = new ArrayList<>(1);
                        p511.add(new JFGDPMsg(511, (long) wonderItem.time));
                        p602.add(new JFGDPMsg(602, entity.getVersion()));
                        appCmd.robotDelData(entity.getUuid(), p511, 0);
                        appCmd.robotDelData("", p602, 0);
                        dpHelper.findDPMsg(entity.getUuid(), entity.getVersion(), entity.getMsgId()).subscribe(DPEntity::delete, error -> AppLogger.d(error.getMessage()));
                        dpHelper.findDPMsg(entity.getUuid(), (long) wonderItem.time, 511).subscribe(DPEntity::delete, error -> AppLogger.d(error.getMessage()));
                    } catch (JfgException e1) {
                        AppLogger.d(e1.getMessage());
                    }

                });
    }
}
