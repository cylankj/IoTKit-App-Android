package com.cylan.jiafeigou.n.mvp.impl.home;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FastBlurUtil;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

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

    private Subscription onBlurSubscribtion;
    private CompositeSubscription subscription;
    private JFGAccount userInfo;                          //用户信息bean

    private boolean isOpenLogin = false;
    private boolean hasUnRead;

    public HomeMinePresenterImpl(HomeMineContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        subscription = new CompositeSubscription();
        subscription.add(getAccountBack());
        subscription.add(unReadMesgBack());
        subscription.add(loginInMe());
    }

    @Override
    public void stop() {
        super.stop();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
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
                        return new BitmapDrawable(ContextUtils.getContext().getResources(), bitmap);
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
                }, AppLogger::e);
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
     * 获取到未读消息数
     */
    @Override
    public void getUnReadMesg() {

        Observable.just("Now Get UnReadMsg")
                .observeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        ArrayList<JFGDPMsg> list = new ArrayList<JFGDPMsg>();
                        JFGDPMsg msg1 = new JFGDPMsg(1101L, System.currentTimeMillis());
                        JFGDPMsg msg2 = new JFGDPMsg(1103L, System.currentTimeMillis());
                        JFGDPMsg msg3 = new JFGDPMsg(1104L, System.currentTimeMillis());
                        list.add(msg1);
                        list.add(msg2);
                        list.add(msg3);
                        BaseApplication.getAppComponent().getCmd().robotGetData("", list, 10, false, 0);
                        AppLogger.d("getUnReadMesg:");
                    } catch (JfgException e) {
                        AppLogger.e("getUnReadMesg" + e.getLocalizedMessage());
                    }
                }, AppLogger::e);

    }

    public Subscription unReadMesgBack() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .onBackpressureBuffer()
                .filter(rsp -> rsp.map != null)
                .observeOn(Schedulers.io())
                .subscribe(rsp -> {
                    int count = -1;
                    if (rsp != null && rsp.map != null && rsp.map.size() != 0) {
                        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : rsp.map.entrySet()) {
                            try {
                                if (entry.getKey() == 1101 || entry.getKey() == 1103 || entry.getKey() == 1104) {
                                    count = 0;
                                    ArrayList<JFGDPMsg> value = entry.getValue();
                                    if (value.size() != 0) {
                                        JFGDPMsg jfgdpMsg = value.get(0);
                                        Integer unReadCount = DpUtils.unpackData(jfgdpMsg.packValue, Integer.class);
                                        if (unReadCount == null) unReadCount = 0;
                                        AppLogger.d("unReadCount:" + unReadCount);
                                        count += unReadCount;
                                    }
                                }
                            } catch (Exception e) {
                                AppLogger.e("getUnreadBack:" + e.getLocalizedMessage());
                            }
                        }
                        if (count >= 0) {
                            AppLogger.d("unrecount:" + count);
                            hasUnRead = count != 0;
                            int finalCount = count;
                            AndroidSchedulers.mainThread().createWorker().schedule(() -> {
                                if (getView() != null) getView().setMesgNumber(finalCount);
                            });
                        }
                    }
                }, AppLogger::e);
    }

    @Override
    public boolean hasUnReadMesg() {
        return hasUnRead;
    }


    private boolean isDefaultPhoto(String photoUrl) {
        return TextUtils.isEmpty(photoUrl) || photoUrl.contains("image/default.jpg");
    }

    @Override
    public Subscription getAccountBack() {
        return Observable.just(BaseApplication.getAppComponent().getSourceManager()).map(s -> {
            Account account = s.getAccount();
            JFGAccount jfgAccount = s.getJFGAccount();
            RxEvent.AccountArrived arrived = null;
            if (account != null && jfgAccount != null) {
                arrived = new RxEvent.AccountArrived(account);
                arrived.jfgAccount = jfgAccount;
            }
            return arrived;
        }).mergeWith(RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class))
                .filter(accountArrived -> accountArrived != null)
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getUserInfo -> {
                    if (getUserInfo != null) {
                        userInfo = getUserInfo.jfgAccount;
                        String photoUrl = userInfo.getPhotoUrl();
                        String alias = null;
                        RxEvent.ThirdLoginTab event = RxBus.getCacheInstance().getStickyEvent(RxEvent.ThirdLoginTab.class);
                        isOpenLogin = event != null && event.isThird;
                        if (isOpenLogin && isDefaultPhoto(photoUrl)) {

                            photoUrl = PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON);
                        }
                        if (isOpenLogin && TextUtils.isEmpty(userInfo.getAlias())) {
                            try {
                                BaseApplication.getAppComponent().getCmd().setAccount(userInfo);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                            userInfo.setAlias(PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ALIAS));
                        }
                        if (userInfo.getAlias() == null || TextUtils.isEmpty(userInfo.getAlias())) {
                            boolean isEmail = JConstant.EMAIL_REG.matcher(userInfo.getAccount()).find();
                            if (isEmail) {
                                String[] split = userInfo.getAccount().split("@");
                                userInfo.setAlias(split[0]);
                            } else {
                                userInfo.setAlias(userInfo.getAccount());
                            }
                        }
                        alias = userInfo.getAlias();
                        if (getView() != null && !TextUtils.isEmpty(photoUrl))
                            getView().setUserImageHeadByUrl(photoUrl);
                        if (getView() != null && !TextUtils.isEmpty(alias))
                            getView().setAliasName(alias);

                    }
                }, AppLogger::e);
    }

    @Override
    public void loginType() {
        Observable.just(null)
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
                    if (getView() != null) {
                        if (i == 3 || i == 4) {
                            getView().jump2SetPhoneFragment();
                        } else if (i == 6 || i == 7) {
                            getView().jump2BindMailFragment();
                        }
                    }
                }, AppLogger::e);
    }


    public Subscription loginInMe() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginMeTab.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginMeTab -> {
                    if (loginMeTab.b)
                        start();
                }, AppLogger::e);
    }

}
