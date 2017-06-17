package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.cache.db.module.FriendBean;
import com.cylan.jiafeigou.cache.db.module.FriendsReqBean;
import com.cylan.jiafeigou.n.mvp.BaseFragmentView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineFriendsContract {

    interface View extends BaseFragmentView<Presenter> {

        /**
         * desc:初始化好友列表
         */
        void initFriendList(ArrayList<FriendBean> list);


        void initAddReqReqList(ArrayList<FriendsReqBean> list);

        void showReqOutTimeDialog(FriendsReqBean item);

        /**
         * desc：长按删除添加请求条目
         */
        void deleteItemRsp(final String account, int code);

        void consentRsp(final String account, int code);

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);

    }

    interface Presenter extends BasePresenter {


        boolean checkAddRequestOutTime(FriendsReqBean bean);        //检测添加请求是否超时


        /**
         * 发送添加请求
         */
        void sendAddReq(String account);

        /**
         * 同意添加成功后调用SDK
         */
        void acceptAddSDK(String account);

        /**
         * 删除好友请求
         */
        void deleteAddReq(String account);

        void removeCache(String account);


    }

}
