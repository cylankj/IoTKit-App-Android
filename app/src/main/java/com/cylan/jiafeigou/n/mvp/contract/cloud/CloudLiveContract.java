package com.cylan.jiafeigou.n.mvp.contract.cloud;

import android.widget.ImageView;

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

        /**
         * 播放录音动画
         *
         * @param view
         */
        void startPlayVoiceAnim(ImageView view);

        /**
         * 停止播放录音动画
         */
        void stopPlayVoiceAnim();
    }

    interface Presenter extends BasePresenter {

        String startRecord();

        void startTalkAnimation();

        void stopRecord();

        void playRecord(String fileName);

        void stopPlayRecord();

        boolean checkSDCard();

        void addMesgItem(CloudLiveBaseBean bean);

        CloudLiveBaseBean creatMesgBean();

        String getLeaveMesgLength();

        String parseTime(long times);

        void getDBManger(String dbName);

        byte[] getSerializedObject(Serializable s);

        Object readSerializedObject(byte[] in);

        void saveIntoDb(CloudLiveBaseDbBean bean);          //保存到数据库

        List<CloudLiveBaseDbBean> findAllFromDb();          //查询数据库

        void handlerVideoTalk();                            //处理视频通话

        void handlerLeveaMesg();                            //处理语音留言

        /**
         * 获取到账号的信息用于创建数据库
         */
        Subscription getAccount();

        /**
         * 初始化消息列表的数据
         */
        void initData(String userIcon);

        /**
         * 检测录音的权限
         */
        boolean checkRecordPermission();

        /**
         * 获取用户的头像
         *
         * @return
         */
        String getUserIcon();

        /**
         * 设备是否在线
         *
         * @return
         */
        boolean isDeviceOnline();
    }
}
