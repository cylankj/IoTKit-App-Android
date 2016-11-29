package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.UserInfoBean;
import com.cylan.jiafeigou.support.galleryfinal.FunctionConfig;


import java.io.File;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public class MineInfoPresenterImpl extends AbstractPresenter<MineInfoContract.View> implements MineInfoContract.Presenter {

    public MineInfoPresenterImpl(MineInfoContract.View view, Context context) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void bindPersonEmail() {
        getView().jump2SetEmailFragment();
    }

    /**
     * 退出登录
     */
    @Override
    public void logOut() {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        JfgCmdInsurance.getCmd().logout();
                    }
                });
    }

    /**
     * 检查文件是否存在
     * @param dirPath
     * @return
     */
    @Override
    public String checkFileExit(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return "";
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dirPath;
    }

    /**
     * 检验是否存在相机
     * @return
     */
    @Override
    public boolean checkHasCamera() {
        PackageManager pm = getView().getContext().getPackageManager();
        boolean hasACamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD
                || Camera.getNumberOfCameras() > 0;
        return hasACamera;
    }

    /**
     * 检测相机是否可用
     * @return
     */
    @Override
    public boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }

        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
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
