package com.cylan.jiafeigou.n.mvp.contract.home;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.UiThread;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * Created by hunt on 16-5-23.
 */
public interface HomeMineContract {

    interface View extends BaseView<Presenter> {
        /**
         * @param url: 返回url,可以使用`glide`或者`uil`直接加载
         */
        @UiThread
        void onPortraitUpdate(String url);

        void onBlur(Drawable drawable);

        void setUserImageHead(Drawable drawable);

        /**
         * 设置昵称
         *
         * @param name
         */
        void setAliasName(String name);

        /**
         * 通过URL设置头像
         *
         * @param url
         */
        void setUserImageHead(String url);

        /**
         * 设置新消息的数量
         */
        void setMesgNumber(int number);
    }

    interface Presenter extends BasePresenter {

        void requestLatestPortrait();

        /**
         * 设置头像的背景
         *
         * @param id
         */
        void portraitBlur(@DrawableRes int id);

        /**
         * 设置头像
         *
         * @param url
         */
        void portraitUpdateByUrl(String url);

        /**
         * 产生随机的昵称
         *
         * @return
         */
        String createRandomName();

        /**
         * 初始化界面的数据
         */
        Subscription initData();

        /**
         * 获取到登录用户的bean
         *
         * @return
         */
        JFGAccount getUserInfoBean();

        /**
         * 判断是否是三方登录
         *
         * @return
         */
        boolean checkOpenLogIn();

    }
}
