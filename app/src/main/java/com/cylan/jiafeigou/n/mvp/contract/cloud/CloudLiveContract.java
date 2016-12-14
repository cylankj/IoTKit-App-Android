package com.cylan.jiafeigou.n.mvp.contract.cloud;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseDbBean;

import java.io.Serializable;
import java.util.List;

import rx.Subscription;


/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public interface CloudLiveContract {

    interface View extends BaseView<Presenter> {

        void showVoiceTalkDialog(boolean isOnLine);

        void refreshView(int leftVal, int rightVal);

        void refreshRecycleView(CloudLiveBaseBean bean);

        void hangUpRefreshView(String result);

        void handlerVideoTalkResult(boolean isOnline);

        void showReconnetProgress();

        void hideReconnetProgress();

        void scrollToLast();                               //滚动到最后一条

        /**
         * 初始化消息列表
         *
         * @param list
         */
        void initRecycleView(List<CloudLiveBaseBean> list);

        /**
         * 显示空视图
         */
        void showNoMesg();

        /**
         * 隐藏空视图
         */
        void hideNoMesg();
    }

    interface Presenter extends BasePresenter {

        String startRecord();

        void startTalk();

        void stopRecord();

        void playRecord(String fileName);

        void stopPlayRecord();

        boolean checkSDCard();

        void addMesgItem(CloudLiveBaseBean bean);

        CloudLiveBaseBean creatMesgBean();

        String getLeaveMesgLength();

        String parseTime(String times);

        void getDBManger(String dbName);

        byte[] getSerializedObject(Serializable s);

        Object readSerializedObject(byte[] in);

        void saveIntoDb(CloudLiveBaseDbBean bean);          //保存到数据库

        List<CloudLiveBaseDbBean> findFromAllDb();          //查询数据库

        Subscription refreshHangUpView();                   //更新挂断结果

        void handlerVideoTalk();                            //处理视频通话

        void handlerLeveaMesg();                            //处理语音留言

        /**
         * 获取到账号的信息用于创建数据库
         */
        Subscription getAccount();

        /**
         * 初始化消息列表的数据
         */
        void initData();
    }
}
