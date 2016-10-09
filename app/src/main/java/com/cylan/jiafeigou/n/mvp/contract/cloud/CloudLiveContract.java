package com.cylan.jiafeigou.n.mvp.contract.cloud;

import android.content.Context;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveMesgBean;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public interface CloudLiveContract {

    interface View extends BaseView<Presenter> {
        void showVoiceTalkDialog(Context context);
        void refreshView(int leftVal,int rightVal);
        void initRecycleView();
        void refreshRecycleView(CloudLiveBaseBean bean);
    }

    interface Presenter extends BasePresenter{
        void startRecord();
        void startTalk();
        void stopRecord();
        boolean checkSDCard();
        List<CloudLiveMesgBean> getMesgData();
        void addMesgItem(CloudLiveBaseBean bean);
        CloudLiveBaseBean creatMesgBean();
    }
}
