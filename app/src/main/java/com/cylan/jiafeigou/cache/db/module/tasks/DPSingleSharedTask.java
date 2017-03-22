package com.cylan.jiafeigou.cache.db.module.tasks;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPSingleTask;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JfgCmdInsurance.getCmd;


/**
 * Created by yanzhendong on 2017/3/1.
 */

public class DPSingleSharedTask extends BaseDPTask<BaseDPTaskResult> {
    protected DBOption.SingleSharedOption option;
    private DpMsgDefine.DPWonderItem wonderItem;

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
    public Observable<BaseDPTaskResult> performServer(BaseDPTaskResult local) {
        return Observable.create((Observable.OnSubscribe<DpMsgDefine.DPWonderItem>) subscriber -> {
            try {
                wonderItem = DpUtils.unpackData(entity.getBytes(), DpMsgDefine.DPWonderItem.class);
                subscriber.onNext(wonderItem);
                subscriber.onCompleted();
            } catch (IOException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .map(item -> {
                    if (item == null) throw new BaseDPTaskException(-1, "分享条目不完整");
                    int result = -1;
                    try {
                        ArrayList<JFGDPMsg> req = new ArrayList<>(1);
                        JFGDPMsg msg602 = new JFGDPMsg(602, entity.getVersion());
                        msg602.packValue = entity.getBytes();
                        req.add(msg602);
                        result = (int) JfgCmdInsurance.getCmd().robotSetDataByTime("", req);
                        AppLogger.d("正在执行分享操作步骤一:设置602 DP消息" + result);
                    } catch (Exception e) {
                        AppLogger.d("分享操作步骤一操作失败!!!" + e.getMessage());
                        throw new BaseDPTaskException(-2, "分享步骤一失败");
                    }
                    if (result == -1) throw new BaseDPTaskException(-2, "分享步骤一失败");
                    return result;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnError(e -> mDPHelper.findDPMsg(entity.getUuid(), entity.getVersion(), entity.getMsgId()).subscribe(item -> mDPHelper.delete(item)))
                .flatMap(this::makeSetDataRspResponse)
                .map(rsp -> {
                    long result = -1;
                    int code = rsp.rets.get(0).ret;
                    if (code != 0) throw new BaseDPTaskException(code, "分享步骤一失败");
                    AppLogger.d("分享操作步骤一执行成功,正在执行步骤二:putFileToCloud");
                    try {
                        String remotePath = "/long/" +
                                Security.getVId(JFGRules.getTrimPackageName()) +//vid
                                "/" +
                                entity.getAccount() +//account
                                "/wonder/" +
                                wonderItem.cid + //cid
                                "/" +
                                wonderItem.fileName;
                        FutureTarget<File> future = Glide.with(ContextUtils.getContext())
                                .load(new JFGGlideURL(entity.getUuid(), wonderItem.fileName))
                                .downloadOnly(100, 100);
                        result = getCmd().putFileToCloud(remotePath, future.get().getAbsolutePath());

                    } catch (Exception e) {
                        AppLogger.d("分享操作步骤二操作失败,错误信息为:" + e.getMessage());
                        throw new BaseDPTaskException(-3, "分享步骤二失败");
                    }
                    if (result == -1) throw new BaseDPTaskException(-3, "分享步骤二失败");
                    return result;
                })
                .flatMap(this::makeHttpDoneResultResponse)
                .flatMap(rsp -> {
                    if (rsp.ret != 200) throw new BaseDPTaskException(rsp.ret, "分享步骤二失败");
                    AppLogger.d("分享操作步骤二执行成功,正在更新本地数据Version" + new Gson().toJson(entity));
                    return mDPHelper.findDPMsg(entity.getUuid(), entity.getVersion(), entity.getMsgId())
                            .flatMap(dpEntity -> {
                                dpEntity.setState(DBState.SUCCESS);
                                return mDPHelper.update(dpEntity);

                            });
                })
                .map(ent -> {
                    int result = -1;
                    try {
                        ArrayList<JFGDPMsg> req = new ArrayList<>(1);
                        JFGDPMsg msg511 = new JFGDPMsg(511, wonderItem.time);
                        msg511.packValue = DpUtils.pack(entity.getVersion());
                        req.add(msg511);
                        result = (int) JfgCmdInsurance.getCmd().robotSetDataByTime(entity.getUuid(), req);
                        AppLogger.d("正在执行分享操作步骤三:设置511 DP消息" + result);
                    } catch (Exception e) {
                        AppLogger.d("分享操作步骤三操作失败!!!" + e.getMessage());
                        throw new BaseDPTaskException(-4, "分享步骤三失败");
                    }
                    if (result == -1) throw new BaseDPTaskException(-4, "分享步骤三失败");
                    return result;
                })
                .flatMap(this::makeSetDataRspResponse)
                .map(rsp -> new BaseDPTaskResult().setResultCode(rsp.rets.get(0).ret).setResultResponse(rsp));
    }
}
