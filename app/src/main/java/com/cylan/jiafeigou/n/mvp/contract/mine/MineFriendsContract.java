package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxEvent;

import java.util.ArrayList;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineFriendsContract {

    interface View extends BaseView<Presenter> {

        /**
         * desc:初始化好友列表
         */
        void initFriendRecyList(ArrayList<RelAndFriendBean> list);


        void initAddReqRecyList(ArrayList<MineAddReqBean> list);

        /**
         * desc：显示好友列表标题
         */
        void showFriendListTitle();

        /**
         * desc：没有好友时不显示列表标题
         */
        void hideFriendListTitle();

        /**
         * desc：有添加请求时显示添加请求标题
         */
        void showAddReqListTitle();

        /**
         * desc：无添加请求时不显示添加请求标题
         */
        void hideAddReqListTitle();

        void jump2FriendDetailFragment(int position, RelAndFriendBean account);

        void showLongClickDialog(int position, MineAddReqBean bean);

        void jump2AddReqDetailFragment(int position, MineAddReqBean bean);

        void showReqOutTimeDialog(MineAddReqBean item);

        /**
         * desc：显示空界面
         */
        void showNullView();

        /**
         * desc：删除添加请求条目
         * @param position
         * @param bean
         */
        void addReqDeleteItem(int position,MineAddReqBean bean);

        /**
         * desc：好友列表添加条目
         * @param position
         * @param bean
         */
        void friendlistAddItem(int position,RelAndFriendBean bean);

        /**
         * 显示加载进度
         */
        void showLoadingDialog();

        /**
         * 隐藏加载进度
         */
        void hideLoadingDialog();

    }

    interface Presenter extends BasePresenter {

        ArrayList<MineAddReqBean> initAddRequestData(RxEvent.GetAddReqList addReqList);

        ArrayList<RelAndFriendBean> initRelativatesAndFriendsData(RxEvent.GetFriendList friendList);

        boolean checkAddRequestOutTime(MineAddReqBean bean);        //检测添加请求是否超时
        /**
         * desc：初始化处理好友列表
         */
        Subscription initFriendRecyListData();

        /**
         * desc：初始化处理添加请求列表
         */
        Subscription initAddReqRecyListData();

        /**
         * desc：检查是否为空界面
         */
        void checkAllNull();

        /**
         * 启动获取到添加请求的SDK
         */
        Subscription getAddRequest();

        /**
         * 启动获取好友列表的SDK
         * @return
         */
        Subscription getFriendList();

        /**
         * 发送添加请求
         */
        void sendAddReq(String account);

        /**
         * 同意添加成功后调用SDK
         */
        void acceptAddSDK(String account);

    }

}
