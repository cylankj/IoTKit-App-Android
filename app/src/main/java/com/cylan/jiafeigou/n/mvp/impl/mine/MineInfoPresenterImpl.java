package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.File;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.cylan.jiafeigou.misc.JConstant.KEY_ACCOUNT;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public class MineInfoPresenterImpl extends AbstractPresenter<MineInfoContract.View> implements MineInfoContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private boolean isOpenLogin;

    public MineInfoPresenterImpl(MineInfoContract.View view, Context context) {
        super(view);
        view.setPresenter(this);
    }

    /**
     * 退出登录
     */
    @Override
    public void logOut(String account) {
        DataSourceManager.getInstance().logout()
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    JfgCmdInsurance.getCmd().logout();
                    RxBus.getCacheInstance().removeAllStickyEvents();
                    AutoSignIn.getInstance().autoSave(account, 1, "")
                            .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                            .subscribe(ret -> {
                            }, throwable -> AppLogger.e("err:" + throwable));
                    //emit failed event.
                    PreferencesUtils.putString(KEY_ACCOUNT, "");
                    PreferencesUtils.putInt(JConstant.IS_lOGINED, 0);
                    RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.StartLoginPage));
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("logOut" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 检查文件是否存在
     *
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
     *
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
     *
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
     *
     * @return
     */
    @Override
    public boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getView().getContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 检测存储权限
     *
     * @return
     */
    @Override
    public boolean checkExternalStorePermission() {
        if (ContextCompat.checkSelfPermission(getView().getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 获取到用户的信息
     */
    @Override
    public Subscription getAccount() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null) {
                            if (getView() != null)
                                getView().initPersonalInformation(getUserInfo.jfgAccount);
                        }
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }


    @Override
    public void start() {
        super.start();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(isOpenLoginBack());
            compositeSubscription.add(getAccount());
        }
    }

    /**
     * 判断是否是三方登录
     *
     * @return
     */
    @Override
    public boolean checkOpenLogin() {
        return isOpenLogin;
    }

    /**
     * 三方登录的回调
     *
     * @return
     */
    @Override
    public Subscription isOpenLoginBack() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.ThirdLoginTab.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(thirdLoginTab -> {
                    isOpenLogin = thirdLoginTab.isThird;
                    getView().showSetPwd(thirdLoginTab.isThird);
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    @Override
    public Subscription loginType(String account) {
        return Observable.just(null)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<Object, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Object o) {
                        try {
                            String aesAccount = PreferencesUtils.getString(JConstant.AUTO_SIGNIN_KEY);
                            if (TextUtils.isEmpty(aesAccount)) {
                                AppLogger.d("reShowAccount:aes account is null");
                                return Observable.just(null);
                            }
                            String decryption = AESUtil.decrypt(aesAccount);
                            AutoSignIn.SignType signType = new Gson().fromJson(decryption, AutoSignIn.SignType.class);
                            return Observable.just(signType.type);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Observable.just(1);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i -> {
                    if (getView() != null) getView().setAccount(account, i);
                });
    }

    @Override
    public void stop() {
        super.stop();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }
}
