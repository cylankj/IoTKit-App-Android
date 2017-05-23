package com.cylan.jiafeigou.n.mvp.impl.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractFragmentPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FastBlurUtil;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by hunt on 16-5-23.
 */
public class HomeMinePresenterImpl extends AbstractFragmentPresenter<HomeMineContract.View> implements HomeMineContract.Presenter {

    private CompositeSubscription subscription;

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

//    private Subscription storageTypeUpdate() {
//        return RxBus.getCacheInstance().toObservableSticky(RxEvent.StorageTypeUpdate.class)
//                .observeOn(AndroidSchedulers.mainThread())
//                .filter(ret -> mView != null && mView.isAdded())
//                .filter(ret -> {
//                    JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
//                    return account != null && !TextUtils.isEmpty(account.getPhotoUrl());
//                })
//                .subscribe(ret -> mView.setUserImageHeadByUrl(BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getPhotoUrl()),
//                        AppLogger::e);
//    }

    @Override
    public void stop() {
        super.stop();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @Override
    public void portraitBlur(Bitmap bitmap) {
        //使用默认的图片
        Observable.just(bitmap)
                .subscribeOn(Schedulers.newThread())
                .map(b -> {
                    if (b == null) {
                        b = BitmapFactory.decodeResource(mView.getContext().getResources(), R.drawable.me_bg_top_image);
                    }
                    Bitmap result = BitmapUtils.zoomBitmap(b, 160, 160);
                    Bitmap blur = FastBlurUtil.blur(result, 20, 2);
                    return new BitmapDrawable(ContextUtils.getContext().getResources(), blur);
                })
                .filter(result -> check())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> getView().onBlur(result), AppLogger::e);
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
        return BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
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
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountArrived -> {
                    RxEvent.ThirdLoginTab event = RxBus.getCacheInstance().getStickyEvent(RxEvent.ThirdLoginTab.class);
                    isOpenLogin = event != null && event.isThird;
                    if (isOpenLogin) {
                        String photoUrl = isDefaultPhoto(accountArrived.account.getPhotoUrl()) ? PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON) : null;
                        if (!TextUtils.isEmpty(photoUrl)) {//设置第三方登录图像
                            Glide
                                    .with(getView().getContext()).load(photoUrl)
                                    .downloadOnly(new SimpleTarget<File>() {
                                        @Override
                                        public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                                            try {
                                                AppLogger.e("正在设置第三方登录图像" + resource.getAbsolutePath());
                                                BaseApplication.getAppComponent().getCmd().updateAccountPortrait(resource.getAbsolutePath());
                                            } catch (JfgException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        }
                    }

                    String alias = TextUtils.isEmpty(accountArrived.account.getAlias()) ? PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ALIAS) : accountArrived.account.getAlias();
                    if (TextUtils.isEmpty(alias)) {
                        boolean isEmail = JConstant.EMAIL_REG.matcher(accountArrived.jfgAccount.getAccount()).find();
                        if (isEmail) {
                            String[] split = accountArrived.jfgAccount.getAccount().split("@");
                            alias = split[0];
                        }
                    }
                    if (!TextUtils.isEmpty(alias) && TextUtils.isEmpty(accountArrived.account.getAlias())) {//设置第三方登录昵称
                        accountArrived.jfgAccount.setAlias(alias);
                        try {
                            AppLogger.e("正在设置第三方登录昵称" + alias);
                            BaseApplication.getAppComponent().getCmd().setAccount(accountArrived.jfgAccount);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }

                    if (getView() != null && !TextUtils.isEmpty(accountArrived.account.getPhotoUrl()))
                        getView().setUserImageHeadByUrl(accountArrived.account.getPhotoUrl());
                    if (getView() != null && !TextUtils.isEmpty(accountArrived.account.getAlias()))
                        getView().setAliasName(accountArrived.account.getAlias());
                }, AppLogger::e);
    }


    @Override
    public void loginType() {
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .flatMap(o -> {
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
