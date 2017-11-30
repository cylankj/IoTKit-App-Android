package com.cylan.jiafeigou.base;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/6/10.
 */

public class GetRequestBuilder extends AbstractRequestBuilder<RobotoGetDataRsp> {
    private RequestAdapter adapter;
    private int msgId;
    private String uuid;
    private long version;
    private int limit;
    private boolean asc;
    private int type;

    public GetRequestBuilder withMsgId(int msgId) {
        this.msgId = msgId;
        return this;
    }

    public GetRequestBuilder withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public GetRequestBuilder withVersion(long version) {
        this.version = version;
        return this;
    }

    public GetRequestBuilder withAsc(boolean asc) {
        this.asc = asc;
        return this;
    }


    public GetRequestBuilder withLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public GetRequestBuilder withType(int type) {
        this.type = type;
        return this;
    }


    public <T> Observable<List<T>> execute(Class<T> elementType) {
        return adapter().execute().cast(RobotoGetDataRsp.class)
                .map(getRsp -> {
                    List<T> result = new ArrayList<>();
                    return result;
                });
    }

    @Override
    public Observable<RobotoGetDataRsp> process() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                AppLogger.d("正在发送查询请求,uuid:" + uuid + "msgId:" + msgId + ",version:" + version + "count:" + limit + "acs:" + asc);
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                JFGDPMsg msg = new JFGDPMsg(msgId, version);
                params.add(msg);
                long seq = -1;
                if (type == 0) {
                    seq =  Command.getInstance().robotGetData(uuid, params, limit, asc, 0);//多请求一条数据,用来判断是否是一天最后一条
                } else if (type == 1) {
                    seq =  Command.getInstance().robotGetDataByTime(uuid, params, 0);
                }
                if (seq <= 0) {
                    throw new JfgException("内部错误");
                }
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                AppLogger.e(e.getMessage());
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class).first(rsp -> rsp.seq == seq));
    }

    @Override
    protected boolean checkParameter() {
        if (msgId == 0) {
            return false;
        }
        if (uuid == null) {
            uuid = "";
        }
        if (limit == 0) {
            limit = 20;
        }
        return true;
    }

}
