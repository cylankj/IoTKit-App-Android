package com.cylan.jiafeigou.n.mvp.impl.home;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.DPEntityDao;
import com.cylan.jiafeigou.cache.db.module.SysMsgBean;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.mvp.contract.home.SysMessageContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class SysMessagePresenterImp extends AbstractPresenter<SysMessageContract.View> implements SysMessageContract.Presenter {


    public SysMessagePresenterImp(SysMessageContract.View view) {
        super(view);
    }

    @Override
    public void loadSystemMessageFromServer(long v601, long v701) {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                try {
                    if (v601 == 0 && v701 == 0) {
                        byte[] bytes = DpUtils.pack(0);
                        ArrayList<JFGDPMsg> marker = new ArrayList<JFGDPMsg>();
                        marker.add(new JFGDPMsg(1101L, 0, bytes));
                        marker.add(new JFGDPMsg(1103L, 0, bytes));
                        marker.add(new JFGDPMsg(1104L, 0, bytes));
                        Command.getInstance().robotSetData("", marker);
                    }

                    ArrayList<JFGDPMsg> params = new ArrayList<>();
                    params.add(new JFGDPMsg(601, v601));
                    params.add(new JFGDPMsg(701, v701));
                    long seq = Command.getInstance().robotGetData("", params, 15, false, 0);
                    Log.d(TAG, "getMesgDpData:" + seq);
                    subscriber.onNext(seq);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    AppLogger.e("getMesgDpData:" + e.getLocalizedMessage());
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class).filter(rsp -> {
                    Log.d(TAG, "seq:" + rsp.seq + ",before seq:" + seq);
                    return rsp.seq == seq;
                }))
                .first()
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .flatMap(robotoGetDataRsp -> {
                    Log.d(TAG, "getMesgDpData: robotoGetDataRsp");
                    if (robotoGetDataRsp == null) {
                        return findAllFromDb();
                    } else {
                        ArrayList<SysMsgBean> results = new ArrayList<>();
                        try {
                            for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : robotoGetDataRsp.map.entrySet()) {
                                for (JFGDPMsg msg : entry.getValue()) {
                                    SysMsgBean bean = new SysMsgBean();
                                    bean.type = (int) msg.id;
                                    if (bean.type == 701) {
                                        DpMsgDefine.DPSystemMesg sysMesg = convert(701, msg.packValue, DpMsgDefine.DPSystemMesg.class);
                                        if (sysMesg == null) {
                                            continue;
                                        }
                                        bean.name = sysMesg.content.trim();
                                        bean.content = sysMesg.title.trim();
                                        bean.time = msg.version;
                                        bean.isDone = 0;
                                    } else if (bean.type == 601) {
                                        if (msg.packValue == null) {
                                            continue;
                                        }
                                        Log.d(TAG, "byte/" + Arrays.toString(msg.packValue));
                                        DpMsgDefine.DPMineMesg mesg = convert(601, msg.packValue, DpMsgDefine.DPMineMesg.class);
                                        if (mesg == null) {
                                            continue;
                                        }
                                        bean.name = mesg.account.trim();
                                        bean.isDone = mesg.isDone ? 1 : 0;
                                        bean.content = mesg.cid.trim();
                                        bean.time = msg.version;
                                        bean.sn = mesg.sn;
                                        bean.pid = mesg.pid;
                                    }
                                    results.add(bean);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return Observable.just(results);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    mView.onQuerySystemMessageRsp(list);

                }, AppLogger::e);
        addSubscription(subscribe, "go");
    }

    @Override
    public void deleteSystemMessageFromServer(List<SysMsgBean> sysMsgBeans) {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                ArrayList<JFGDPMsg> jfgdpMsgs = new ArrayList<>(sysMsgBeans.size());
                byte[] pack = DpUtils.pack(0);
                for (SysMsgBean bean : sysMsgBeans) {
                    jfgdpMsgs.add(new JFGDPMsg(bean.type, bean.time, pack));
                }
                try {
                    long seq = Command.getInstance().robotDelData("", jfgdpMsgs, 0);
                    subscriber.onNext(seq);
                    subscriber.onCompleted();
                } catch (JfgException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class).filter(rsp -> rsp.seq == seq))
                .first()
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.DeleteDataRsp>() {
                    @Override
                    public void call(RxEvent.DeleteDataRsp deleteDataRsp) {
                        mView.onDeleteSystemMessageRsp(deleteDataRsp);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                    }
                });
        addStopSubscription(subscribe);
    }

    /**
     * 获取到本地数据库中的所有消息记录
     *
     * @return
     */
    public Observable<ArrayList<SysMsgBean>> findAllFromDb() {
        JFGAccount account = DataSourceManager.getInstance().getJFGAccount();
        if (account == null || TextUtils.isEmpty(account.getAccount())) {
            return null;
        }
        QueryBuilder<DPEntity> builder = BaseDBHelper.getInstance().getDpEntityQueryBuilder();
        builder.whereOr(DPEntityDao.Properties.MsgId.eq(601), DPEntityDao.Properties.MsgId.eq(701))
                .where(DPEntityDao.Properties.Account.eq(account.getAccount()));
        List<DPEntity> list = builder
                .orderDesc(DPEntityDao.Properties.Version)
                .list();
        AppLogger.d("load from db: " + ListUtils.getSize(list));
        if (list != null) {
            ArrayList<SysMsgBean> results = new ArrayList<>();
            for (DPEntity dp : list) {
                SysMsgBean bean = new SysMsgBean();
                bean.type = dp.getMsgId();
                try {
                    if (bean.type == 701) {
                        DpMsgDefine.DPSystemMesg sysMesg = convert(701, dp.getBytes(), DpMsgDefine.DPSystemMesg.class);
                        if (sysMesg == null) {
                            continue;
                        }
                        bean.name = sysMesg.content.trim();
                        bean.content = sysMesg.title.trim();
                        bean.time = dp.getVersion();
                        bean.isDone = 0;
                    } else if (bean.type == 601) {
                        if (dp.getBytes() == null) {
                            continue;
                        }
                        Log.d(TAG, "byte/" + Arrays.toString(dp.getBytes()));
                        DpMsgDefine.DPMineMesg mesg = convert(601, dp.getBytes(), DpMsgDefine.DPMineMesg.class);
                        if (mesg == null) {
                            continue;
                        }
                        bean.name = mesg.account.trim();
                        bean.isDone = mesg.isDone ? 1 : 0;
                        bean.content = mesg.cid.trim();
                        bean.time = dp.getVersion();
                        bean.sn = mesg.sn;
                        bean.pid = mesg.pid;
                    }
                    results.add(bean);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return Observable.just(results);
        }
        return Observable.empty();
    }

    public <T> T convert(int msgId, byte[] data, Class<T> clazz) {
        try {
            return DpUtils.unpackData(data, clazz);
        } catch (IOException e) {
            return null;
        }
    }

}
