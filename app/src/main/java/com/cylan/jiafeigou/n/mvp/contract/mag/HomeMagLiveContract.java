package com.cylan.jiafeigou.n.mvp.contract.mag;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanMagInfo;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/19
 * 描述：
 */
public interface HomeMagLiveContract {

    interface View extends BaseView<Presenter> {

        /**
         * 初始化设备名称的设置
         *
         * @param magInfoBean
         */
        void onMagInfoRsp(BeanMagInfo magInfoBean);

        boolean openDoorNotify();           //打开开和关通知

        void initMagDoorStateNotify();

        /**
         * 消息记录为空
         */
        void showNoMesg();

        /**
         * 显示清除进度
         */
        void showClearProgress();

        /**
         * 隐藏清除进度
         */
        void hideClearProgress();
    }

    interface Presenter extends BasePresenter {

        void clearOpenAndCloseRecord();             //清空开和关的记录

        /**
         * 取反操作
         *
         * @return
         */
        boolean getNegation();

        /**
         * 保存开关的状态
         *
         * @param isChick
         * @param key
         */
        void saveSwitchState(boolean isChick, String key);

        /**
         * 获取的开关的的状态
         *
         * @param key
         * @return
         */
        boolean getSwitchState(String key);

        /**
         * 获取到用户的信息拿到数据库对象
         */
        Subscription getAccount();

        /**
         * 拿到门磁信息
         *
         * @return
         */
        BeanMagInfo getMagInfoBean();

        /**
         * 获取到设备的名字
         *
         * @return
         */
        String getDeviceName();

    }

}
