package com.cylan.jiafeigou.n.view.panorama;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.PanoramaEvent;
import com.cylan.jiafeigou.base.wrapper.BaseViewablePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.ver.AbstractVersion;
import com.cylan.jiafeigou.misc.ver.PanDeviceVersionChecker;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rtmp.youtube.util.EventData;
import com.cylan.jiafeigou.rtmp.youtube.util.YouTubeApi;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.gson.Gson;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.exception.WeiboHttpException;
import com.sina.weibo.sdk.net.RequestListener;
import com.umeng.facebook.AccessToken;
import com.umeng.facebook.FacebookRequestError;
import com.umeng.facebook.FacebookSdk;
import com.umeng.facebook.GraphRequest;
import com.umeng.facebook.HttpMethod;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.weibo.live.WeiboLiveCreate;
import com.weibo.live.WeiboLiveDelete;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.base.module.PanoramaEvent.ERROR_CODE_HTTP_NOT_AVAILABLE;
import static com.cylan.jiafeigou.dp.DpUtils.pack;
import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/**
 * Created by yanzhendong on 2017/3/8.
 */
public class PanoramaPresenter extends BaseViewablePresenter<PanoramaCameraContact.View> implements PanoramaCameraContact.Presenter {

    private boolean isFirst = true;
    //    private boolean shouldRefreshRecord = false;
    private volatile int battery = -1;
    private volatile boolean charge = false;
    private boolean notifyBatteryLow = true;
    private volatile boolean isRecording = false;
    private volatile boolean isRtmpLive = false;
    private volatile boolean hasSDCard;
    private volatile boolean isInProgress = false;
//    private YouTubeApi.LiveEvent liveEvent;

    @Override
    public boolean isApiAvailable() {
        RxEvent.PanoramaApiAvailable event = RxBus.getCacheInstance().getStickyEvent(RxEvent.PanoramaApiAvailable.class);
        return event != null && event.ApiType >= 0;
    }

