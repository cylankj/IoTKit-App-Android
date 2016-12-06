package com.cylan.jiafeigou.n.mvp.contract.mag;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.BeanMagInfo;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/19
 * 描述：
 */
public interface HomeMagLiveContract {

    interface View extends BaseView<Presenter> {

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

        boolean getNegation();

        void saveSwitchState(boolean isChick, String key);

        boolean getSwitchState(String key);

        /**
         * 获取到用户的信息拿到数据库对象
         */
        Subscription getAccount();

        /**
         * 拿到门磁信息
         * @return
         */
        BeanMagInfo getMagInfoBean();

        /**
         * 获取到设备的名字
         * @return
         */
        String getDeviceName();

        /**
         * 保存设备信息
         * @param magInfoBean
         * @param id
         */
        void saveMagInfoBean(BeanMagInfo magInfoBean, int id);

    }

}
