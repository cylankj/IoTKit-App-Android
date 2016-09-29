package com.cylan.jiafeigou.n.mvp.contract.cloud;

import android.content.Context;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public interface CloudLiveContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter{
        void showVoiceTalkDialog(Context context);
        void startRecord();
    }
}
