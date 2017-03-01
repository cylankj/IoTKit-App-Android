package com.cylan.jiafeigou.n.mvp.impl.home;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgCount;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineMessageBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.FastBlurUtil;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by hunt on 16-5-23.
 */
public class HomeMinePresenterImpl extends AbstractPresenter<HomeMineContract.View> implements HomeMineContract.Presenter {

    private Subscription onRefreshSubscription;
    private Subscription onBlurSubscribtion;
    private Subscription onLoadUserHeadSubscribtion;
    private CompositeSubscription subscription;
    private JFGAccount userInfo;                          //用户信息bean

    private boolean isOpenLogin = false;
    private boolean hasUnRead;
    private long requstId;
    private int unreadNum;

    public HomeMinePresenterImpl(HomeMineContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
/*        onRefreshSubscription = Observable.just(null)
                .subscribeOn(Schedulers.io())
                .delay(3000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (getView() != null)
                            getView().onPortraitUpdate(PreferencesUtils.getString(JConstant.USER_IMAGE_HEAD_URL, ""));
                    }
                });*/
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        subscription = new CompositeSubscription();
        subscription.add(checkIsOpenLoginCallBack());
        subscription.add(unReadMesgBack());
    }

    @Override
    public void stop() {
        super.stop();
        unSubscribe(subscription);
        unSubscribe(onRefreshSubscription);
        unSubscribe(onLoadUserHeadSubscribtion);
    }

    @Override
    public void portraitBlur(Bitmap bitmap) {
        onBlurSubscribtion = Observable.just(bitmap)
                .subscribeOn(Schedulers.computation())
                .map(new Func1<Bitmap, Bitmap>() {
                    @Override
                    public Bitmap call(Bitmap bm) {
                        if (getView() == null) {
                            return null;
                        }
                        Bitmap b = BitmapUtils.zoomBitmap(bm, 160, 160);
                        return FastBlurUtil.blur(b, 20, 2);
                    }
                })
                .map(new Func1<Bitmap, Drawable>() {
                    @Override
                    public Drawable call(Bitmap bitmap) {
                        if (getView() == null
                                || getView().getContext() == null
                                || getView().getContext().getResources() == null)
                            return null;
                        return new BitmapDrawable(getView().getContext().getResources(), bitmap);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Drawable>() {
                    @Override
                    public void call(Drawable drawable) {
                        if (getView() == null || drawable == null)
                            return;
                        getView().onBlur(drawable);
                    }
                });
    }

    @Override
    public String createRandomName() {
        String[] firtPart = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"
                , "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        Random random = new Random();
        int randNum1 = random.nextInt(10);
        int randNum2 = random.nextInt(10);

        if (randNum1 == randNum2) {
            randNum2 /= 2;
        }

        int randNum3 = random.nextInt(10);
        if ((randNum1 == randNum3) || (randNum2 == randNum3)) {
            randNum3 /= 2;
        }

        int a = random.nextInt(26);
        int b = random.nextInt(26);
        if (b == a) {
            b /= 2;
        }
        int c = random.nextInt(26);
        if ((a == c) || (b == c)) {
            c /= 2;
        }

        String result = firtPart[a] + firtPart[b] + firtPart[c]
                + randNum1 + randNum2 + randNum3;
        return result;
    }

    /**
     * 初始化界面的数据
     */
    @Override
    public Subscription initData(boolean isOpenLogin) {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null && getUserInfo instanceof RxEvent.GetUserInfo) {
                            userInfo = getUserInfo.jfgAccount;
                            if (isOpenLogin) {
                                getView().setUserImageHeadByUrl(PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON));
                                String userAlias = PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ALIAS);
                                getView().setAliasName(TextUtils.isEmpty(userAlias) ? createRandomName() : userAlias);
                                return;
                            }
                            if (getView() != null) {
                                getView().setUserImageHeadByUrl(userInfo.getPhotoUrl());
                                if (userInfo.getAlias() == null | TextUtils.isEmpty(userInfo.getAlias())) {
                                    boolean isEmail = JConstant.EMAIL_REG.matcher(userInfo.getAccount()).find();
                                    if (isEmail){
                                        String[] split = userInfo.getAccount().split("@");
                                        userInfo.setAlias(split[0]);
                                    }else {
                                        userInfo.setAlias(userInfo.getAccount());
                                    }
                                }
                                getView().setAliasName(userInfo.getAlias());
                            }
                        }
                    }
                });
    }

    /**
     * 获取到用户信息
     *
     * @return
     */
    @Override
    public JFGAccount getUserInfoBean() {
        return userInfo;
    }

    /**
     * 判断是否是三方的登录
     *
     * @return
     */
    @Override
    public boolean checkOpenLogIn() {
        return isOpenLogin;
    }

    /**
     * 是否三方登录的回调
     * @return
     */
    @Override
    public Subscription checkIsOpenLoginCallBack() {
        return RxBus.getCacheInstance().toObservableSticky(Boolean.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        isOpenLogin = aBoolean;
                        subscription.add(initData(aBoolean));
                    }
                });
    }

    /**
     * 获取到未读消息数
     */
    @Override
    public void getUnReadMesg() {
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    ArrayList<Long> idList = new ArrayList<>();
                    idList.add((long) 601);
                    idList.add((long) 701);
                    try {
                        requstId = JfgCmdInsurance.getCmd().robotCountData("", idList, 0);
                    } catch (JfgException e) {
                        AppLogger.e("" + e.getLocalizedMessage());
                    }
                });
    }

    @Override
    public Subscription unReadMesgBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.UnreadCount.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.UnreadCount unreadCount) ->{
                    if (unreadCount != null && unreadCount.seq == requstId){
                        for (JFGDPMsgCount jfgdpMsgCount:unreadCount.msgList){
                            unreadNum += jfgdpMsgCount.count;
                        }
                        getView().setMesgNumber(unreadNum);
                        if (unreadNum != 0){
                            hasUnRead = true;
                            markHasRead();
                        }else {
                            hasUnRead = false;
                        }
                    }
                });
    }

    @Override
    public boolean hasUnReadMesg() {
        return hasUnRead;
    }

    /**
     * 清空未读消息数
     */
    @Override
    public void markHasRead() {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe((Object o)->{
                    ArrayList<Long> idList = new ArrayList<>();
                    idList.add((long) 601);
                    idList.add((long) 701);
                    try {
                        JfgCmdInsurance.getCmd().robotCountDataClear(uuid, idList, 0);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                });
    }

}
