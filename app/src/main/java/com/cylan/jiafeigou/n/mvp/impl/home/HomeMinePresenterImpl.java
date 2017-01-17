package com.cylan.jiafeigou.n.mvp.impl.home;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineMessageBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.utils.BitmapUtil;
import com.cylan.utils.FastBlurUtil;

import java.io.IOException;
import java.util.ArrayList;
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

    public static int PHONE_LOGIN = 1;                     //手机登录
    public static int EMAIL_LOGIN = 2;                     //邮箱登录
    public static int THIRD_PART_LOGIN = 3;                //第三方登录

    private Subscription onRefreshSubscription;
    private Subscription onBlurSubscribtion;
    private Subscription onLoadUserHeadSubscribtion;
    private CompositeSubscription subscription;
    private JFGAccount userInfo;                          //用户信息bean
    private ArrayList<MineMessageBean> results = new ArrayList<MineMessageBean>();
    private boolean isOpenLogin = false;

    public HomeMinePresenterImpl(HomeMineContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
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
        subscription.add(getMesgDpData());
        subscription.add(getMesgDpDataCallBack());
    }

    @Override
    public void stop() {
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
                        Bitmap b = BitmapUtil.zoomBitmap(bm, 160, 160);
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
                                    userInfo.setAlias(createRandomName());
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
     * Dp获取到消息记录
     */
    @Override
    public Subscription getMesgDpData() {
        return rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        try {
                            ArrayList<JFGDPMsg> dp = new ArrayList<>();
                            JFGDPMsg msg = new JFGDPMsg(601, 0);
                            dp.add(msg);
                            long seq = JfgCmdInsurance.getCmd().getInstance().robotGetData("", dp, 0, false, 0);
                            AppLogger.d("getMesgDpData" + seq);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getMesgDpData" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * Dp获取消息记录的回调
     *
     * @return
     */
    @Override
    public Subscription getMesgDpDataCallBack() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .subscribeOn(Schedulers.io())
                .map(new Func1<RobotoGetDataRsp, ArrayList<MineMessageBean>>() {
                    @Override
                    public ArrayList<MineMessageBean> call(RobotoGetDataRsp robotoGetDataRsp) {
                        if (robotoGetDataRsp != null && robotoGetDataRsp instanceof RobotoGetDataRsp) {
                            results.clear();
                            ArrayList<JFGDPMsg> jfgdpMsgs = robotoGetDataRsp.map.get(601);
                            results.addAll(convertData(jfgdpMsgs));
                        }
                        return results;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<MineMessageBean>>() {
                    @Override
                    public void call(ArrayList<MineMessageBean> list) {
                        if (list.size() != 0) {
                            getView().setMesgNumber(list.size());
                        }
                    }
                });

    }

    /**
     * 拿到消息的所有的数据
     *
     * @return
     */
    @Override
    public ArrayList<MineMessageBean> getMesgAllData() {
        return results;
    }

    /**
     * 是否三方登录的回调
     *
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
     * 解析转换数据
     *
     * @param jfgdpMsgs
     */
    private ArrayList<MineMessageBean> convertData(ArrayList<JFGDPMsg> jfgdpMsgs) {
        MineMessageBean bean;
        ArrayList<MineMessageBean> results = new ArrayList<MineMessageBean>();
        if (jfgdpMsgs != null) {
            for (JFGDPMsg jfgdpMsg : jfgdpMsgs) {
                try {
                    bean = DpUtils.unpackData(jfgdpMsg.packValue, MineMessageBean.class);
                    results.add(bean);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return results;
    }

}