    @Override
    public void startYoutubeLiveRtmp(String url) {
        Subscription subscribe = Observable.just("cameraLiveRtmpCtrl")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        ArrayList<JFGDPMsg> params = new ArrayList<>();
                        DpMsgDefine.DPCameraLiveRtmpCtrl ctrl = new DpMsgDefine.DPCameraLiveRtmpCtrl(url, 1);
                        AppLogger.w("ctrl is " + ctrl.toString());
                        JFGDPMsg msg = new JFGDPMsg(516, 0, DpUtils.pack(ctrl));
                        params.add(msg);
                        return BaseApplication.getAppComponent().getCmd().robotSetData(uuid, params);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(MiscUtils.getErr(e));
                    }
                    return -1L;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class).first(setDataRsp -> setDataRsp.seq == seq))
                .observeOn(AndroidSchedulers.mainThread())
                .filter(rsp -> {
                    boolean success = rsp != null && rsp.rets != null && rsp.rets.size() > 0 && rsp.rets.get(0).ret == 0;
                    mView.onSendCameraLiveResponse(1, success);
                    return success;
                })
                .observeOn(Schedulers.newThread())//需要一个新线程以使 timeout 生效
                .map(ret -> {
                    try {
                        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mView.getAppContext(), YouTubeScopes.all());
                        credential.setSelectedAccountName(PreferencesUtils.getString(JConstant.YOUTUBE_PREF_ACCOUNT_NAME));
                        YouTube youTube = new YouTube.Builder(AndroidHttp.newCompatibleTransport(),
                                JacksonFactory.getDefaultInstance(),
                                credential
                        )
                                .setApplicationName(mView.getAppContext().getString(R.string.app_name))
                                .build();
                        String string = PreferencesUtils.getString(JConstant.YOUTUBE_PREF_CONFIGURE + ":" + uuid, null);
                        EventData eventData = JacksonFactory.getDefaultInstance().fromString(string, EventData.class);
                        if (eventData != null) {
                            YouTubeApi.startEvent(youTube, eventData.getId(), eventData.getEvent().getContentDetails().getBoundStreamId());
                        }
                    } catch (Exception e) {
                        try {
                            // TODO: 2017/9/12 出错了 关掉设备上的直播
                            ArrayList<JFGDPMsg> params = new ArrayList<>();
                            DpMsgDefine.DPCameraLiveRtmpCtrl ctrl = new DpMsgDefine.DPCameraLiveRtmpCtrl(url, 0);
                            AppLogger.w("ctrl is " + ctrl.toString());
                            JFGDPMsg msg = new JFGDPMsg(516, 0, DpUtils.pack(ctrl));
                            params.add(msg);
                            BaseApplication.getAppComponent().getCmd().robotSetData(uuid, params);
                        } catch (JfgException e1) {
                            e.printStackTrace();
                            AppLogger.e(MiscUtils.getErr(e));
                        }
                        return e;
                    }
                    return null;
                })
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> isInProgress = true)
                .doOnTerminate(() -> isInProgress = false)
                .subscribe(error -> {
                    //在这里查询517的结果不准确,设备会自己推消息过来的
                    if (error == null) {
                        // TODO: 2017/9/12 没有异常,正常播放了
                    } else if (error instanceof IllegalArgumentException) {
                        mView.onRtmpAddressError();
                    } else if (error instanceof GoogleJsonResponseException) {
                        mView.onRtmpAddressError();
                    } else {
                        AppLogger.w(MiscUtils.getErr(error));
                    }
                }, e -> {
                    if (e instanceof TimeoutException) {
                        mView.onRtmpAddressError();
                    }
                    AppLogger.w(MiscUtils.getErr(e));
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#startYoutubeLiveRtmp", subscribe);
    }

    @Override
    public void stopYoutubeLiveRtmp() {
        Subscription subscribe = Observable.just("cameraLiveRtmpCtrl")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        ArrayList<JFGDPMsg> params = new ArrayList<>();
                        DpMsgDefine.DPCameraLiveRtmpCtrl ctrl = new DpMsgDefine.DPCameraLiveRtmpCtrl("", 0);
                        AppLogger.w("ctrl is " + ctrl.toString());
                        JFGDPMsg msg = new JFGDPMsg(516, 0, DpUtils.pack(ctrl));
                        params.add(msg);
                        return BaseApplication.getAppComponent().getCmd().robotSetData(uuid, params);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(MiscUtils.getErr(e));
                    }
                    return -1L;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class).first(setDataRsp -> setDataRsp.seq == seq))
                .observeOn(AndroidSchedulers.mainThread())
                .filter(rsp -> {
                    boolean success = rsp != null && rsp.rets != null && rsp.rets.size() > 0 && rsp.rets.get(0).ret == 0;
                    mView.onSendCameraLiveResponse(0, success);
                    return success;
                })
                .observeOn(Schedulers.newThread())//需要创建新的线程
                .map(ret -> {
                    try {
                        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mView.getAppContext(), YouTubeScopes.all());
                        credential.setSelectedAccountName(PreferencesUtils.getString(JConstant.YOUTUBE_PREF_ACCOUNT_NAME));
                        YouTube youTube = new YouTube.Builder(AndroidHttp.newCompatibleTransport(),
                                JacksonFactory.getDefaultInstance(),
                                credential
                        )
                                .setApplicationName(mView.getAppContext().getString(R.string.app_name))
                                .build();
                        String string = PreferencesUtils.getString(JConstant.YOUTUBE_PREF_CONFIGURE + ":" + uuid, null);
                        EventData eventData = JacksonFactory.getDefaultInstance().fromString(string, EventData.class);
                        YouTubeApi.endEvent(youTube, eventData.getId());
                    } catch (Exception e) {
                        AppLogger.w(MiscUtils.getErr(e));
                        return e;
                    }
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .doOnSubscribe(() -> RxBus.getCacheInstance().post(PanoramaCameraContact.View.RecordFinishEvent.INSTANCE))
                .subscribe(error -> {
                    mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_LIVE, getLiveAction().hasResolution, false);
                }, e -> {
                    mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_LIVE, getLiveAction().hasResolution, false);
                    AppLogger.w(MiscUtils.getErr(e));
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#stopYoutubeLiveRtmp", subscribe);
    }

    private void startWeiboLiveRtmp() {
        Subscription subscribe = Observable.create((Observable.OnSubscribe<String>) subscriber -> {
            String map = PreferencesUtils.getString(JConstant.OPEN_LOGIN_MAP + SHARE_MEDIA.SINA.toString(), null);
            Oauth2AccessToken oauth2AccessToken = Oauth2AccessToken.parseAccessToken(map);
            if (oauth2AccessToken == null || oauth2AccessToken.getExpiresTime() - System.currentTimeMillis() <= 0) {
                mView.showRtmpLiveSetting();
            } else {
                Context context = mView.getAppContext();
                mView.showBottomPanelInformation(context.getString(R.string.LIVE_CREATING, getPlatformString(context, 2)), false);
                String defaultContent = context.getString(R.string.LIVE_DETAIL_DEFAULT_CONTENT);
                String content = PreferencesUtils.getString(JConstant.WEIBO_PREF_DESCRIPTION + ":" + uuid, defaultContent);
                WeiboLiveCreate weiboLiveCreate = new WeiboLiveCreate(mView.getAppContext(), PackageUtils.getMetaString(ContextUtils.getContext(), "sinaAppKey"), oauth2AccessToken);
                weiboLiveCreate.setAc(mView.getActivityContext());
                weiboLiveCreate.setTitle(defaultContent);
                weiboLiveCreate.setHeight("1080");
                weiboLiveCreate.setWidth("1920");
                weiboLiveCreate.setSummary(content);
                weiboLiveCreate.setPublished("1");
                weiboLiveCreate.setReplay("1");
                weiboLiveCreate.setPanoLive("1");
                weiboLiveCreate.createLive(new RequestListener() {
                    @Override
                    public void onComplete(String s) {
                        AppLogger.w(s);

                        try {
                            Map result = new Gson().fromJson(s, Map.class);
                            String url = (String) result.get("url");
                            String id = (String) result.get("id");
                            if (!TextUtils.isEmpty(id)) {
                                PreferencesUtils.putString(JConstant.WEIBO_PREF_LIVE_ID + ":" + uuid, id);
                            }
                            if (TextUtils.isEmpty(url)) {
                                // TODO: 2017/9/13 获取 URL 失败了
                            } else {
                                PreferencesUtils.putString(JConstant.WEIBO_PREF_LIVE_URL + ":" + uuid, url);
                                subscriber.onNext(url);
                            }
                            subscriber.onCompleted();
                        } catch (Exception e) {
                            AppLogger.w(e.getMessage());
                            subscriber.onError(e);
                        }

                    }

                    @Override
                    public void onWeiboException(WeiboException e) {
                        subscriber.onError(e);
                        AppLogger.w(e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(url -> {
                    try {
                        ArrayList<JFGDPMsg> params = new ArrayList<>();
                        DpMsgDefine.DPCameraLiveRtmpCtrl ctrl = new DpMsgDefine.DPCameraLiveRtmpCtrl("", 0);
                        AppLogger.w("ctrl is " + ctrl.toString());
                        JFGDPMsg msg = new JFGDPMsg(516, 0, DpUtils.pack(ctrl));
                        params.add(msg);
                        return BaseApplication.getAppComponent().getCmd().robotSetData(uuid, params);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(MiscUtils.getErr(e));
                    }
                    return -1L;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class).first(setDataRsp -> setDataRsp.seq == seq))
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> isInProgress = true)
                .doOnTerminate(() -> isInProgress = false)
                .subscribe(rsp -> {
                            // TODO: 2017/9/18 在这里查询517不准确,设备会自己推过来的
                            boolean success = rsp != null && rsp.rets != null && rsp.rets.size() > 0 && rsp.rets.get(0).ret == 0;
                            mView.onSendCameraLiveResponse(0, success);
                        }, e -> {
                            if (e instanceof TimeoutException) {
                                mView.onRtmpAddressError();
                            } else if (e instanceof WeiboHttpException) {
                                int statusCode = ((WeiboHttpException) e).getStatusCode();
                                mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_LIVE, getLiveAction().hasResolution, false);
                            } else {
                                mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_LIVE, getLiveAction().hasResolution, false);
                            }
                            AppLogger.w(MiscUtils.getErr(e));
                        }
                );
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#startWeiboLiveRtmp", subscribe);

    }

    private void stopWeiboLiveRtmp() {
        Subscription subscribe = Observable.just("cameraLiveRtmpCtrl")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        ArrayList<JFGDPMsg> params = new ArrayList<>();
                        DpMsgDefine.DPCameraLiveRtmpCtrl ctrl = new DpMsgDefine.DPCameraLiveRtmpCtrl("", 0);
                        AppLogger.w("ctrl is " + ctrl.toString());
                        JFGDPMsg msg = new JFGDPMsg(516, 0, DpUtils.pack(ctrl));
                        params.add(msg);
                        return BaseApplication.getAppComponent().getCmd().robotSetData(uuid, params);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(MiscUtils.getErr(e));
                    }
                    return -1L;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class).first(setDataRsp -> setDataRsp.seq == seq))
                .observeOn(AndroidSchedulers.mainThread())
                .filter(rsp -> {
                    boolean success = rsp != null && rsp.rets != null && rsp.rets.size() > 0 && rsp.rets.get(0).ret == 0;
                    mView.onSendCameraLiveResponse(0, success);
                    return success;
                })
                .observeOn(Schedulers.newThread())//需要创建新的线程
                .flatMap(ret -> Observable.create((Observable.OnSubscribe<String>) subscriber -> {

                    String map = PreferencesUtils.getString(JConstant.OPEN_LOGIN_MAP + SHARE_MEDIA.SINA.toString(), null);
                    Oauth2AccessToken oauth2AccessToken = Oauth2AccessToken.parseAccessToken(map);
                    String liveId = PreferencesUtils.getString(JConstant.WEIBO_PREF_LIVE_ID + ":" + uuid, null);
                    if (oauth2AccessToken == null || oauth2AccessToken.getExpiresTime() - System.currentTimeMillis() <= 0 || TextUtils.isEmpty(liveId)) {
//                        mView.showRtmpLiveSetting();
                        // TODO: 2017/9/14 没有 token ,还有这种操作?
                        if (oauth2AccessToken != null && oauth2AccessToken.getExpiresTime() - System.currentTimeMillis() <= 0) {
                            PreferencesUtils.remove(JConstant.OPEN_LOGIN_MAP + SHARE_MEDIA.SINA.toString());
                        }
                        subscriber.onError(new IllegalArgumentException("token 或者 直播 id 不存在"));
                    } else {
                        Context context = mView.getAppContext();
                        WeiboLiveDelete liveDelete = new WeiboLiveDelete(context, PackageUtils.getMetaString(ContextUtils.getContext(), "sinaAppKey"), oauth2AccessToken);
                        liveDelete.setId(liveId);
                        liveDelete.deleteLive(new RequestListener() {
                            @Override
                            public void onComplete(String s) {
                                PreferencesUtils.remove(JConstant.WEIBO_PREF_LIVE_URL + ":" + uuid);
                                PreferencesUtils.remove(JConstant.WEIBO_PREF_LIVE_ID + ":" + uuid);
                                subscriber.onNext(s);
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onWeiboException(WeiboException e) {
                                subscriber.onError(e);
                                AppLogger.e(MiscUtils.getErr(e));
                            }
                        });
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .doOnSubscribe(() -> RxBus.getCacheInstance().post(PanoramaCameraContact.View.RecordFinishEvent.INSTANCE))
                .subscribe(error -> {
                    mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_LIVE, getLiveAction().hasResolution, false);
                }, e -> {
                    mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_LIVE, getLiveAction().hasResolution, false);
                    AppLogger.w(MiscUtils.getErr(e));
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#stopWeiboLiveRtmp", subscribe);
    }

    private void startRtmpLive(String url) {
        Subscription subscribe = Observable.just("cameraLiveRtmpCtrl")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        ArrayList<JFGDPMsg> params = new ArrayList<>();
                        DpMsgDefine.DPCameraLiveRtmpCtrl ctrl = new DpMsgDefine.DPCameraLiveRtmpCtrl(url, 1);
                        AppLogger.w("ctrl is " + ctrl.toString());
                        JFGDPMsg msg = new JFGDPMsg(516, 0, DpUtils.pack(ctrl));
                        params.add(msg);
                        return BaseApplication.getAppComponent().getCmd().robotSetData(uuid, params);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(MiscUtils.getErr(e));
                    }
                    return -1L;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class).first(setDataRsp -> setDataRsp.seq == seq))
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> isInProgress = true)
                .doOnTerminate(() -> isInProgress = false)
                .subscribe(rsp -> {
                    boolean success = rsp != null && rsp.rets != null && rsp.rets.size() > 0 && rsp.rets.get(0).ret == 0;
                    mView.onSendCameraLiveResponse(1, success);
                }, e -> {
                    if (e instanceof TimeoutException) {
                        mView.onRtmpAddressError();
                    }
                    AppLogger.w(MiscUtils.getErr(e));
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#startRtmpLive", subscribe);
    }

    private void stopRtmpLive() {
        Subscription subscribe = Observable.just("cameraLiveRtmpCtrl")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        ArrayList<JFGDPMsg> params = new ArrayList<>();
                        DpMsgDefine.DPCameraLiveRtmpCtrl ctrl = new DpMsgDefine.DPCameraLiveRtmpCtrl("", 0);
                        AppLogger.w("ctrl is " + ctrl.toString());
                        JFGDPMsg msg = new JFGDPMsg(516, 0, DpUtils.pack(ctrl));
                        params.add(msg);
                        return BaseApplication.getAppComponent().getCmd().robotSetData(uuid, params);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(MiscUtils.getErr(e));
                    }
                    return -1L;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class).first(setDataRsp -> setDataRsp.seq == seq))
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .doOnSubscribe(() -> RxBus.getCacheInstance().post(PanoramaCameraContact.View.RecordFinishEvent.INSTANCE))
                .subscribe(rsp -> {
                    boolean success = rsp != null && rsp.rets != null && rsp.rets.size() > 0 && rsp.rets.get(0).ret == 0;
                    mView.onSendCameraLiveResponse(0, success);
                    mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_LIVE, getLiveAction().hasResolution, false);
                }, e -> {
                    mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_LIVE, getLiveAction().hasResolution, false);
                    AppLogger.w(MiscUtils.getErr(e));
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#stopYoutubeLiveRtmp", subscribe);
    }

    private void startFacebookRtmpLive() {
        Subscription subscribe = Observable.just("startFacebookRtmpLive")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.newThread())//新建线程吧,以免阻塞 IO 线程
                .map(cmd -> {
                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                    String defaultContent = ContextUtils.getContext().getString(R.string.LIVE_DETAIL_DEFAULT_CONTENT);
                    String description = PreferencesUtils.getString(JConstant.FACEBOOK_PREF_DESCRIPTION + ":" + uuid, defaultContent);
                    String privacy = PreferencesUtils.getString(JConstant.FACEBOOK_PREF_PERMISSION_KEY + ":" + uuid, "EVERYONE");
                    Bundle params = new Bundle();
//                    params.putString("content_tags", "panorama,全景");
                    params.putString("description", description);
                    params.putString("privacy", String.format("{value:\"%s\"}", privacy));
                    params.putString("status", "LIVE_NOW");
//                    params.putBoolean("save_vod", true);
                    params.putBoolean("stop_on_delete_stream", true);
//                    params.putString("stream_type", "REGULAR");
                    params.putString("title", defaultContent);
                    params.putBoolean("is_spherical", true);
                    String graphPath = String.format("/%s/live_videos", accessToken.getUserId());
                    GraphRequest graphRequest = new GraphRequest(accessToken, graphPath, params, HttpMethod.POST);
                    AppLogger.w("Facebook 请求参数为:" + graphRequest.toString());
                    return graphRequest.executeAndWait();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .doOnSubscribe(() -> isInProgress = true)
                .doOnTerminate(() -> isInProgress = false)
                .subscribe(response -> {
                    FacebookRequestError responseError = response.getError();
                    boolean resultOK = responseError == null;
                    AppLogger.w("创建 Facebook 直播返回的结果为:" + response.toString());
                    if (!resultOK) {
                        AppLogger.w("创建 Facebook 失败了");
                        mView.onRtmpAddressError();
                    } else {
                        try {
                            JSONObject object = response.getJSONObject();
                            String streamUrl = object.getString("stream_url");
                            String videoId = object.getString("id");//直播流 id 用来删除直播用
                            if (!TextUtils.isEmpty(videoId)) {
                                PreferencesUtils.putString(JConstant.FACEBOOK_PREF_VIDEO_ID + ":" + uuid, videoId);
                            }
                            if (!TextUtils.isEmpty(streamUrl)) {
                                startRtmpLive(streamUrl);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(MiscUtils.getErr(e));
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#startFacebookRtmpLive", subscribe);

    }

    private void stopFacebookRtmpLive() {
        Subscription subscribe = Observable.just("stopFacebookRtmpLive")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.newThread())
                .map(cmd -> {
                    String videoId = PreferencesUtils.getString(JConstant.FACEBOOK_PREF_VIDEO_ID + ":" + uuid, null);
                    if (!TextUtils.isEmpty(videoId)) {
                        AccessToken accessToken = AccessToken.getCurrentAccessToken();
                        Bundle params = new Bundle();
                        params.putBoolean("end_live_video", true);
                        GraphRequest graphRequest = new GraphRequest(accessToken, videoId, params, HttpMethod.POST);
                        return graphRequest.executeAndWait();
                    }
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .subscribe(response -> stopRtmpLive(), e -> {
                    AppLogger.e(MiscUtils.getErr(e));
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#stopFacebookRtmpLive", subscribe);
    }

    private String getPlatformString(Context context, int livePlatform) {
        switch (livePlatform) {
            case 0:
                return context.getString(R.string.LIVE_PLATFORM_FACEBOOK);
            case 1:
                return context.getString(R.string.LIVE_PLATFORM_YOUTUBE);
            case 2:
                return context.getString(R.string.LIVE_PLATFORM_WEIBO);
            case 3:
                return "RTMP";
        }
        return "RTMP";
    }

    @Override
    public void cameraLiveRtmpCtrl(int livePlatform, int enable) {
        Context context = ContextUtils.getContext();
        if (enable == 1) {
            mView.showBottomPanelInformation(context.getString(R.string.LIVE_CREATING, getPlatformString(context, livePlatform)), false);
        }
        switch (livePlatform) {
            case 0://facebook
            {
                if (!FacebookSdk.isInitialized()) {
                    FacebookSdk.sdkInitialize(mView.getAppContext(), () -> {
                        if (enable == 1) {
                            startFacebookRtmpLive();
                        } else if (enable == 0) {
                            stopFacebookRtmpLive();
                        }
                    });
                } else {
                    if (enable == 1) {
                        startFacebookRtmpLive();
                    } else if (enable == 0) {
                        stopFacebookRtmpLive();
                    }
                }
            }
            break;
            case 1://youtube
            {
                if (enable == 1) {
                    String youtube = PreferencesUtils.getString(JConstant.YOUTUBE_PREF_CONFIGURE + ":" + uuid, null);
                    try {
                        EventData eventData = JacksonFactory.getDefaultInstance().fromString(youtube, EventData.class);
                        String rtmpAddress = eventData.getIngestionAddress();
                        if (TextUtils.isEmpty(rtmpAddress)) {
                            mView.showRtmpLiveSetting();
                        } else {
                            startYoutubeLiveRtmp(rtmpAddress);
                        }
                    } catch (Exception e) {
                        AppLogger.e(MiscUtils.getErr(e));
                    }
                } else if (enable == 0) {
                    stopYoutubeLiveRtmp();
                }
            }
            break;
            case 2://weibo
            {
                if (enable == 1) {
                    startWeiboLiveRtmp();
                } else if (enable == 0) {
                    stopWeiboLiveRtmp();
                }
            }
            break;
            case 3://rtmp
            {
                if (enable == 1) {
                    String url = PreferencesUtils.getString(JConstant.RTMP_PREF_CONFIGURE + ":" + uuid, null);
                    AppLogger.w("rtmp 播放的地址为:" + url);
                    if (TextUtils.isEmpty(url) || !url.startsWith("rtmp://")) {
                        AppLogger.w("非法的 rtmp 地址");
                    } else {
                        startRtmpLive(url);
                    }
                } else if (enable == 0) {
                    stopRtmpLive();
                }

            }
            break;
        }
    }

    @Override
    public void clean() {
    }

    @Override
    public int getBattery() {
        if (battery == -1) {
            Device device = DataSourceManager.getInstance().getDevice(uuid);
            battery = device.$(DpMsgMap.ID_206_BATTERY, 0);
        }
        return battery;
    }

    @Override
    public void onViewAttached(PanoramaCameraContact.View view) {
        super.onViewAttached(view);
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());
        hasSDCard = status.hasSdcard;
    }

    @Override
    public void onStart() {
        super.onStart();
        BaseDeviceInformationFetcher.getInstance().init(uuid);
        DataSourceManager.getInstance().syncAllProperty(uuid);
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
    }


    @Override
    protected boolean disconnectBeforePlay() {
        return true;
    }

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
//        registerSubscription(getApiMonitorSub());
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#newVersionRspSub", newVersionRspSub());
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#getReportMsgSub", getReportMsgSub());
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#getNetWorkMonitorSub", getNetWorkMonitorSub());
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#getDeviceRecordStateSub", getDeviceRecordStateSub());
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#makeNewMsgSub", makeNewMsgSub());
    }

    private Subscription getDeviceRecordStateSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceRecordStateChanged.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    AppLogger.w("设备录像状态发生了变化");
                    PanoramaEvent.MsgVideoStatusRsp deviceState = (PanoramaEvent.MsgVideoStatusRsp) sourceManager.getDeviceState(uuid);
                    if (deviceState != null && deviceState.ret == 0 && deviceState.videoType == 2) {//只处理长路像的情况
                        if (!isRecording) {
                            mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO, false, true);
                            if (deviceState.videoType != 3) {
                                isRecording = true;
                                mView.onRefreshVideoRecordUI(deviceState.seconds, deviceState.videoType);
                                refreshVideoRecordUI(deviceState.seconds, deviceState.videoType);
                            }
                        }
                        AppLogger.w("有录像状态:" + new Gson().toJson(deviceState));
                    } else if (deviceState == null) {
//                        if (shouldRefreshRecord) {
//                            shouldRefreshRecord = false;
                        AppLogger.w("无录像状态:" + new Gson().toJson(deviceState));
                        if (isRecording) {
                            RxBus.getCacheInstance().post(PanoramaCameraContact.View.RecordFinishEvent.INSTANCE);
                            mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO, getLiveAction().hasResolution, false);
                        }
//                        mView.onRefreshControllerViewVisible(true);
//                        }
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
    }

    private Subscription getNetWorkMonitorSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    AppLogger.w("监听到网络状态发生变化");
                    BaseDeviceInformationFetcher.getInstance().init(uuid);
                    if (event.mobile != null && event.mobile.isConnected()) {
                        mView.onRefreshConnectionMode(event.mobile.getType());
                    } else if (event.wifi != null && event.wifi.isConnected()) {
                        mView.onRefreshConnectionMode(event.wifi.getType());
                    } else {
                        liveStreamAction.reset();
                        mView.onRefreshConnectionMode(-1);
                    }
                }, e -> {
                });
    }

    private Subscription newVersionRspSub() {
        Subscription subscription = RxBus.getCacheInstance().toObservable(AbstractVersion.BinVersion.class)
                .subscribeOn(Schedulers.io())
                .subscribe(version -> {
                    version.setLastShowTime(System.currentTimeMillis());
                    PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(version));
                    mView.onNewFirmwareRsp();
                    //必须手动断开,因为rxBus订阅不会断开
                    throw new RxEvent.HelperBreaker(version);
                }, AppLogger::e);
        AbstractVersion<PanDeviceVersionChecker.BinVersion> version = new PanDeviceVersionChecker();
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        version.setPortrait(new AbstractVersion.Portrait().setCid(uuid).setPid(device.pid));
        version.setShowCondition(() -> {
            Device d = BaseApplication.getAppComponent().getSourceManager().
                    getDevice(uuid);
            DpMsgDefine.DPNet dpNet = d.$(201, new DpMsgDefine.DPNet());
            //设备离线就不需要弹出来
            if (!JFGRules.isDeviceOnline(dpNet)) {
                return false;
            }
            //局域网弹出
            if (!MiscUtils.isDeviceInWLAN(uuid)) return false;
            return true;
        });
        version.startCheck();
        return subscription;
    }

    private Subscription getReportMsgSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter(msg -> TextUtils.equals(msg.uuid, uuid))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    AppLogger.w("收到设备同步消息:" + new Gson().toJson(result));
                    try {
                        for (JFGDPMsg msg : result.dpList) {
                            //屏蔽掉204 消息
                            if (msg.id == 222) {//? 204 或者 222?
                                DpMsgDefine.DPSdcardSummary sdcardSummary = null;
                                try {
                                    sdcardSummary = unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
                                } catch (Exception e) {
//                                    DpMsgDefine.DPSdStatusInt statusInt = unpackData(msg.packValue, DpMsgDefine.DPSdStatusInt.class);
//                                    status = new DpMsgDefine.DPSdStatus();
//                                    status.total = statusInt.total;
//                                    status.used = statusInt.used;
//                                    status.err = statusInt.err;
//                                    status.hasSdcard = statusInt.hasSdcard == 1;
                                }
                                AppLogger.w("204:" + new Gson().toJson(sdcardSummary));
                                if (sdcardSummary != null && !sdcardSummary.hasSdcard && hasSDCard) {//SDCard 不存在
                                    mView.onReportDeviceError(2004, true);
                                } else if (sdcardSummary != null && sdcardSummary.errCode != 0) {//SDCard 需要格式化
//                                    mView.onReportDeviceError(2022, true);//只有SD 卡不存在才弹
                                }
                                hasSDCard = sdcardSummary != null && sdcardSummary.hasSdcard;
//                                shouldRefreshRecord = status != null && status.hasSdcard && status.err == 0;
                            } else if (msg.id == 204) {
                                // TODO: 2017/8/17 AP 模式下发的是204 消息,需要特殊处理
                                Device device = DataSourceManager.getInstance().getDevice(uuid);
//                                if (JFGRules.isAPDirect(uuid, device.$(202, ""))) {
                                DpMsgDefine.DPSdStatus status = unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);
                                if (status != null && !status.hasSdcard && hasSDCard) {//SDCard 不存在
                                    mView.onReportDeviceError(2004, true);
                                } else if (status != null && status.err != 0) {//SDCard 需要格式化
//                                    mView.onReportDeviceError(2022, true);
                                }
                                hasSDCard = status != null && status.hasSdcard;
//                                }
                            } else if (msg.id == 205) {
                                charge = unpackData(msg.packValue, boolean.class);
                                if (charge) {
                                    mView.onDeviceBatteryChanged(-1);
                                } else {
                                    mView.onDeviceBatteryChanged(battery);
                                }
                                AppLogger.w("charge:" + charge);
                            } else if (msg.id == 206) {
                                Integer battery = unpackData(msg.packValue, int.class);
                                if (battery != null) {
                                    mView.onDeviceBatteryChanged(this.battery = battery);
                                }
                                if (this.battery <= 20 && notifyBatteryLow) {
                                    mView.onBellBatteryDrainOut();
                                    notifyBatteryLow = false;
                                } else if (this.battery > 20) {
                                    notifyBatteryLow = true;
                                }
                                AppLogger.w("battery:" + battery);
                            } else if (msg.id == 201) {
                                DpMsgDefine.DPNet dpNet = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPNet.class);

                                if (dpNet != null && dpNet.net > 0) {
                                    mView.onDeviceOnLine();
                                }
                            } else if (msg.id == 517) {
                                DpMsgDefine.DPCameraLiveRtmpStatus dpCameraLiveRtmpStatus = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPCameraLiveRtmpStatus.class);
                                if (dpCameraLiveRtmpStatus != null) {
                                    AppLogger.w("收到 rtmp 消息,url is: " + dpCameraLiveRtmpStatus.url
                                            + ",liveType is:" + dpCameraLiveRtmpStatus.liveType
                                            + ",flag is:" + dpCameraLiveRtmpStatus.flag
                                            + ",timestamp is:" + dpCameraLiveRtmpStatus.timestamp
                                            + ",error is:" + dpCameraLiveRtmpStatus.error
                                    );
                                    if (!isRtmpLive) {
                                        mView.onRtmpQueryResponse(dpCameraLiveRtmpStatus);
                                    }
                                    if (dpCameraLiveRtmpStatus.error != 0) {
                                        // TODO: 2017/9/9 出错了
                                        isRtmpLive = false;
                                    } else if (dpCameraLiveRtmpStatus.flag != 2) {
                                        // TODO: 2017/9/9 直播还未开始
                                        isRtmpLive = false;
                                    } else if (dpCameraLiveRtmpStatus.timestamp != 0 && !isRtmpLive) {
                                        isRtmpLive = true;
                                        refreshVideoRecordUI((int) ((System.currentTimeMillis() / 1000L) - dpCameraLiveRtmpStatus.timestamp), PanoramaCameraContact.View.PANORAMA_RECORD_MODE.MODE_LIVE);
                                    }

                                }
                            }
                        }
                    } catch (Exception e) {
                        AppLogger.e(MiscUtils.getErr(e));
                    }
                }, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
                });
    }

    @Override
    public void makePhotograph() {
        mView.onRefreshControllerViewVisible(false);
        Subscription subscribe = BasePanoramaApiHelper.getInstance().snapShot(uuid)
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msgFileRsp -> {
                    if (msgFileRsp.ret == 0) {
                        mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_PICTURE, getLiveAction().hasResolution, false);
                        mView.onRefreshControllerView(getLiveAction().hasResolution, false);
                        mView.onRefreshControllerViewVisible(true);
                        if (msgFileRsp.files != null && msgFileRsp.files.size() > 0) {
                            mView.onShowPreviewPicture(null);
                            if (BasePanoramaApiHelper.getInstance().getDeviceIp() == null) {
                                mView.onReportDeviceError(ERROR_CODE_HTTP_NOT_AVAILABLE, true);
                            }
                        }
                    } else {
                        if (msgFileRsp.ret == 2004) {
                            hasSDCard = false;
                        }
                        mView.onReportDeviceError(msgFileRsp.ret, false);
                    }
                    AppLogger.w("拍照返回结果为:" + new Gson().toJson(msgFileRsp));
                }, e -> {
                    AppLogger.e(e);
                    mView.onReportDeviceError(-1, false);//timeout
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#makePhotograph", subscribe);
    }

    @Override
    protected boolean shouldShowPreview() {
        return false;
    }

    @Override
    public void checkAndInitRecord() {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().getUpgradeStatus(uuid)
                .onErrorResumeNext(Observable.just(null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> {
                    boolean isUpgrade = ret != null && ret.upgradeStatus == 1;
                    if (isUpgrade) {
                        cancelViewer();
                        mView.onVideoDisconnect(-3);
                    }
                    return !isUpgrade;
                })
                .observeOn(Schedulers.io())
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getRecStatus(uuid).onErrorResumeNext(Observable.just(null)))
                .observeOn(AndroidSchedulers.mainThread())
                .map(rsp -> {
                    if (rsp != null && rsp.ret == 0 && rsp.videoType != 3) {//检查录像状态
//                        if (!shouldRefreshRecord) {
//                            shouldRefreshRecord = true;
//                        mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO, false, true);
                        refreshVideoRecordUI(rsp.seconds, rsp.videoType);
//                            DataSourceManager.getInstance().pushDeviceState(uuid, rsp);
//                        }
                    }
                    AppLogger.w("初始化录像状态结果为:" + new Gson().toJson(rsp));
                    return rsp;
                })
                .observeOn(Schedulers.io())
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getResolution(uuid).onErrorResumeNext(Observable.just(null)))
                .observeOn(AndroidSchedulers.mainThread())
                .map(ret -> {
                    if (ret != null && ret.ret == 0) {
                        mView.onSwitchSpeedMode(ret.resolution);//检查分辨率
                    }
                    return ret;
                })
                .observeOn(Schedulers.io())
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getPowerLine(uuid).onErrorResumeNext(Observable.just(null)))
                .observeOn(AndroidSchedulers.mainThread())
                .map(ret -> {
                    if (ret != null && ret.powerline == 1) {
                        charge = true;
                        mView.onDeviceBatteryChanged(-1);
                    } else {
                        charge = false;
                    }
                    return ret;
                })
                .observeOn(Schedulers.io())
                .flatMap(ret -> BasePanoramaApiHelper.getInstance().getBattery(uuid).onErrorResumeNext(Observable.just(null)).observeOn(AndroidSchedulers.mainThread())
                        .map(bat -> {
                            if (bat != null) {
                                this.battery = bat.battery;
                                Device device = sourceManager.getDevice(uuid);
                                DPEntity property = device.getProperty(206);
                                if (property == null) {
                                    property = device.getEmptyProperty(206);
                                }
                                property.setValue(new DpMsgDefine.DPPrimary<>(this.battery), pack(this.battery), property.getVersion());
                                if (bat.battery <= 20 && isFirst) {//检查电量
                                    isFirst = false;
                                    DBOption.DeviceOption option = device.option(DBOption.DeviceOption.class);
                                    if (option != null && option.lastLowBatteryTime < TimeUtils.getTodayStartTime()) {//新的一天
                                        option.lastLowBatteryTime = System.currentTimeMillis();
                                        device.setOption(option);
                                        sourceManager.updateDevice(device);
                                        mView.onBellBatteryDrainOut();
                                    }
                                }
                                if (ret != null && ret.powerline != 1) {
                                    mView.onDeviceBatteryChanged(this.battery);
                                }

                            }
                            return bat;
                        })

                )
                .flatMap(ret -> Observable.just("517")
                        .observeOn(Schedulers.io())
                        .map(cmd -> {
                            try {
                                ArrayList<JFGDPMsg> params = new ArrayList<>();
                                params.add(new JFGDPMsg(517, 0, new byte[]{0}));
                                return BaseApplication.getAppComponent().getCmd().robotGetData(uuid, params, 1, false, 0);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                            return -1L;
                        })
                        .flatMap(seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class).first(rsp -> rsp.seq == seq))
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(rsp -> {
                            if (rsp != null && rsp.map != null && rsp.map.size() > 0) {
                                ArrayList<JFGDPMsg> jfgdpMsgs = rsp.map.get(517);
                                if (jfgdpMsgs != null && jfgdpMsgs.size() > 0) {
                                    JFGDPMsg msg = jfgdpMsgs.get(0);
                                    try {
                                        DpMsgDefine.DPCameraLiveRtmpStatus unpackData = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPCameraLiveRtmpStatus.class);
                                        if (unpackData != null) {
                                            mView.onRtmpQueryResponse(unpackData);
                                            if (unpackData.error == 0 && unpackData.flag == 2 && !isRtmpLive) {
                                                isRtmpLive = true;
                                                refreshVideoRecordUI((int) ((System.currentTimeMillis() / 1000) - unpackData.timestamp), PanoramaCameraContact.View.PANORAMA_RECORD_MODE.MODE_LIVE);
                                            }
                                        }
                                        AppLogger.w("517 消息为:" + new Gson().toJson(unpackData));
                                    } catch (IOException e) {
                                        AppLogger.e(MiscUtils.getErr(e));
                                    }

                                }
                            }
                            return rsp;
                        }).onErrorResumeNext(Observable.just(null)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    mView.onDeviceInitFinish();//初始化成功,可以播放视频了
                }, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
                });

        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#checkAndInitRecord", subscribe);
    }

    @Override
    public void switchVideoResolution(@PanoramaCameraContact.View.SPEED_MODE int mode) {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().setResolution(uuid, mode)
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    AppLogger.w("切换模式返回结果为" + new Gson().toJson(ret));
                    if (ret.ret == 0) {
                        mView.onSwitchSpeedMode(mode);
                    } else {
                        AppLogger.w("切换模式失败了");
                    }
                }, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#switchVideoResolution", subscribe);
    }

    @Override
    public void startVideoRecord(int type) {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().startRec(uuid, type)
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> mView.onRefreshControllerView(false, false))
                .subscribe(rsp -> {
                    AppLogger.w("开启视频录制返回结果为" + new Gson().toJson(rsp));
                    if (rsp.ret == 0) {
                        AppLogger.w("开启视频录制成功了");
//                        if (!shouldRefreshRecord) {
//                            shouldRefreshRecord = true;

                        refreshVideoRecordUI(0, type);
//                            DataSourceManager.getInstance().pushDeviceState(uuid, msgVideoStatusRsp);
//                        }
                    } else {
//                        shouldRefreshRecord = false;
                        if (rsp.ret == 2004) {
                            hasSDCard = false;
                        }
                        mView.onReportDeviceError(rsp.ret, false);
                    }
                }, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
//                    shouldRefreshRecord = false;
                    mView.onReportDeviceError(-1, false);
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#startVideoRecord", subscribe);
    }

    @Override
    public void stopVideoRecord(int type) {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().stopRec(uuid, type)
                .timeout(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> RxBus.getCacheInstance().post(PanoramaCameraContact.View.RecordFinishEvent.INSTANCE))
                .doOnTerminate(() -> mView.onRefreshViewModeUI(PanoramaCameraContact.View.PANORAMA_VIEW_MODE.MODE_VIDEO, getLiveAction().hasResolution, false))
                .subscribe(ret -> {
                    if (ret.ret == 0 && ret.files != null && ret.files.size() > 0) {//成功了
                        mView.onShowPreviewPicture(null);
                        if (BasePanoramaApiHelper.getInstance().getDeviceIp() == null) {
                            mView.onReportDeviceError(ERROR_CODE_HTTP_NOT_AVAILABLE, true);
                        }
                    } else {//失败了
                        if (ret.ret == 2004) {
                            hasSDCard = false;
                        }
                        mView.onReportDeviceError(ret.ret, false);
                    }
                    AppLogger.d("停止直播返回结果为:" + new Gson().toJson(ret));
                }, e -> {
                    mView.onReportDeviceError(-1, false);
                    AppLogger.e(MiscUtils.getErr(e));
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#stopVideoRecord", subscribe);
    }

    @Override
    public void formatSDCard() {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().sdFormat(uuid)
                .timeout(120, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (ret != null && ret.sdIsExist && ret.sdcard_recogntion == 0) {
                        mView.onSDFormatResult(1);
                    } else {
                        mView.onSDFormatResult(-1);
                    }
                }, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
                    mView.onSDFormatResult(-1);
                });
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#formatSDCard", subscribe);
    }

    public void refreshVideoRecordUI(int offset, @PanoramaCameraContact.View.PANORAMA_RECORD_MODE int type) {
        Subscription subscribe = Observable.interval(0, 500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(count -> (int) (count / 2) + offset)
                .takeUntil(RxBus.getCacheInstance().toObservable(PanoramaCameraContact.View.RecordFinishEvent.class))
                .doOnSubscribe(() -> RxBus.getCacheInstance().post(PanoramaCameraContact.View.RecordFinishEvent.INSTANCE))
                .doOnTerminate(() -> {
                    isRecording = false;
                    isRtmpLive = false;
                })
                .subscribe(second -> {
                    if (type == PanoramaCameraContact.View.PANORAMA_RECORD_MODE.MODE_LIVE) {
                        isRtmpLive = true;
                    } else {
                        isRecording = true;
                    }
                    if (!isInProgress) {
                        mView.onRefreshVideoRecordUI(second, type);
                    }
                }, AppLogger::e);
        registerSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "PanoramaPresenter#refreshVideoRecordUI", subscribe);
    }

    private Subscription makeNewMsgSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .subscribeOn(Schedulers.io())
                .flatMap(ret -> Observable.from(ret.dpList))
                .filter(ret -> filterNewMsgId(ret.id))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    mView.onShowNewMsgHint();
                }, AppLogger::e);
    }

    private boolean filterNewMsgId(long id) {
        return id == 505 || id == 222 || id == 512;
    }

}
