package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.VisitorLoader;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.VisitorListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractFragmentPresenter;
import com.cylan.jiafeigou.n.view.cam.item.FaceItem;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.request.PostRequest;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Response;
import okhttp3.ResponseBody;
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
        Subscription subscription = VisitorLoader.loadAllVisitorList(uuid)
                .subscribeOn(Schedulers.io())
                .map(ret -> {
                    List<FaceItem> result = new ArrayList<>();
                    if (ret != null && ret.dataList != null) {
                        FaceItem item;
                        for (DpMsgDefine.Visitor visitor : ret.dataList) {
                            item = new FaceItem();
                            item.withFaceType(FaceItem.FACE_TYPE_ACQUAINTANCE);
                            item.withVisitor(visitor);
                            item.withUuid(uuid);
                            result.add(item);
                        }
                    }
                    return result;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(r -> mView != null)
                .timeout(10, TimeUnit.SECONDS)
                .subscribe(visitorList -> mView.onVisitorListReady(visitorList), AppLogger::e);
        addSubscription(subscription, FETCH_VISITOR_LIST);
    }

    @Override
    public void start() {
        super.start();
    }


    private static final String FETCH_VISITOR_LIST = "fetchVisitorList";
    private static final String FETCH_STRANGER_VISITOR_LIST = "fetchStrangerVisitorList";

    @Override
    public void fetchStrangerVisitorList() {
//        if (containsSubscription(FETCH_STRANGER_VISITOR_LIST)) {
//            return;
//        }
        Subscription subscription = VisitorLoader.loadAllStrangerList(uuid)
                .subscribeOn(Schedulers.io())
                .map(ret -> {
                    List<FaceItem> result = new ArrayList<>();
                    if (ret != null && ret.strangerVisitors != null) {
                        FaceItem item;
                        for (DpMsgDefine.StrangerVisitor strangerVisitor : ret.strangerVisitors) {
                            item = new FaceItem();
                            item.withFaceType(FaceItem.FACE_TYPE_STRANGER_SUB);
                            item.withStrangerVisitor(strangerVisitor);
                            item.withSetSelected(false);
                            item.withUuid(uuid);
                            result.add(item);
                        }
                    }
                    return result;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(r -> mView != null)
                .subscribe(visitorList -> mView.onStrangerVisitorListReady(visitorList), AppLogger::e);
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
                        AppLogger.w("获取未读数:" + rrsp.toString());
                        mView.onVisitsTimeRsp(rrsp.faceFaceId, rrsp.count);
                    }, throwable -> {

                    });
            addSubscription(su, "fetchVisitsCount");

        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    //    cid, type, id, delMsg

    public Observable<RxEvent.UniversalDataRsp> deleteFaceByDp(int type, String id, int delMsg) {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                long seq = BaseApplication.getAppComponent()
                        .getCmd().sendUniservalDataSeq(9, DpUtils.pack(new DpMsgDefine.DelVisitorReq(uuid, type, id, delMsg)));
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp.class).first(ret -> ret.seq == seq));
    }

    public Observable<DpMsgDefine.ResponseHeader> deleteFaceByRobot(String faceId) {
        return Observable.create(subscriber -> {
            try {
                String account = DataSourceManager.getInstance().getAccount().getAccount();
                String vid = Security.getVId();
                String serviceKey = OptionsImpl.getServiceKey(vid);
                String timestamp = String.valueOf((System.currentTimeMillis() / 1000));//这里的时间是秒
                String seceret = OptionsImpl.getServiceSeceret(vid);
                String sessionId = BaseApplication.getAppComponent().getCmd().getSessionId();
                if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                    subscriber.onError(new IllegalArgumentException("ServiceKey或Seceret为空"));
                } else {
                    String sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_DELETE_API, seceret, timestamp);
                    String url = OptionsImpl.getRobotServer() + JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_DELETE_API;
                    if (!url.startsWith("http://")) {
                        url = "http://" + url;
                    }
                    PostRequest request = OkGo.post(url)
                            .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_VID, vid)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICE_KEY, serviceKey)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_BUSINESS, "1")
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICETYPE, "1")
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SIGN, sign)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_TIMESTAMP, timestamp)

                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_ACCOUNT, account)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SN, uuid)
                            .params(JConstant.RobotCloudApi.ACCESS_TOKEN, sessionId);

