package com.cylan.jiafeigou.n.mvp.contract.home;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.UiThread;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.mvp.BaseFragmentView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * Created by hunt on 16-5-23.
 */
public interface HomeMineContract {

    interface View extends BaseFragmentView<Presenter> {
        /**
         * @param url: 返回url,可以使用`glide`或者`uil`直接加载
         */
        @UiThread
        void onPortraitUpdate(String url);

        void onBlur(Drawable drawable);


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
        void setUserImageHeadByUrl(String url);

        /**
         * 设置新消息的数量
         */
        void setMesgNumber(int number);

        void jump2SetPhoneFragment();

        void jump2BindMailFragment();
    }

    interface Presenter extends BasePresenter {


        /**
         * 设置头像的背景
         *
         * @param bitmap
         */
        void portraitBlur(Bitmap bitmap);

        /**
         * 产生随机的昵称
         *
         * @return
         */
        String createRandomName();

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

        /**
         * 获取到未读的消息数
         */
        void getUnReadMesg();

//        /**
//         * 未读消息的回调
//         *
//         * @return
//         */
//        Subscription unReadMesgBack();

        /**
         * 是否有未读消息
         *
         * @return
         */
        boolean hasUnReadMesg();

        Subscription getAccountBack();

        void loginType();

        void updateAccount();

    }
}
