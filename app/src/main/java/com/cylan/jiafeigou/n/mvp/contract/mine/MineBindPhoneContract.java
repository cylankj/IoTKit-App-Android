package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl personal
 * 创建时间：2016/9/1
 * 描述：
 */
public interface MineBindPhoneContract {

    interface View extends BaseView<Presenter> {
        /**
         * 修改标题
         */
        void initToolbarTitle(String title);

        /**
         * 获取到输入号码
         * @return
         */
        String getInputPhone();

        /**
         * 检测账号的是否已经注册的结果
         * @param checkAccountCallback
         */
        void handlerCheckPhoneResult(RxEvent.CheckAccountCallback checkAccountCallback);
    }

    interface Presenter extends BasePresenter {
        /**
         * 判断是绑定还是修改
         * @param userinfo
         */
        void isBindOrChange(JFGAccount userinfo);

        /**
         * 获取到验证码
         */
        void getCheckCode(String phone);

        /**
         * 检测账号是否已经注册
         */
        void checkPhoneIsBind(String phone);

        /**
         * 获取到检测账号的回调
         * @return
         */
        Subscription getCheckPhoneCallback();

        /**
         * 发送修改手机号请求
         */
        void sendChangePhoneReq(JFGAccount userinfo);
    }

}
