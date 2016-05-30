package com.cylan.jiafeigou.n.view.splash;

import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.cylan.jiafeigou.utils.ParamStatic;
import com.cylan.support.DswLog;

import java.lang.ref.WeakReference;

import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.PermissionUtils;

import static android.support.v4.app.ActivityCompat.requestPermissions;

/**
 * Created by chen on 5/27/16.
 */


final class SplashPermisionDispatcher {
/**
 * 所有提示语句暂用camera的
 * */

    private static final String TAG = SplashPermisionDispatcher.class.getName();

    private SplashPermisionDispatcher() {
    }

    public static void showCameraWithCheck(WelcomePage target,String[] permisions,int requestCode) {
        if (PermissionUtils.hasSelfPermissions(target, permisions)) {
            //    target.showCamera();
        } else {
            if (PermissionUtils.shouldShowRequestPermissionRationale(target, permisions)) {
                target.showRationaleForCamera(new ShowCameraPermissionRequest(target, permisions, requestCode));
            } else {
                ActivityCompat.requestPermissions(target, permisions, requestCode);
            }
        }
    }

    public static void showWriteSdCardWithCheck(WelcomePage target) {
        if (PermissionUtils.hasSelfPermissions(target, ParamStatic.PERMISSION_SHOWMULTIS)) {
            //    target.showCamera();
        } else {
            if (PermissionUtils.shouldShowRequestPermissionRationale(target, ParamStatic.PERMISSION_SHOWMULTIS)) {
                target.showRationaleForCamera(new ShowCameraPermissionRequest(target, ParamStatic.PERMISSION_SHOWMULTIS, ParamStatic.REQUEST_SHOWMULTIS));
            } else {
                ActivityCompat.requestPermissions(target, ParamStatic.PERMISSION_SHOWMULTIS, ParamStatic.REQUEST_SHOWMULTIS);
            }
        }
    }

    /**
     * 一次可以检测某一个或者检测所有需要的权限
     */
    static void onRequestPermissionsResult(WelcomePage target, String[] permissions, int requestCode, int[] grantResults) {
        switch (requestCode) {
            //一次检测照相机权限
            case ParamStatic.REQUEST_SHOWCAMERA:
                if (PermissionUtils.getTargetSdkVersion(target) < 23 && !PermissionUtils.hasSelfPermissions(target, ParamStatic.PERMISSION_SHOWCAMERA)) {
                    target.onCameraDenied();
                    return;
                }
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    //     target.showCamera();
                } else {
                    if (!PermissionUtils.shouldShowRequestPermissionRationale(target, ParamStatic.PERMISSION_SHOWCAMERA)) {
                        target.onCameraNeverAskAgain();
                    } else {
                        target.onCameraDenied();
                    }
                }
                break;
            //一次检测所有需要的权限
            case ParamStatic.REQUEST_SHOWMULTIS:
                if (PermissionUtils.getTargetSdkVersion(target) < 23 && !PermissionUtils.hasSelfPermissions(target, ParamStatic.PERMISSION_SHOWCONTACTS)) {
                    //sdk < 23 的手机
                    return;
                }
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    //    you have all permision
                    target.showWriteSdCard();
                    return;
                }
                //没有某些权限不影响运行
                if (!PermissionUtils.hasSelfPermissions(target, ParamStatic.PERMISSION_SHOWCONTACTS)) {
                    //没有通讯录权限
                }

                if (!PermissionUtils.hasSelfPermissions(target, ParamStatic.PERMISSION_SHOWCAMERA)) {
                    //没有相机,录像机权限
                }

                //没有某些权限,影响app运行,需要做处理
                if (!PermissionUtils.hasSelfPermissions(target, ParamStatic.PERMISSION_SHOWWRITE_STORAGE)) {
                    //没有读写权限
                    target.onWriteSdCardDenied();
                }else {
                    target.showWriteSdCard();
                }
                break;
            default:
                break;
        }
    }

    private static class ShowCameraPermissionRequest implements PermissionRequest {
        private final WeakReference<WelcomePage> weakTarget;
        private String[] permision_Show;
        private int requstCode;

        private ShowCameraPermissionRequest(WelcomePage target, String[] permision, int request) {
            this.weakTarget = new WeakReference<>(target);
            this.permision_Show = permision;
            this.requstCode = request;
        }

        @Override
        public void proceed() {
            WelcomePage target = weakTarget.get();
            if (target == null) return;
            requestPermissions(target, permision_Show, requstCode);
        }

        @Override
        public void cancel() {
            WelcomePage target = weakTarget.get();
            if (target == null) return;
            target.onCameraDenied();
        }

    }
}
