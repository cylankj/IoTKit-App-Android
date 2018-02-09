package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.VisitorLoader;
import com.cylan.jiafeigou.module.Command;
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
import com.cylan.jiafeigou.utils.NetUtils;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.request.PostRequest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.msgpack.type.Value;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.RequestBody;
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
//        setSSL();
        subscriberDeviceSync();
    }

    private void subscriberDeviceSync() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter((RxEvent.DeviceSyncRsp data) -> (getView() != null && TextUtils.equals(uuid, data.uuid)))
                .subscribeOn(Schedulers.io())
                .map(deviceSyncRsp -> {
                    Device device = DataSourceManager.getInstance().getDevice(uuid);
                    int count = device.$(1001, 0) +
                            device.$(1002, 0) +
                            device.$(1003, 0) +
                            device.$(1004, 0) +
                            device.$(1005, 0) +
                            device.$(1006, 0);
                    return count;
                })

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msgCount -> {
                    mView.onReceiveNewMessage(msgCount);
                    AppLogger.d("收到,属性同步了");
                }, e -> AppLogger.d(e.getMessage()));
        addStopSubscription(subscribe);
    }

    private static void setSSL() {
        OkGo.getInstance().setHostnameVerifier((hostname, session) -> true);
        OkGo.getInstance().setCertificates(new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
        });
    }

    @Override
    public void fetchVisitorList(long version) {
        Subscription subscription = VisitorLoader.loadAllVisitorList(uuid, version)
                .subscribeOn(Schedulers.io())
                .map(ret -> {
                    List<FaceItem> result = new ArrayList<>();
                    if (ret != null && ret.dataList != null) {
                        FaceItem item;
                        for (DpMsgDefine.Visitor visitor : ret.dataList) {
                            if (visitor.detailList == null || visitor.detailList.size() == 0) {
                                continue;
                            }
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
                .timeout(30, TimeUnit.SECONDS)
                .subscribe(visitorList -> mView.onVisitorListReady(visitorList, version), e -> {
                    e.printStackTrace();
                    AppLogger.e(e);
                });
        addSubscription(subscription, FETCH_VISITOR_LIST);
    }

    @Override
    public void start() {
        super.start();
    }


    private static final String FETCH_VISITOR_LIST = "fetchVisitorList";
    private static final String FETCH_STRANGER_VISITOR_LIST = "fetchStrangerVisitorList";

    @Override
    public void fetchStrangerVisitorList(long version) {
        Subscription subscription = VisitorLoader.loadAllStrangerList(uuid, version)
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
                .subscribe(visitorList -> mView.onStrangerVisitorListReady(visitorList, version), AppLogger::e);
        addSubscription(subscription, FETCH_STRANGER_VISITOR_LIST);
    }

    @Override
    public void fetchVisitsCount(String faceId, int type) {
        //msgType = 7
        //req=msgpack(cid, type, id)
        //rsp=msgpack(cid, type, id, count)
        final String sessionId = Command.getInstance().getSessionId();
        AppLogger.d("sessionId:" + sessionId);
        try {
            DpMsgDefine.VisitsTimesReq reqContent = new DpMsgDefine.VisitsTimesReq();
            reqContent.cid = uuid;
            reqContent.faceId = faceId;
            reqContent.msgType = type;
            final long seq = Command.getInstance().sendUniservalDataSeq(16, DpUtils.pack(reqContent));
            Subscription su = RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp.class)
                    .filter(rsp -> rsp.seq == seq)
                    .subscribeOn(Schedulers.io())
                    .timeout(BuildConfig.DEBUG ? 3 : 10, TimeUnit.SECONDS, Observable.just(null))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(rsp -> {
                        DpMsgDefine.VisitsTimesRsp rrsp = DpUtils.unpackDataWithoutThrow(rsp.data, DpMsgDefine.VisitsTimesRsp.class, null);
                        if (rrsp == null || !TextUtils.equals(rrsp.cid, uuid)) {
                            return;
                        }
                        AppLogger.w("获取未读数:" + rrsp.toString());
                        mView.onVisitsTimeRsp(rrsp.faceFaceId, rrsp.count, type);
                    }, throwable -> {
                        AppLogger.e(MiscUtils.getErr(throwable));
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
                long seq = Command.getInstance().sendUniservalDataSeq(18, DpUtils.pack(new DpMsgDefine.DelVisitorReq(uuid, type, id, delMsg)));
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
                vid = "0001";
                String serviceKey = OptionsImpl.getServiceKey(vid);
                String timestamp = String.valueOf((System.currentTimeMillis() / 1000));//这里的时间是秒
                String seceret = OptionsImpl.getServiceSeceret(vid);
                String sessionId = Command.getInstance().getSessionId();
                if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                    subscriber.onError(new IllegalArgumentException("ServiceKey或Seceret为空"));
                } else {
                    String sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_DELETE_API, seceret, timestamp);
                    DpMsgDefine.GetRobotServerRsp serverRsp = OptionsImpl.getRobotServer(uuid, vid);
                    String url = serverRsp.host + ":" + serverRsp.port + JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_DELETE_API;
                    if (!url.startsWith("http://")) {
                        url = "http://" + url;
                    }
                    PostRequest request = OkGo.post(url)
                            .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                            //TODO 现在 VID 写死成 0001
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
                vid = "0001";
                String serviceKey = OptionsImpl.getServiceKey(vid);
                String timestamp = String.valueOf((System.currentTimeMillis() / 1000));//这里的时间是秒
                String seceret = OptionsImpl.getServiceSeceret(vid);
                String sessionId = Command.getInstance().getSessionId();
                if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                    subscriber.onError(new IllegalArgumentException("ServiceKey或Seceret为空"));
                } else {
                    String sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_PERSON_DELETE_API, seceret, timestamp);
                    DpMsgDefine.GetRobotServerRsp serverRsp = OptionsImpl.getRobotServer(uuid, vid);
                    String url = serverRsp.host + ":" + serverRsp.port + JConstant.RobotCloudApi.ROBOTSCLOUD_PERSON_DELETE_API;
                    if (!url.startsWith("http://")) {
                        url = "http://" + url;
                    }
                    PostRequest request = OkGo.post(url)
                            .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                            //TODO 现在 VID 写死成 0001
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
        String method = method();
        Observable<RxEvent.UniversalDataRsp> deleteObserver = null;
        if (type == 1) {
            deleteObserver = Observable.zip(deleteFaceByDp(type, id, delMsg), deleteFaceByRobot(id), (universalDataRsp, responseHeader) -> {
                Integer result = DpUtils.unpackDataWithoutThrow(universalDataRsp.data, int.class, -1);
                if (result == 0 && responseHeader.ret == 0) {
                    //删除成功了
                    return universalDataRsp;
                }
                return null;
            });
        } else if (type == 2) {
            deleteObserver = Observable.zip(deleteFaceByDp(type, id, delMsg), deletePersonByRobot(id),
                    (universalDataRsp, responseHeader) -> {
                        Integer result = DpUtils.unpackDataWithoutThrow(universalDataRsp.data, int.class, -1);
                        if (result == 0 && responseHeader.ret == 0) {
                            //删除成功了
                            return universalDataRsp;
                        }
                        return null;
                    });
        }

        if (deleteObserver != null) {
            Subscription subscribe = deleteObserver
                    .subscribeOn(Schedulers.io())
                    .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(applyLoading(false, R.string.LOADING))
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

    @Override
    public void deleteFaceV2(int type, @NotNull String id, int delMsg) {
        long time = System.currentTimeMillis() / 1000;
        Observable<Integer> observable = Observable.create((Observable.OnSubscribe<Boolean>) subscriber -> {
            boolean hasNetwork = NetUtils.hasNetwork();
            if (!hasNetwork) {
                mView.onDeleteFaceError();
            } else {
                subscriber.onNext(true);
            }
            subscriber.onCompleted();
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(req -> {
                    try {
                        return Command.getInstance().sendUniservalDataSeq(14, DpUtils.pack(0));
                    } catch (JfgException e) {
                        AppLogger.e(MiscUtils.getErr(e));
                        e.printStackTrace();
                    }
                    return -1L;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp.class).filter(rsp -> rsp.seq == seq))
                .map(rsp -> {
                    Value unpack = DpUtils.unpack(rsp.data);
                    if (unpack != null && unpack.isArrayValue()) {
                        return unpack.asArrayValue().get(0).asRawValue().getString();
                    }
                    return null;
                })
                .map(authToken -> {
                    try {
                        String server = ("http://" + OptionsImpl.getServer() + ":8082").replace(":443", "");
                        String aiAppApi = server + "/aiapp";
                        JSONObject tokenParams = new JSONObject();
                        tokenParams.put("action", "DeletePerson");
                        tokenParams.put("auth_token", authToken);
                        tokenParams.put("time", time);
                        tokenParams.put("person_id", id);
                        Response execute = OkGo.post(aiAppApi)
                                .requestBody(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), tokenParams.toString()))
                                .execute();
                        JSONObject jsonObject = new JSONObject(execute.body().string());
                        Log.e("BaseVisitorPresenter", "DeletePerson response:" + jsonObject);
                        return jsonObject.optInt("code", -1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        AppLogger.e(e);
                    }
                    return -1;
                })
                .first();
        Subscription subscribe = Observable.zip(deleteFaceByDp(type, id, delMsg), observable,
                (universalDataRsp, responseHeader) -> {
                    Integer result = DpUtils.unpackDataWithoutThrow(universalDataRsp.data, int.class, -1);
                    return responseHeader;
                })
                .timeout(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(applyLoading(false, R.string.LOADING))
                .subscribe(code -> {
                    switch (code) {
                        case 100: {
                            mView.onDeleteFaceErrorPermissionError();
                        }
                        break;
                        case 101: {
                            mView.onDeleteFaceErrorInvalidParams();
                        }
                        break;
                        case 102: {
                            mView.onDeleteFaceErrorServerInternalError();
                        }
                        break;
                        case 200: {
                            mView.onDeleteFaceSuccess(type, delMsg);
                        }
                        break;
                        case -1: {
                            mView.onDeleteFaceError();
                        }
                        break;
                        default: {
                            mView.onDeleteFaceError();
                        }
                    }
                }, throwable -> {
                    if (throwable instanceof TimeoutException) {
                        mView.onDeleteFaceTimeout();
                    } else {
                        mView.onDeleteFaceError();
                    }
                    throwable.printStackTrace();
                    AppLogger.e(MiscUtils.getErr(throwable));
                });
        addStopSubscription(subscribe);
    }
}
