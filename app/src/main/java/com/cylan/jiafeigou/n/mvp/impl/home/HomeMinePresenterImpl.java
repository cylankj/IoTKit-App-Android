package com.cylan.jiafeigou.n.mvp.impl.home;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
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

import java.util.ArrayList;
import java.util.HashMap;
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
    private long requstId;

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
        subscription.add(checkIsOpenLoginCallBack());
        subscription.add(getAccountBack());
        subscription.add(unReadMesgBack());
        subscription.add(loginInMe());
        getUnReadMesg();
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
                        AppLogger.d("mine_account:" + account);
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
                        ArrayList<JFGDPMsg> list = new ArrayList<JFGDPMsg>();
                        JFGDPMsg msg1 = new JFGDPMsg(1101L, System.currentTimeMillis());
                        JFGDPMsg msg2 = new JFGDPMsg(1103L, System.currentTimeMillis());
                        JFGDPMsg msg3 = new JFGDPMsg(1104L, System.currentTimeMillis());
                        list.add(msg1);
                        list.add(msg2);
                        list.add(msg3);
                        requstId = JfgCmdInsurance.getCmd().robotGetData("", list, 10, false, 0);
                        AppLogger.d("getUnReadMesg:" + requstId);

                        //新接口
//                        HashMap<String,long[]> map = new HashMap<>();
//                        long[] ip = new long[]{1101L,1103L,1104L};
//                        map.put("",ip);
//                        long reqNew = JfgCmdInsurance.getCmd().robotCountMultiData(map, false, 0);
//                        AppLogger.d("newCount:"+reqNew);
                    } catch (JfgException e) {
                        AppLogger.e("getUnReadMesg" + e.getLocalizedMessage());
                    }
                });
    }

    public Subscription unReadMesgBack() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .onBackpressureBuffer()
                .filter(rsp -> rsp.map != null)
                .flatMap(new Func1<RobotoGetDataRsp, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(RobotoGetDataRsp rsp) {
                        int count = 0;
                        if (rsp != null && requstId == rsp.seq && rsp.map != null && rsp.map.size() != 0){
                            for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : rsp.map.entrySet()) {
                                try {
                                    if (entry.getKey() == 1101 || entry.getKey() == 1103 || entry.getKey() == 1104) {
                                        ArrayList<JFGDPMsg> value = entry.getValue();
                                        if (value.size() != 0) {
                                            JFGDPMsg jfgdpMsg = value.get(0);
                                            DpMsgDefine.DPUnreadCount unReadCount = DpUtils.unpackData(jfgdpMsg.packValue, DpMsgDefine.DPUnreadCount.class);
                                            if (unReadCount != null)
                                                count += unReadCount.count;
                                        }
                                    }
                                } catch (Exception e) {
                                    AppLogger.e("getUnreadBack:" + e.getLocalizedMessage());
                                    return Observable.just(count);
                                }
                            }
                        }
                        return Observable.just(count);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    AppLogger.d("unrecount:" + integer);
                    if (getView() != null) getView().setMesgNumber(integer);
                    hasUnRead = integer != 0;
                });
    }

    @Override
    public boolean hasUnReadMesg() {
        return hasUnRead;
    }

    @Override
    public Subscription getAccountBack() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getUserInfo -> {
                    if (getUserInfo != null)
                        userInfo = getUserInfo.jfgAccount;
                });
    }


    public Subscription loginInMe() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginMeTab.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginMeTab -> {
                    if (loginMeTab.b)
                        start();
                });
    }

}
