package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.Manifest;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsAddFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

import permissions.dispatcher.PermissionUtils;

/**
 * 作者：zsl
 * 创建时间：2016/11/26
 * 描述：
 */
public class MineFriendsAddFriendPresenterImp extends AbstractPresenter<MineFriendsAddFriendContract.View> implements MineFriendsAddFriendContract.Presenter {

    public MineFriendsAddFriendPresenterImp(MineFriendsAddFriendContract.View view) {
        super(view);
    }

    /**
     * 检测联系人权限
     *
     * @return
     */
    @Override
    public boolean checkContractPermission() {
        return PermissionUtils.hasSelfPermissions(getView().getContext(), Manifest.permission.READ_CONTACTS);
    }

    /**
     * 检测相机的权限
     *
     * @return
     */
    @Override
    public boolean checkCameraPermission() {
        return PermissionUtils.hasSelfPermissions(getView().getContext(), Manifest.permission.CAMERA);
    }

}
