package com.cylan.jiafeigou.n.mvp.impl.home;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsgCount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.FastBlurUtil;
import com.cylan.jiafeigou.utils.PreferencesUtils;

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

    private Subscription onBlurSubscribtion;
    private CompositeSubscription subscription;
    private JFGAccount userInfo;                          //用户信息bean

    private boolean isOpenLogin = false;
    private boolean hasUnRead;
    private long requstId;
    private int unreadNum;

    public HomeMinePresenterImpl(HomeMineContract.View view) {
        super(view);
        view.setPresenter(this);
        loginInMe();
    }

    @Override
    public void start() {
        super.start();
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
        unSubscribe(loginInMe());
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
     *
     * @return
     */
    @Override
    public Subscription checkIsOpenLoginCallBack() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.ThirdLoginTab.class)
                .flatMap(new Func1<RxEvent.ThirdLoginTab, Observable<JFGAccount>>() {
                    @Override
                    public Observable<JFGAccount> call(RxEvent.ThirdLoginTab thirdLoginTab) {
                        if (thirdLoginTab.isThird) {
                            JFGAccount account = new JFGAccount();
                            account.setEnablePush(true);
                            return Observable.just(account);
                        } else {
                            return Observable.just(DataSourceManager.getInstance().getJFGAccount());
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGAccount>() {
                    @Override
                    public void call(JFGAccount account) {
                        userInfo = account;
                        if (account != null && getView() != null) {
                            if (TextUtils.isEmpty(account.getAccount()) && account.isEnablePush()) {
                                isOpenLogin = true;
                                getView().setUserImageHeadByUrl(PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON));
                                String userAlias = PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ALIAS);
                                getView().setAliasName(TextUtils.isEmpty(userAlias) ? createRandomName() : userAlias);
                            } else {
                                isOpenLogin = false;
                                getView().setUserImageHeadByUrl(account.getPhotoUrl());
                                if (account.getAlias() == null | TextUtils.isEmpty(account.getAlias())) {
                                    boolean isEmail = JConstant.EMAIL_REG.matcher(account.getAccount()).find();
                                    if (isEmail) {
                                        String[] split = account.getAccount().split("@");
                                        account.setAlias(split[0]);
                                    } else {
                                        account.setAlias(account.getAccount());
                                    }
                                }
                                getView().setAliasName(account.getAlias());
                            }
                        }
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
                    try {
                        requstId = JfgCmdInsurance.getCmd().robotCountData("", new long[]{601L, 701L}, 0);
                    } catch (JfgException e) {
                        AppLogger.e("" + e.getLocalizedMessage());
                    }
                });
    }

    @Override
    public Subscription unReadMesgBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.UnreadCount.class)
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.UnreadCount unreadCount) -> {
                    if (unreadCount != null && unreadCount.seq == requstId) {
                        for (JFGDPMsgCount jfgdpMsgCount : unreadCount.msgList) {
                            unreadNum += jfgdpMsgCount.count;
                        }
                        getView().setMesgNumber(unreadNum);
                        if (unreadNum != 0) {
                            hasUnRead = true;
                            markHasRead();
                        } else {
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
                .subscribe((Object o) -> {
                    try {
                        JfgCmdInsurance.getCmd().robotCountDataClear(uuid, new long[]{601L, 701L}, 0);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                });
    }

    public Subscription loginInMe() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginMeTab.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginMeTab -> {
                    start();
                });
    }

}
