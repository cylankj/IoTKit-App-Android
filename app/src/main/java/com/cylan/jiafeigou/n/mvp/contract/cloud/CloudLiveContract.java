package com.cylan.jiafeigou.n.mvp.contract.cloud;

import android.content.Context;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;

import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseDbBean;
import java.io.Serializable;
import java.util.List;
import com.cylan.jiafeigou.support.db.DbManager;


/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public interface CloudLiveContract {

    interface View extends BaseView<Presenter> {
        void showVoiceTalkDialog(Context context);

        void refreshView(int leftVal, int rightVal);

        void initRecycleView();

        void refreshRecycleView(CloudLiveBaseBean bean);
    }

    interface Presenter extends BasePresenter {
        String startRecord();

        void startTalk();

        void stopRecord();

        boolean checkSDCard();

        void addMesgItem(CloudLiveBaseBean bean);

        CloudLiveBaseBean creatMesgBean();

        String getLeaveMesgLength();

        String parseTime(String times);

        void createDB();

        byte[] getSerializedObject(Serializable s);

        Object readSerializedObject(byte[] in);

        void saveIntoDb(CloudLiveBaseDbBean bean);          //保存到数据库

        List<CloudLiveBaseDbBean> findFromAllDb();          //查询数据库

    }
}
