package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public interface MineFriendAddReqDetailContract {

    interface View extends BaseView<Presenter> {

        /**
         * 显示或隐藏添加请求信息
         * @param isFrome
         */
        void showOrHideReqMesg(boolean isFrome);

        /**
         * 添加请求过期弹出框
         */
        void showReqOutTimeDialog();

        /**
         * 添加请求已发送的提示
         */
        void showSendAddReqResult(boolean flag);

        /**
         * 添加成功提示
         */
        void showAddedReult(boolean flag);

        /**
         * 跳转到添加请求页
         */
        void jump2AddReqFragment();

        /**
         * 是否存在该账号的结果
         */
        void isHasAccountResult(RxEvent.GetAddReqList getAddReqList);
    }

    interface Presenter extends BasePresenter {
        /**
         * 添加为亲友
         */
        void handlerAddAsFriend(String addRequestItems);

        /**
         * 判断添加请求是否过期
         * @param addRequestItems
         * @return
         */
        void checkAddReqOutTime(MineAddReqBean addRequestItems);
        /**
         * 发送添加请求
         * @param addRequestItems
         */
        void sendAddReq(MineAddReqBean addRequestItems);


        /**
         * 获取到好友添加请求的列表，用户判断是否向我发送过添加请求
         * @return
         */
        Subscription getAddReqListDataCall();

        /**
         * 执行请求数据
          */
        Subscription excuteGetAddReqlistData();
    }

}
