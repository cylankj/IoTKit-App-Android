package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.view.adapter.item.ShareContactItem;

import java.util.List;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public interface MineShareToContactContract {

    interface View extends BaseView<Presenter> {

        void onInitContactFriends(List<ShareContactItem> friendItems);

        /**
         * 修改正在分享的进度提示
         */
        void changeShareingProHint(String finish);

        /**
         * 分享不同状态提示
         */
        void showPersonOverDialog(String content);

        /**
         * 调用系统发送短信的界面
         */
        void startSendMesgActivity(String account);

        /**
         * 分享结果处理
         *
         * @param requtestId
         * @param account
         */
        void handlerCheckRegister(int requtestId, String account);

        void showLoading(int resId, Object... args);

        void hideLoading();

    }

    interface Presenter extends BasePresenter {

        /**
         * 处理点击按钮
         */
        void handlerShareClick(String cid, String account);

        /**
         * 获取到已经分享给的亲友数
         *
         * @param cid
         * @return
         */
        Subscription getHasShareContract(String cid);

        /**
         * 获取以分享好友的回调
         *
         * @return
         */
        Subscription getHasShareContractCallBack();

        /**
         * 分享设备的回调
         *
         * @return
         */
        Subscription shareDeviceCallBack();

        /**
         * 检测发送短信权限
         *
         * @return
         */
        boolean checkSendSmsPermission();

        void checkAndInitContactList();

        void shareDeviceToContact(ShareContactItem shareContactItem);
    }

}
