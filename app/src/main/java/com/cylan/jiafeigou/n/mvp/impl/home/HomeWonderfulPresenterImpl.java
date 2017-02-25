package com.cylan.jiafeigou.n.mvp.impl.home;


import android.content.pm.PackageManager;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    private List<DpMsgDefine.DPWonderItem> mWonderItems = new ArrayList<>();
    private static final int MAX_DAY_COUNT = 40;
    private static final long DAY_TIME = 24 * 60 * 60 * 1000L;

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getTimeTickEventSub(), getPageScrolledSub());
    }

    @Override
    public void onStop() {
        super.onStop();
        registerSubscription(getDeleteWonderfulSub());
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        onUnRegisterSubscription();
    }

    private Subscription getDeleteWonderfulSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeleteWonder.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteWonder -> {
                    AppLogger.e("收到删除请求,正在删除");
                    deleteTimeline(deleteWonder.position);
                });
    }

    @Override
    public void onSetContentView() {
        if (showGuidePage()) {
            mView.chooseEmptyView(VIEW_TYPE_GUIDE);
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
                    onUnRegisterSubscription();
                    mView.onPageScrolled();
                });
    }

    @Override
    public void startRefresh() {
        queryTimeLine(0, 20, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    mWonderItems.clear();
                    mWonderItems.addAll(result);
                    mView.chooseEmptyView(mWonderItems.size() > 0 ? VIEW_TYPE_HIDE : VIEW_TYPE_EMPTY);
                    mView.onQueryTimeLineSuccess(mWonderItems, true);
                }, e -> {
                    if (e instanceof TimeoutException) {
                        mView.onQueryTimeLineTimeOut();
                    }
                    AppLogger.e("请求数据超时");
                });
    }

    @Override
    public void startLoadMore() {
        queryTimeLine(mWonderItems.get(mWonderItems.size() - 1).version, 20, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                }, e -> {
                    if (e instanceof TimeoutException) {
                        mView.onQueryTimeLineTimeOut();
                    }
                });
    }

    public Observable<List<DpMsgDefine.DPWonderItem>> queryTimeLine(long version, int count, boolean asc) {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            subscriber.onNext(sendQueryRequest(version, count, asc));
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.io())
                .filter(seq -> seq > 0)
                .flatMap(seq -> RxBus.getCacheInstance()
                        .toObservable(RxEvent.ParseResponseCompleted.class)
                        .filter(rsp -> rsp.seq == seq)
                        .first()
                        .timeout(10, TimeUnit.SECONDS))
                .map(rsp -> {
                    AppLogger.e("收到从服务器返回数据!!!");
                    DpMsgDefine.DPSet<DpMsgDefine.DPWonderItem> result = mSourceManager.getValue(mUUID, DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, rsp.seq);
                    return result.list();
                });
    }

    @Override
    public void deleteTimeline(int position) {
        Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, mWonderItems.get(position).version);
            params.add(msg);
            try {
                AppLogger.e("正在删除!");
                long seq = JfgCmdInsurance.getCmd().robotDelData("", params, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class).onBackpressureBuffer().filter(rsp -> rsp.seq == seq).first().timeout(10, TimeUnit.SECONDS))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp.resultCode == 0) {//success
                        AppLogger.e("删除成功");
                        DpMsgDefine.DPWonderItem item = mWonderItems.remove(position);
                        mWonderItems.remove(item);
                        mView.onDeleteWonderSuccess(position);
                        if (mWonderItems.isEmpty()) {//说明当天的已经删完了
                            mView.chooseEmptyView(VIEW_TYPE_EMPTY);
                        }
                        RxBus.getCacheInstance().post(new RxEvent.DeleteWonderRsp(true, position));
                    } else {
                        AppLogger.e("删除失败");
                    }
                }, e -> {
                    ToastUtil.showNegativeToast("删除失败!");
                    AppLogger.e(e.getMessage());
                });
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
    public void removeGuideAnymore() {
        PreferencesUtils.putBoolean(JConstant.KEY_WONDERFUL_GUIDE, false);
        mView.chooseEmptyView(VIEW_TYPE_EMPTY);
    }

    private long sendQueryRequest(long version, int count, boolean asc) {
        try {
            AppLogger.e("正在发送查询请求,version:" + version + "count:" + count + "acs:" + asc);
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, version);
            params.add(msg);
            return JfgCmdInsurance.getCmd().robotGetData("", params, count, asc, 0);//多请求一条数据,用来判断是否是一天最后一条
        } catch (JfgException e) {
            AppLogger.e(e.getMessage());
            return -1;
        }
    }
}

