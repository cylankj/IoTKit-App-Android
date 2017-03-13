package com.cylan.jiafeigou.n.mvp.impl.home;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.db.DataBaseUtil;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineMessageContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineMessageBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class HomeMineMessagePresenterImp extends AbstractPresenter<HomeMineMessageContract.View> implements HomeMineMessageContract.Presenter {

    private boolean hasNewMesg;
    private DbManager dbManager;
    private ArrayList<MineMessageBean> results = new ArrayList<MineMessageBean>();
    private long seq;

    public HomeMineMessagePresenterImp(HomeMineMessageContract.View view, boolean hasNewMesg) {
        super(view);
        view.setPresenter(this);
//        this.hasNewMesg = hasNewMesg;
        this.hasNewMesg = true;
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                getAccount(),
                getMesgDpDataCallBack()
        };
    }

    /**
     * 加载消息数据
     */
    @Override
    public void initMesgData() {
        if (hasNewMesg) {
            getMesgDpData();
        } else {
            handlerDataResult(findAllFromDb());
        }
    }

    /**
     * 处理数据的显示
     *
     * @param list
     */
    private void handlerDataResult(ArrayList<MineMessageBean> list) {
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
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo account) {
                        if (account != null) {
                            // 加载数据库数据
                            dbManager = DataBaseUtil.getInstance(account.jfgAccount.getAccount()).dbManager;
                            initMesgData();
                        }
                    }
                });
    }

    /**
     * 获取到本地数据库中的所有消息记录
     *
     * @return
     */
    @Override
    public ArrayList<MineMessageBean> findAllFromDb() {
        ArrayList<MineMessageBean> tempList = new ArrayList<>();
        if (dbManager != null) {
            try {
                List<MineMessageBean> allData = dbManager.findAll(MineMessageBean.class);
                if (allData != null) {
                    tempList.addAll(allData);
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        return tempList;
    }

    /**
     * 清空本地消息记录
     */
    @Override
    public void clearRecoard() {
        try {
            dbManager.delete(MineMessageBean.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息保存到数据库
     *
     * @param bean
     */
    @Override
    public void saveIntoDb(MineMessageBean bean) {
        try {
            dbManager.save(bean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    /**
     * Dp获取到消息记录
     */
    @Override
    public void getMesgDpData() {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        try {
                            JFGDPMsg msg1 = new JFGDPMsg(601, System.currentTimeMillis());
                            JFGDPMsg msg4 = new JFGDPMsg(701, System.currentTimeMillis());
                            ArrayList<JFGDPMsg> params = new ArrayList<>();
                            params.add(msg1);
                            params.add(msg4);
                            seq = JfgCmdInsurance.getCmd().getInstance().robotGetData("", params, 100, false, 0);
                            AppLogger.d("getMesgDpData:" + seq);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getMesgDpData" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * Dp获取消息记录的回调
     *
     * @return
     */
    @Override
    public Subscription getMesgDpDataCallBack() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .subscribeOn(Schedulers.io())
//                .flatMap(new Func1<RobotoGetDataRsp, Observable<ArrayList<MineMessageBean>>>() {
//                    @Override
//                    public Observable<ArrayList<MineMessageBean>> call(RobotoGetDataRsp robotoGetDataRsp) {
//                        return null;
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<ArrayList<MineMessageBean>>() {
//                    @Override
//                    public void call(ArrayList<MineMessageBean> list) {
//
//                    }
//                });

                .map(new Func1<RobotoGetDataRsp, ArrayList<MineMessageBean>>() {
                    @Override
                    public ArrayList<MineMessageBean> call(RobotoGetDataRsp robotoGetDataRsp) {
                        if (results.size() != 0)
                            results.clear();
                        AppLogger.d("getMesgDpDataCallBack:" + robotoGetDataRsp.seq + ":" + robotoGetDataRsp.identity);
                        if (robotoGetDataRsp != null && robotoGetDataRsp.seq == seq) {
                            results.addAll(convertData(robotoGetDataRsp));
                        }
                        return results;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<MineMessageBean>>() {
                    @Override
                    public void call(ArrayList<MineMessageBean> list) {
                        if (list.size() != 0) {
                            handlerDataResult(list);
                        } else {
                            getView().showNoMesgView();
                        }
                    }
                });
    }

    /**
     * 解析转换数据
     *
     * @param robotoGetDataRsp
     */
    private ArrayList<MineMessageBean> convertData(RobotoGetDataRsp robotoGetDataRsp) {
        MineMessageBean bean;
        clearRecoard();
        ArrayList<MineMessageBean> results = new ArrayList<MineMessageBean>();
        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : robotoGetDataRsp.map.entrySet()) {
            if (entry.getValue() == null) continue;
            for (JFGDPMsg dp : entry.getValue()) {
                bean = new MineMessageBean();
                bean.type = entry.getKey();
                try {
                    if (bean.type == 701) {
                        DpMsgDefine.DPSystemMesg sysMesg = DpUtils.unpackData(dp.packValue, DpMsgDefine.DPSystemMesg.class);
                        if (sysMesg == null) continue;
                        bean.name = sysMesg.content.trim();
                        bean.content = sysMesg.title.trim();
                        bean.time = dp.version + "";
                        bean.isDone = 0;
                    } else {
                        DpMsgDefine.DPMineMesg mesg = DpUtils.unpackData(dp.packValue, DpMsgDefine.DPMineMesg.class);
                        if (mesg == null) continue;
                        bean.name = mesg.account.trim();
                        bean.isDone = mesg.isDone ? 1 : 0;
                        bean.content = mesg.cid.trim();
                        bean.time = dp.version + "";
                        bean.sn = mesg.sn;
                    }
                    results.add(bean);
                    saveIntoDb(bean);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sortAddReqList(results);
    }


    public ArrayList<MineMessageBean> sortAddReqList(ArrayList<MineMessageBean> list) {
        Comparator<MineMessageBean> comparator = new Comparator<MineMessageBean>() {
            @Override
            public int compare(MineMessageBean lhs, MineMessageBean rhs) {
                long oldTime = Long.parseLong(rhs.time + "");
                long newTime = Long.parseLong(lhs.time + "");
                return (int) (oldTime - newTime);
            }
        };
        Collections.sort(list, comparator);
        return list;
    }

}
