package com.cylan.jiafeigou.n.mvp.impl.mag;

import com.cylan.jiafeigou.n.db.DataBaseUtil;
import com.cylan.jiafeigou.n.mvp.contract.mag.MagLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MagBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/10/20
 * 描述：
 */
public class MagLivePresenterImp extends AbstractPresenter<MagLiveContract.View> implements MagLiveContract.Presenter {

    private DbManager dbManager;

    private CompositeSubscription compositeSubscription;

    public MagLivePresenterImp(MagLiveContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getAccount());
        }
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }

    @Override
    public boolean getDoorCurrentState() {
        //TODO 获取到当前门的状态
        return false;
    }

    /**
     * 门磁的消息记录
     */
    @Override
    public void initMagData() {
        if (getView() != null) {
            getView().initRecycleView(getDbData());
        }
    }

    /**
     * 监听门磁发过来的一条新的消息
     */
    @Override
    public void getMesgFromMag() {
        // test
        MagBean bean = new MagBean();
        bean.magTime = System.currentTimeMillis();
        bean.visibleType = 0;
        bean.isFirst = true;
        getView().addOneMagMesg(bean);
/*
        // TODO SDK 调用
        return RxBus.getCacheInstance().toObservable(null)
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (getView() != null){
                            MagBean bean = new MagBean();
                            bean.magTime = SystemClock.currentThreadTimeMillis();
                            bean.visibleType = 0;
                            getView().addOneMagMesg(bean);
                        }
                    }
                });*/
    }

    /**
     * 获取到账号信息，用于命名本地数据库的表名
     */
    @Override
    public Subscription getAccount() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null) {
                            getDb(getUserInfo.jfgAccount.getAccount());
                            initMagData();
                        }
                    }
                });
    }

    /**
     * 获取到数据库的操作对象
     *
     * @param account
     */
    @Override
    public void getDb(String account) {
        dbManager = DataBaseUtil.getInstance(account).dbManager;
    }

    /**
     * 保存到数据库
     *
     * @param bean
     */
    @Override
    public void saveIntoDb(MagBean bean) {
        try {
            dbManager.save(bean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拿到数据库中的所有数据
     *
     * @return
     */
    @Override
    public List<MagBean> findFromAllDb() {
        List<MagBean> allData = new ArrayList<>();
        try {
            List<MagBean> tempList = dbManager.findAll(MagBean.class);
            if (tempList != null && tempList.size() > 0) {
                allData.clear();
                allData.addAll(tempList);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        Collections.sort(allData, new SortComparator());
        return allData;
    }

    /**
     * 获取本地的数据
     */
    private List<MagBean> getDbData() {
        List<MagBean> list = new ArrayList<>();
        list.addAll(findFromAllDb());
        return list;
    }

    /**
     * 将时间字符串转换成毫秒
     *
     * @param time
     * @return
     */
    public static Long timeStrToSecond(String time) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long second = format.parse(time).getTime();
            return second;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1l;
    }

    /**
     * 按时间排序
     */
    public class SortComparator implements Comparator<MagBean> {
        @Override
        public int compare(MagBean lhs, MagBean rhs) {
            return (int) (rhs.magTime - lhs.magTime);
        }
    }
}
