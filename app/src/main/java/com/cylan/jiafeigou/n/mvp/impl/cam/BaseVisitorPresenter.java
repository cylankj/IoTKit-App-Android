package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.VisitorLoader;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.VisitorListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractFragmentPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author hds
 *         Created by hds on 17-10-20.
 */

public class BaseVisitorPresenter extends AbstractFragmentPresenter<VisitorListContract.View>
        implements VisitorListContract.Presenter {


    public BaseVisitorPresenter(VisitorListContract.View view) {
        super(view);
    }

    @Override
    public void fetchVisitorList() {
        if (containsSubscription(FETCH_VISITOR_LIST)) {
            return;
        }
        Subscription subscription = VisitorLoader.loadAllVisitorList(uuid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(r -> mView != null)
                .subscribe(visitorList -> mView.onVisitorListReady(visitorList), AppLogger::e);
        addSubscription(subscription, FETCH_VISITOR_LIST);
    }

    @Override
    public void start() {
        super.start();
        if (mView.isNormalVisitor()) {
            fetchVisitorList();
        }
        fetchStrangerVisitorList();
    }

    private static final String FETCH_VISITOR_LIST = "fetchVisitorList";
    private static final String FETCH_STRANGER_VISITOR_LIST = "fetchStrangerVisitorList";

    @Override
    public void fetchStrangerVisitorList() {
        if (containsSubscription(FETCH_STRANGER_VISITOR_LIST)) {
            return;
        }
        Subscription subscription = VisitorLoader.loadAllStrangerList(uuid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(r -> mView != null)
                .subscribe(visitorList -> mView.onVisitorListReady(visitorList), AppLogger::e);
        addSubscription(subscription, FETCH_STRANGER_VISITOR_LIST);
    }

    @Override
    public void fetchVisitsCount(@NotNull String faceId) {
        //msgType = 7
        //req=msgpack(cid, type, id)
        //rsp=msgpack(cid, type, id, count)
        final String sessionId = BaseApplication.getAppComponent().getCmd().getSessionId();
        AppLogger.d("sessionId:" + sessionId);
        try {
            DpMsgDefine.VisitsTimesReq reqContent = new DpMsgDefine.VisitsTimesReq();
            reqContent.cid = uuid;
            reqContent.faceId = faceId;
            reqContent.msgType = 7;
            final long seq = BaseApplication.getAppComponent()
                    .getCmd().sendUniservalDataSeq(7, DpUtils.pack(reqContent));
            Subscription su = RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp.class)
                    .filter(rsp -> rsp.seq == seq)
                    .subscribeOn(Schedulers.io())
                    .timeout(BuildConfig.DEBUG ? 3 : 10, TimeUnit.SECONDS, Observable.just(null))
                    .filter(ret -> mView != null)
                    .subscribe(rsp -> {
                        DpMsgDefine.VisitsTimesRsp rrsp = DpUtils.unpackDataWithoutThrow(rsp.data, DpMsgDefine.VisitsTimesRsp.class, null);
                        if (rrsp == null || !TextUtils.equals(rrsp.cid, uuid)) {
                            return;
                        }
                        mView.onVisitsTimeRsp(rrsp.faceFaceId, rrsp.count);
                    }, throwable -> {

                    });
            addSubscription(su, "fetchVisitsCount");

        } catch (JfgException e) {
            e.printStackTrace();
        }
    }
}
