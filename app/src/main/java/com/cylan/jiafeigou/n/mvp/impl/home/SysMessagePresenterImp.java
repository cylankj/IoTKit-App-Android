package com.cylan.jiafeigou.n.mvp.impl.home;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.DPEntityDao;
import com.cylan.jiafeigou.cache.db.module.SysMsgBean;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.n.base.BaseApplication;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.rx.RxBus.getCacheInstance;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class SysMessagePresenterImp extends AbstractPresenter<SysMessageContract.View> implements SysMessageContract.Presenter {


    public SysMessagePresenterImp(SysMessageContract.View view) {
        super(view);
    }

//    @Override
//    protected Subscription[] register() {
//        return new Subscription[]{
//                getAccount()};
//    }

    @Override
    public void start() {
        super.start();
//        Subscription subscription =
//
//                findAllFromDb().subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(this::handlerDataResult, AppLogger::e);
        loadSystemMessageFromServer();
//        addSubscription(subscription, "go");
    }

    private void loadSystemMessageFromServer() {
        Subscription subscribe = getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(Schedulers.io())
                .map(accountArrived -> {
                    markMesgHasRead();
                    long seq = -1;
                    try {
                        JFGDPMsg msg1 = new JFGDPMsg(601, 0);
                        JFGDPMsg msg4 = new JFGDPMsg(701, 0);
                        ArrayList<JFGDPMsg> params = new ArrayList<>();
                        params.add(msg1);
                        params.add(msg4);
                        seq = BaseApplication.getAppComponent().getCmd().robotGetData("", params, 15, false, 0);
                        Log.d(TAG, "getMesgDpData:" + seq);
                    } catch (Exception e) {
                        AppLogger.e("getMesgDpData:" + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                    return seq;

                })
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
                        clearALLFromDB();
                        ArrayList<SysMsgBean> results = new ArrayList<>();
                        try {
                            for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : robotoGetDataRsp.map.entrySet()) {
                                for (JFGDPMsg msg : entry.getValue()) {
                                    SysMsgBean bean = new SysMsgBean();
                                    bean.type = (int) msg.id;
                                    if (bean.type == 701) {
                                        DpMsgDefine.DPSystemMesg sysMesg = convert(701, msg.packValue, DpMsgDefine.DPSystemMesg.class);
                                        if (sysMesg == null) continue;
                                        bean.name = sysMesg.content.trim();
                                        bean.content = sysMesg.title.trim();
                                        bean.time = msg.version;
                                        bean.isDone = 0;
                                    } else if (bean.type == 601) {
                                        if (msg.packValue == null) continue;
                                        Log.d(TAG, "byte/" + Arrays.toString(msg.packValue));
                                        DpMsgDefine.DPMineMesg mesg = convert(601, msg.packValue, DpMsgDefine.DPMineMesg.class);
                                        if (mesg == null) continue;
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
                            Collections.sort(results, (o1, o2) -> (int) (o2.time - o1.time));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        BaseApplication.getAppComponent().getDBHelper().saveDPByteInTx(robotoGetDataRsp);
                        return Observable.just(results);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (getView() != null) {
                        if (list.size() != 0) {
                            getView().hideNoMesgView();
                            getView().initRecycleView(list);
                        } else {
                            getView().showNoMesgView();
                            getView().initRecycleView(new ArrayList<>());
                        }
                    }
                }, AppLogger::e);
        addSubscription(subscribe, "go");
    }

    private void clearALLFromDB() {
        JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        if (account == null || TextUtils.isEmpty(account.getAccount())) return;
        QueryBuilder<DPEntity> builder = BaseApplication.getAppComponent().getDBHelper().getDpEntityQueryBuilder();
        builder.whereOr(DPEntityDao.Properties.MsgId.eq(601), DPEntityDao.Properties.MsgId.eq(701))
                .where(DPEntityDao.Properties.Account.eq(account.getAccount()));
        builder.buildDelete().executeDeleteWithoutDetachingEntities();
    }


    /**
     * 处理数据的显示
     *
     * @param list
     */
    private void handlerDataResult(ArrayList<SysMsgBean> list) {
        if (getView() != null) {
            if (list.size() != 0) {
                getView().hideNoMesgView();
                getView().initRecycleView(list);
            } else {
                getView().showNoMesgView();
                getView().initRecycleView(new ArrayList<>());
            }
        }
    }

    /**
     * 拿到数据库的操作对象
     *
     * @return
     */
    @Override
    public Subscription getAccount() {
        return getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(account -> {
                    if (account != null) {
                        // 加载数据库数据
                        getMesgDpData(account.jfgAccount.getAccount());
                        markMesgHasRead();
                    }
                }, AppLogger::e);
    }

    /**
     * 获取到本地数据库中的所有消息记录
     *
     * @return
     */
    public Observable<ArrayList<SysMsgBean>> findAllFromDb() {
        JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        if (account == null || TextUtils.isEmpty(account.getAccount())) return null;
        QueryBuilder<DPEntity> builder = BaseApplication.getAppComponent().getDBHelper().getDpEntityQueryBuilder();
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
                        if (sysMesg == null) continue;
                        bean.name = sysMesg.content.trim();
                        bean.content = sysMesg.title.trim();
                        bean.time = dp.getVersion();
                        bean.isDone = 0;
                    } else if (bean.type == 601) {
                        if (dp.getBytes() == null) continue;
                        Log.d(TAG, "byte/" + Arrays.toString(dp.getBytes()));
                        DpMsgDefine.DPMineMesg mesg = convert(601, dp.getBytes(), DpMsgDefine.DPMineMesg.class);
                        if (mesg == null) continue;
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

    /**
     * 清空本地消息记录
     */
    @Override
    public void deleteAllRecords() {
        JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        if (account != null && !TextUtils.isEmpty(account.getAccount())) {
            BaseApplication.getAppComponent().getDBHelper()
                    .deleteDpSync(account.getAccount(), null, 601);
            BaseApplication.getAppComponent().getDBHelper()
                    .deleteDpSync(account.getAccount(), null, 701);
        }
    }

    /**
     * Dp获取到消息记录
     */
    @Override
    public void getMesgDpData(String account) {
        Subscription subscription = Observable.just(null)
                .observeOn(Schedulers.io())
                .map(o -> {
                    long seq = -1;
                    try {
                        JFGDPMsg msg1 = new JFGDPMsg(601, 0);
                        JFGDPMsg msg4 = new JFGDPMsg(701, 0);
                        ArrayList<JFGDPMsg> params = new ArrayList<>();
                        params.add(msg1);
                        params.add(msg4);
                        seq = BaseApplication.getAppComponent().getCmd().robotGetData("", params, 15, false, 0);
                        Log.d(TAG, "getMesgDpData:" + seq);
                    } catch (Exception e) {
                        AppLogger.e("getMesgDpData:" + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                    return seq;
                })
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


                        return Observable.just(null);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(list -> {
                    if (list.size() != 0) {
                        handlerDataResult(list);
                    } else {
                        getView().showNoMesgView();
                    }
                    return list;
                })
                .subscribeOn(Schedulers.io())
                .subscribe(list -> {
                    //save in db
                    AppLogger.d("需要保存到数据库中");
                }, AppLogger::e);
        addSubscription(subscription, "getMesgDpData");
    }


    @Override
    public Subscription deleteMsgBack() {
        return getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteDataRspClass -> {
                    if (getView() != null) getView().deleteMesgReuslt(deleteDataRspClass);
                }, AppLogger::e);
    }

    @Override
    public void markMesgHasRead() {
        Observable.just("markMesgHasRead")
                .observeOn(Schedulers.io())
                .subscribe(new MarkAsReadTask(), AppLogger::e);
    }

    @Override
    public void removeItems(ArrayList<SysMsgBean> list) {
        Observable.just(list)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {
                    removeFromServer(o);
                    JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
                    if (!TextUtils.isEmpty(account.getAccount()))
                        for (SysMsgBean bean : o) {
                            BaseApplication.getAppComponent().getDBHelper().deleteDPMsgForce(
                                    account.getAccount(), null, null, bean.getTime(), bean.getType())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(ret -> AppLogger.d("本地删除"),
                                            AppLogger::e);
                        }
                }, AppLogger::e);
    }

    private void removeFromServer(ArrayList<SysMsgBean> beans) {
        ArrayList<JFGDPMsg> parse = parse(beans);
        List<DPEntity> dpEntities = new ArrayList<>(ListUtils.getSize(parse));
        for (SysMsgBean bean : beans) {
            DPEntity entity = new DPEntity();
            entity.setMsgId(bean.type);
            entity.setVersion(bean.time);
            entity.setAction(DBAction.DELETED);
            dpEntities.add(entity);
        }
        BaseApplication.getAppComponent().getTaskDispatcher()
                .perform(dpEntities)
                .subscribeOn(Schedulers.io())
                .subscribe(ret -> AppLogger.d("从服务器删除"),
                        AppLogger::e);
    }

    private ArrayList<JFGDPMsg> parse(ArrayList<SysMsgBean> list) {
        if (ListUtils.isEmpty(list)) return new ArrayList<>();
        ArrayList<JFGDPMsg> finalList = new ArrayList<>(list.size());
        for (SysMsgBean bean : list) {
            JFGDPMsg msg = new JFGDPMsg();
            msg.id = bean.type;
            msg.version = bean.time;
            finalList.add(msg);
        }
        return finalList;
    }


    /**
     * 静态内部类
     */
    private static class MarkAsReadTask implements Action1<String> {
        @Override
        public void call(String s) {
            try {
                ArrayList<JFGDPMsg> list = new ArrayList<JFGDPMsg>();
                JFGDPMsg msg1 = new JFGDPMsg(1101L, 0);
                JFGDPMsg msg2 = new JFGDPMsg(1103L, 0);
                JFGDPMsg msg3 = new JFGDPMsg(1104L, 0);
                msg1.packValue = DpUtils.pack(0);
                msg2.packValue = DpUtils.pack(0);
                msg3.packValue = DpUtils.pack(0);
                list.add(msg1);
                list.add(msg2);
                list.add(msg3);
                long req = BaseApplication.getAppComponent().getCmd().robotSetData("", list);
                AppLogger.d("mine_markHasRead:" + req);
            } catch (JfgException e) {
                AppLogger.e("mine_markHasRead:" + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

}