//                    if (!TextUtils.isEmpty(faceId)) {
                    request.params(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_ID, "[" + faceId + "]");
//                    }
                    Response response = request.execute();
                    ResponseBody body = response.body();
                    if (body != null) {
                        String string = body.string();
                        AppLogger.w(string);
                        Gson gson = new Gson();
                        DpMsgDefine.ResponseHeader header = gson.fromJson(string, DpMsgDefine.ResponseHeader.class);
                        subscriber.onNext(header);
                        subscriber.onCompleted();
                    }
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    public Observable<DpMsgDefine.ResponseHeader> deletePersonByRobot(String personId) {
        return Observable.create(subscriber -> {
            try {
                String account = DataSourceManager.getInstance().getAccount().getAccount();
                String vid = Security.getVId();
                String serviceKey = OptionsImpl.getServiceKey(vid);
                String timestamp = String.valueOf((System.currentTimeMillis() / 1000));//这里的时间是秒
                String seceret = OptionsImpl.getServiceSeceret(vid);
                String sessionId = BaseApplication.getAppComponent().getCmd().getSessionId();
                if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                    subscriber.onError(new IllegalArgumentException("ServiceKey或Seceret为空"));
                } else {
                    String sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_PERSON_DELETE_API, seceret, timestamp);
                    String url = OptionsImpl.getRobotServer() + JConstant.RobotCloudApi.ROBOTSCLOUD_PERSON_DELETE_API;
                    if (!url.startsWith("http://")) {
                        url = "http://" + url;
                    }
                    PostRequest request = OkGo.post(url)
                            .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_VID, vid)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICE_KEY, serviceKey)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_BUSINESS, "1")
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICETYPE, "1")
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SIGN, sign)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_TIMESTAMP, timestamp)

                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_ACCOUNT, account)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SN, uuid)
                            .params(JConstant.RobotCloudApi.ACCESS_TOKEN, sessionId);

//                    if (!TextUtils.isEmpty(faceId)) {
                    request.params(JConstant.RobotCloudApi.ROBOTSCLOUD_PERSON_ID, personId);
//                    }
                    Response response = request.execute();
                    ResponseBody body = response.body();
                    if (body != null) {
                        String string = body.string();
                        AppLogger.w(string);
                        Gson gson = new Gson();
                        DpMsgDefine.ResponseHeader header = gson.fromJson(string, DpMsgDefine.ResponseHeader.class);
                        subscriber.onNext(header);
                        subscriber.onCompleted();
                    }
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    @Override
    public void deleteFace(int type, String id, int delMsg) {
        Observable<DpMsgDefine.ResponseHeader> faceByRobot = null;
        if (type == 1) {
            faceByRobot = deleteFaceByRobot(id);
        } else if (type == 2) {
            faceByRobot = deletePersonByRobot(id);
        }

        Subscription subscribe = Observable.zip(deleteFaceByDp(type, id, delMsg), faceByRobot,
                (universalDataRsp, responseHeader) -> {
                    Integer result = DpUtils.unpackDataWithoutThrow(universalDataRsp.data, int.class, -1);
                    if (result == 0 && responseHeader.ret == 0) {
                        //删除成功了
                        return universalDataRsp;
                    }
                    return null;
                })
                .subscribeOn(Schedulers.io())
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp != null) {
                        Integer result = DpUtils.unpackDataWithoutThrow(rsp.data, int.class, -1);
                        if (result == 0) {
                            mView.onDeleteFaceSuccess(type, delMsg);
                        } else {
                            mView.onDeleteFaceError();
                        }
                    } else {
                        mView.onDeleteFaceError();
                    }
                }, throwable -> {
                    AppLogger.e(MiscUtils.getErr(throwable));
                });
        addSubscription(getMethodName(), subscribe);
    }
}
