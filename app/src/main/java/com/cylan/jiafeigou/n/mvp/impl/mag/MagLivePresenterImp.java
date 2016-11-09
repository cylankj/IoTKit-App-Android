package com.cylan.jiafeigou.n.mvp.impl.mag;

import android.os.SystemClock;

import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mag.MagLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MagBean;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.utils.RandomUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import rx.Subscription;
import rx.functions.Action1;

/**
 * 作者：zsl
 * 创建时间：2016/10/20
 * 描述：
 */
public class MagLivePresenterImp extends AbstractPresenter<MagLiveContract.View> implements MagLiveContract.Presenter {

    public MagLivePresenterImp(MagLiveContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        initMagData();
    }

    @Override
    public void stop() {

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
        if (getView() != null){
            //TODO 从本地数据库 获取消息记录 或者从服务器拉取
            getView().initRecycleView(TestData());
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
        return RxBus.getDefault().toObservable(null)
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
        return RxBus.getDefault().toObservable(RxEvent.GetUserInfo.class)
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {

                    }
                });
    }


    /**
     * 测试的数据
     */
    private ArrayList<MagBean> TestData() {
        ArrayList<MagBean> list = new ArrayList<>();
        //TODO 获取本地数据
        MagBean bean = new MagBean();
        bean.magTime = timeStrToSecond("2016-11-8 20:32:12");
        bean.isFirst = true;
        bean.isOpen = true;
        list.add(bean);
        return list;
    }

    /**
     * 将时间字符串转换成毫秒
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



    private ArrayList<MagBean> initData() {
        ArrayList<MagBean> magList = new ArrayList<>();

        for (int i = 0; i <= 10; i++) {
            MagBean magBean = new MagBean();
            magBean.setIsOpen(i % 2 == 0 ? false : true);
            if (i == 0) {
                magBean.setVisibleType(0);
            } else if (i == 5) {
                magBean.setVisibleType(1);
            } else if (i == 6) {
                magBean.setVisibleType(0);
            } else {
                magBean.setVisibleType(0);
            }
            magBean.setMagTime(System.currentTimeMillis() - RandomUtils.getRandom(24 * 3600));
            magList.add(magBean);
        }
        return magList;
    }

}
