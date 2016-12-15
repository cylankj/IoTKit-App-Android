package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsAddFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/11/26
 * 描述：
 */
public class MineFriendsAddFriendPresenterImp extends AbstractPresenter<MineFriendsAddFriendContract.View> implements MineFriendsAddFriendContract.Presenter{

    public MineFriendsAddFriendPresenterImp(MineFriendsAddFriendContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    /**
     * 检测联系人权限
     * @return
     */
    @Override
    public boolean checkContractPermission() {
        if (ContextCompat.checkSelfPermission(getView().getContext(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }else{
            return true;
        }
    }

    /**
     * 检测相机的权限
     * @return
     */
    @Override
    public boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getView().getContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
