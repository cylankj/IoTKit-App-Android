package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.UserInfoBean;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public interface MineInfoContract {

    interface View extends BaseView<Presenter> {

        void initPersonalInformation(JFGAccount bean);        //初始化显示个人信息

        void jump2SetEmailFragment();

        void showChooseImageDialog();

        void showLogOutDialog();                                //退出登录提示框

    }


    interface Presenter extends BasePresenter {

        void bindPersonEmail();                 //绑定邮箱

        /**
         * 退出登录
         */
        void logOut();

        /**
         * 检查文件是否存在
         * @param path
         * @return
         */
        String checkFileExit(String path);

        boolean checkHasCamera();

        boolean cameraIsCanUse();
    }

}
