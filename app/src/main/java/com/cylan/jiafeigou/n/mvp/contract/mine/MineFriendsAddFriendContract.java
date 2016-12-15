package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineFriendsAddFriendContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {
        /**
         * 检测联系人权限
         * @return
         */
        boolean checkContractPermission();

        /**
         * 检测相机的权限
         * @return
         */
        boolean checkCameraPermission();
    }

}
