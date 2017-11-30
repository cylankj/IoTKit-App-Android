package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/3.
 */

public class DPMultiDeleteTask extends BaseDPTask<BaseDPTaskResult> {
    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        AppLogger.w("执行删除:");
        if (!DataSourceManager.getInstance().isOnline()) {
            return Observable.just(new BaseDPTaskResult().setResultCode(-1).setMessage("当前网络无法发生请求到服务器"));
        }
        return Observable.from(multiEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(entity -> BaseDBHelper.getInstance().deleteDPMsgForce(entity.getAccount(), null, entity.getUuid(), entity.getVersion(), entity.getMsgId()))
                .buffer(multiEntity.size())
                .map(items -> new BaseDPTaskResult().setResultCode(0).setResultResponse(items));
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            AppLogger.w("正在执行批量删除操作, uuid 为:" + entity.getUuid());
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            JFGDPMsg msg;
            for (IDPEntity entity : multiEntity) {
                msg = new JFGDPMsg(entity.getMsgId(), entity.getVersion());
                msg.packValue = DpUtils.pack(0);
                params.add(msg);
            }
            try {
                long seq = Command.getInstance().robotDelData(entity.getUuid() == null ? "" : entity.getUuid(), params, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onCompleted();
                AppLogger.e("执行 task 出错了 ,错误信息为:" + e.getMessage());
            }
        })//1491922972000
                .subscribeOn(Schedulers.io())
                .flatMap(this::makeDeleteDataRspResponse)
                .flatMap(rsp -> performLocal());

//                        Observable.from(multiEntity)
//                                .flatMap(entity -> dpHelper.deleteDPMsgForce(entity.uuid(), null, entity.uuid(), entity.getVersion(), entity.getMsgId())).last()
//                                .map(cache -> new BaseDPTaskResult().setResultCode(rsp.resultCode).setResultResponse(multiEntity)));
    }
}
