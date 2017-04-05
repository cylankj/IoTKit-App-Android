package com.cylan.jiafeigou.cache.db.module.tasks;

import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.log.AppLogger;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JfgCmdInsurance.getCmd;

/**
 * Created by yanzhendong on 2017/3/2.
 */

public class DPSimpleMultiQueryTask extends BaseDPTask<BaseDPTaskResult> {
    private DBOption.SimpleMultiDpQueryOption option;

    public DPSimpleMultiQueryTask() {
    }

    public Observable<BaseDPTaskResult> run() {
        if (DataSourceManager.getInstance().isOnline())
            return performServer();
        else return performLocal();
    }

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        List<Integer> list = new ArrayList<>();
        for (IDPEntity entity : multiEntity) {
            list.add(entity.getMsgId());
            Log.d("DPSimpleMultiQueryTask", "DPSimpleMultiQueryTask: " + entity);
        }
        QueryBuilder<DPEntity> builder = ((BaseDBHelper) mDPHelper).buildDPMsgQueryBuilder(multiEntity.get(0).getAccount(),
                OptionsImpl.getServer(), multiEntity.get(0).getUuid(), null, null,
                list, null, null, null);
        return mDPHelper.queryDpMsg(builder)
                .map(ret -> {
                    Object result = null;
                    if (ret != null && DBAction.AVAILABLE.accept(ret.action())) {
                        result = propertyParser.parser(ret.getMsgId(), ret.getBytes(), ret.getVersion());
                    }
                    return new BaseDPTaskResult().setResultCode(0).setResultResponse(result);
                });
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                String uuid = multiEntity.get(0).getUuid();
                option = multiEntity.get(0).option(DBOption.SimpleMultiDpQueryOption.class);
                AppLogger.d("正在发送查询请求,uuid:" + multiEntity.get(0));
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                for (int i = 0; i < multiEntity.size(); i++) {
                    JFGDPMsg msg = new JFGDPMsg(multiEntity.get(i).getMsgId(), multiEntity.get(i).getVersion());
                    params.add(msg);
                }
                long seq = -1;
                seq = getCmd().robotGetData(uuid, params, option.limit, option.asc, 0);//多请求一条数据,用来判断是否是一天最后一条
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                AppLogger.e(e.getMessage());
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .filter(seq -> seq > 0)
                .flatMap(this::makeGetDataRspResponse)
                .flatMap(rsp -> {
                    AppLogger.d("收到从服务器返回数据!!!");
                    return performLocal();
                });
    }
}
