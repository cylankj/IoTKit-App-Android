package com.cylan.jiafeigou.n.mvp.contract.home;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.UiThread;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.MineMessageBean;
import com.cylan.jiafeigou.n.mvp.model.UserInfoBean;

import java.util.ArrayList;

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
    }

    interface Presenter extends BasePresenter {


        /**
         * 设置头像的背景
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

        /**
         * Dp获取消息记录数据
         */
        Subscription getMesgDpData();

        /**
         * Dp获取消息记录数据回调
         * @return
         */
        Subscription getMesgDpDataCallBack();

        /**
         * 获取的消息的所有的数据
         * @return
         */
        ArrayList<MineMessageBean> getMesgAllData();

        /**
         * 获取是否三方登录的回调
         * @return
         */
        Subscription checkIsOpenLoginCallBack();
    }
}
