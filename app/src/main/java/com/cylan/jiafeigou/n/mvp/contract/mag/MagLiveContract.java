package com.cylan.jiafeigou.n.mvp.contract.mag;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseDbBean;
import com.cylan.jiafeigou.n.mvp.model.MagBean;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/10/20
 * 描述：
 */
public interface MagLiveContract {

    interface View extends BaseView<Presenter>{

        /**
         * 初始化消息列表显示
         */
        void initRecycleView(List<MagBean> list);

        /**
         * 添加一条门磁消息
         */
        void addOneMagMesg(MagBean addBean);

        /**
         * 无消息记录
         */
        void showNoMesg();

        /**
         * 隐藏无消息
         */
        void hideNoMesg();

    }

    interface Presenter extends BasePresenter{
        /**
         *获取当前的门磁的状态
         * @return
         */
        boolean getDoorCurrentState();

        /**
         * 门磁的消息记录
         */
        void initMagData();

        /**
         * 监听门磁发送来的一条新消息
         */
        void getMesgFromMag();

        /**
         * 获取到账号信息，用于命名本地数据库的表名
         */
        Subscription getAccount();

        /**
         * 获取到数据库操作对象
         */
        void getDb(String account);

        /**
         * 保存到数据库
         * @param bean
         */
        void saveIntoDb(MagBean bean);

        /**
         * 拿到数据库中的所有数据
         * @return
         */
        List<MagBean> findFromAllDb();
    }

}
