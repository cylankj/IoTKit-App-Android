package com.cylan.jiafeigou.n.mvp.impl.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.utils.BitmapUtil;
import com.cylan.utils.FastBlurUtil;

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
        if(subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }
            subscription = new CompositeSubscription();
            subscription.add(initData());
    }

    @Override
    public void stop() {
        unSubscribe(subscription);
        unSubscribe(onRefreshSubscription);
        unSubscribe(onLoadUserHeadSubscribtion);
    }

    @Override
    public void requestLatestPortrait() {

    }

    @Override
    public void portraitBlur(@DrawableRes int id) {
        onBlurSubscribtion = Observable.just(id)
                .subscribeOn(Schedulers.computation())
                .map(new Func1<Integer, Bitmap>() {
                    @Override
                    public Bitmap call(Integer integer) {
                        if (getView() == null) {
                            return null;
                        }
                        Bitmap bm = BitmapFactory.decodeResource(getView().getContext().getResources(),
                                integer);
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
    public void portraitUpdateByUrl(String url) {
        onLoadUserHeadSubscribtion = Observable.just(url)
                .subscribeOn(Schedulers.computation())
                .map(new Func1<String, Bitmap>() {
                    @Override
                    public Bitmap call(String url) {
                        if (getView() == null) {
                            return null;
                        }
                        final Bitmap[] bit = new Bitmap[1];
                        Glide.with(getView().getContext())
                                .load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                bit[0] = resource;
                                bit[0] = BitmapUtil.zoomBitmap(bit[0], 160, 160);
                            }
                        });
                        return FastBlurUtil.blur(bit[0], 20, 2);
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
                        getView().setUserImageHead(drawable);
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
    public Subscription initData() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null && getUserInfo instanceof RxEvent.GetUserInfo){
                            userInfo = getUserInfo.jfgAccount;
                            if (getView() != null){
                                getView().setUserImageHead(userInfo.getPhotoUrl());
                                if (userInfo.getAlias() == null | "".equals(userInfo.getAlias())){
                                    userInfo.setAlias(createRandomName());
                                }
                                getView().setAliasName(userInfo.getAlias());
                                getView().setMesgNumber(99);
                            }
                        }
                    }
                });
    }

    /**
     * 获取到用户信息
     * @return
     */
    @Override
    public JFGAccount getUserInfoBean() {
        return userInfo;
    }

    /**
     * 判断是否是三方的登录
     * @return
     */
    @Override
    public boolean checkOpenLogIn() {
        // TODO
        return false;
    }

}
