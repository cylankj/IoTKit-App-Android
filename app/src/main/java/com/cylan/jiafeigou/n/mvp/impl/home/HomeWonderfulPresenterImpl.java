package com.cylan.jiafeigou.n.mvp.impl.home;


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.wechat.WechatShare;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract.View.VIEW_TYPE_EMPTY;
import static com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract.View.VIEW_TYPE_GUIDE;
import static com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract.View.VIEW_TYPE_HIDE;


/**
 * Created by hunt on 16-5-23.
 */
public class HomeWonderfulPresenterImpl extends BasePresenter<HomeWonderfulContract.View>
        implements HomeWonderfulContract.Presenter {
    private static final int LOAD_PAGE_COUNT = 20;
    private SoftReference<List<DpMsgDefine.DPWonderItem>> mWeakMediaLists;
    private WechatShare wechatShare;

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getTimeTickEventSub(), getPageScrolledSub());
    }

    @Override
    public void onSetContentView() {
        if (showGuidePage()) {
            mView.chooseEmptyView(VIEW_TYPE_GUIDE);
        } else {
            startLoadMore();
        }
    }

    private boolean showGuidePage() {
        return PreferencesUtils.getBoolean(JConstant.KEY_WONDERFUL_GUIDE, true);
    }


    private Subscription getTimeTickEventSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.TimeTickEvent.class)
                .subscribeOn(Schedulers.newThread())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(timeTickEvent -> {
                    mView.onTimeTick(JFGRules.getTimeRule());

                });
    }

    private Subscription getPageScrolledSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.PageScrolled.class)
                .subscribeOn(Schedulers.io())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(timeTickEvent -> {
                    mView.onPageScrolled();
                });
    }

    /**
     * 备份所有需要显示的数据，再次取的时候，首先从这个reference中取，如果空再查询数据库。
     *
     * @param list
     */
    private synchronized void updateCache(List<DpMsgDefine.DPWonderItem> list) {
        if (list == null || list.size() == 0)
            return;
        if (mWeakMediaLists == null) {
            mWeakMediaLists = new SoftReference<>(list);
            return;
        }
        if (mWeakMediaLists.get() == null) {
            mWeakMediaLists = new SoftReference<>(list);
            return;
        }
        if (mWeakMediaLists != null && mWeakMediaLists.get() != null) {
            List<DpMsgDefine.DPWonderItem> rawList = mWeakMediaLists.get();
            rawList.addAll(list);
            //remove the same one by time
            rawList = new ArrayList<>(new HashSet<>(rawList));
            Collections.sort(rawList);
            //retain them again
            mWeakMediaLists = new SoftReference<>(rawList);
        }
    }


    /**
     * 组装timeLine的数据
     *
     * @param list
     * @return
     */
    private List<Long> assembleTimeLineData(List<DpMsgDefine.DPWonderItem> list) {
        ArrayList<Long> result = new ArrayList<>(1024);
        for (DpMsgDefine.DPWonderItem bean : list) {
            result.add((long) bean.time * 1000);
        }
        return result;
    }


    private String getDate(final long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd", Locale.getDefault());
        return dateFormat.format(new Date(time));
    }

    @Override
    public void startRefresh() {
        Observable.create(subscriber -> {
            load(true);
            subscriber.onNext(null);
            subscriber.onCompleted();
        }).delay(5, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(delay -> {
                    mView.onMediaListRsp(null);
                });
    }

    private void load(boolean refresh) {
        long version = 0;
        boolean asc = false;
        if (mWeakMediaLists != null && mWeakMediaLists.get() != null && mWeakMediaLists.get().size() > 0) {
            if (refresh) {
                version = mWeakMediaLists.get().get(0).version * 1000L;
                asc = true;
            } else {
                version = mWeakMediaLists.get().get(mWeakMediaLists.get().size() - 1).version * 1000L;
                asc = false;
            }
        }
        ArrayList<JFGDPMsg> params = new ArrayList<>();
        JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, version);
        params.add(msg);
        try {
            long seq = JfgCmdInsurance.getCmd().robotGetData("", params, LOAD_PAGE_COUNT, asc, 0);
            mRequestSeqs.add(seq);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startLoadMore() {
        Observable.create(subscriber -> {
            load(false);
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void deleteTimeline(long time) {

    }

    private void initWechatInstance() {
        if (wechatShare == null || !wechatShare.isRegister()) {
            wechatShare = new WechatShare(mView.getActivityContext());
        }
    }

    @Override
    public boolean checkWechat() {
        try {
            return mView
                    .getAppContext()
                    .getPackageManager()
                    .getPackageInfo("com.tencent.mm", PackageManager.GET_SIGNATURES) != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void unregisterWechat() {
        if (wechatShare != null) {
            wechatShare.unregister();
            wechatShare = null;
        }
    }

    @Override
    public void shareToWechat(DpMsgDefine.DPWonderItem mediaBean, final int type) {
        if (mediaBean == null) {
            AppLogger.i("mediaBean is null");
            return;
        }
        initWechatInstance();
        //find bitmap from glide
        final WechatShare.ShareContent shareContent = new WechatShare.ShareContentImpl();
        //朋友圈，微信
        shareContent.shareScene = type;
        Glide.with(ContextUtils.getContext())
                .load(mediaBean.fileName)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        shareContent.bitmap = resource;
                        shareContent.shareContent = WechatShare.WEIXIN_SHARE_CONTENT_PIC;
                        wechatShare.shareByWX(shareContent);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        AppLogger.e("fxxx,load image failed: " + e.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void removeGuideAnymore() {
        PreferencesUtils.putBoolean(JConstant.KEY_WONDERFUL_GUIDE, false);
        mView.chooseEmptyView(VIEW_TYPE_EMPTY);
    }

    @Override
    protected void onRegisterResponseParser() {
        super.onRegisterResponseParser();
        registerResponseParser(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, this::onWonderfulAccountRsp);
    }

    private void onWonderfulAccountRsp(DataPoint... values) {
        List<DpMsgDefine.DPWonderItem> results = new ArrayList<>();
        DpMsgDefine.DPWonderItem bean;
        for (DpMsgDefine.DPWonderItem value : (DpMsgDefine.DPWonderItem[]) values) {
            bean = value;
            if (bean != null && !TextUtils.isEmpty(bean.cid)) {
                results.add(bean);
            }
        }
        updateCache(results);
        List<Long> times = assembleTimeLineData(results);
        mView.onMediaListRsp(results);
        mView.onTimeLineDataUpdate(times);
        mView.chooseEmptyView(results.size() > 0 ? VIEW_TYPE_HIDE : VIEW_TYPE_EMPTY);
    }
}

