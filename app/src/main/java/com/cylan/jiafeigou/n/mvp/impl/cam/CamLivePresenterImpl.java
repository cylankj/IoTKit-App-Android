package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.Converter;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.utils.NetUtils;
import com.cylan.utils.RandomUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLivePresenterImpl extends AbstractPresenter<CamLiveContract.View> implements CamLiveContract.Presenter {
    private static final String TAG = "CamLivePresenterImpl";
    private DeviceBean bean;
    private BeanCamInfo beanCamInfo;
    private CompositeSubscription compositeSubscription;
    private boolean isRtcpSignal;
    private int playType = CamLiveContract.TYPE_LIVE;
    /**
     * 帧率记录
     */
    private List<Integer> frameRateList = new ArrayList<>();

    public CamLivePresenterImpl(CamLiveContract.View view, DeviceBean bean) {
        super(view);
        view.setPresenter(this);
        this.bean = bean;
        this.beanCamInfo = Converter.convert(bean);
    }

    /**
     * Rtcp和resolution的回调,
     * 只有resolution回调之后,才能设置{@link JfgAppCmd#setRenderLocalView(View)}
     * 正常播放回调
     * 10s没有视频,直接断开
     *
     * @return
     */
    private Subscription rtcpNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .filter(new Func1<JFGMsgVideoRtcp, Boolean>() {
                    @Override
                    public Boolean call(JFGMsgVideoRtcp rtcp) {
                        return getView() != null && isRtcpSignal;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<JFGMsgVideoRtcp, JFGMsgVideoRtcp>() {
                    @Override
                    public JFGMsgVideoRtcp call(JFGMsgVideoRtcp rtcp) {
                        frameRateList.add(rtcp.frameRate);
                        if (frameRateList.size() == 11) {
                            frameRateList.remove(0);//移除最前沿的一个
                            boolean isBad = MiscUtils.isBad(frameRateList, 2, 10);
                            if (isBad) {
                                frameRateList.clear();
                                AppLogger.e("is bad net work");
                                getView().onLiveStop(playType,
                                        JFGRules.PlayErr.ERR_LOW_FRAME_RATE);
                                //暂停播放
                                stopPlayVideo(playType);
                            }
                        }
                        return rtcp;
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGMsgVideoRtcp>() {
                    @Override
                    public void call(JFGMsgVideoRtcp rtcp) {
                        getView().onRtcp(rtcp);
                        Log.d(TAG, "rtcp: " + new Gson().toJson(rtcp));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("rtcp err: " + throwable.getLocalizedMessage());
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        AppLogger.d("what...complete?");
                    }
                });
    }

    /**
     * 分辨率回调
     *
     * @return
     */
    private Subscription resolutionNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                .filter(new Func1<JFGMsgVideoResolution, Boolean>() {
                    @Override
                    public Boolean call(JFGMsgVideoResolution jfgMsgVideoResolution) {
                        boolean filter = getCamInfo() != null
                                && getCamInfo().deviceBase != null
                                && TextUtils.equals(getCamInfo().deviceBase.uuid, jfgMsgVideoResolution.peer)
                                && getView() != null;
                        if (!filter) {
                            AppLogger.e("getView(): " + (getView() != null));
                            AppLogger.e("this peer is out date: " + jfgMsgVideoResolution.peer);
                        }
                        return filter;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGMsgVideoResolution>() {
                    @Override
                    public void call(JFGMsgVideoResolution resolution) {
                        isRtcpSignal = true;
                        getView().onResolution(resolution);
                        getView().onLiveStarted(playType);
                        AppLogger.i("ResolutionNotifySub: " + new Gson().toJson(resolution));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("resolution err: " + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 视频断开连接
     *
     * @return
     */
    private Subscription videoDisconnectSub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                .subscribeOn(Schedulers.newThread())
                .filter(new Func1<JFGMsgVideoDisconn, Boolean>() {
                    @Override
                    public Boolean call(JFGMsgVideoDisconn jfgMsgVideoDisconn) {
                        boolean notNull = getView() != null
                                && getCamInfo().deviceBase != null
                                && TextUtils.equals(getCamInfo().deviceBase.uuid, jfgMsgVideoDisconn.remote);
                        if (!notNull) {
                            AppLogger.e("err: " + getCamInfo());
                        }
                        return notNull;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGMsgVideoDisconn>() {
                    @Override
                    public void call(JFGMsgVideoDisconn jfgMsgVideoDisconn) {
                        getView().onLiveStop(playType, jfgMsgVideoDisconn.code);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("videoDisconnectSub:" + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public int getPlayState() {
        return 0;
    }

    @Override
    public int getPlayType() {
        return playType;
    }

    @Override
    public void fetchHistoryDataList() {
        Observable.just(null)
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object dataStack) {
                        //获取设备历史录像
                        if (getCamInfo().deviceBase != null && getCamInfo().deviceBase.uuid != null) {
                            RxEvent.JFGHistoryVideoReq req = new RxEvent.JFGHistoryVideoReq();
                            req.uuid = getCamInfo().deviceBase.uuid;
                            RxBus.getCacheInstance().post(req);
                            //不直接使用这个接口,因为在videoList的数据结构中没有uuid标签,只能使用请求的seq来判断.
                            //所有把它统一放到History类中管理.
                            //JfgCmdInsurance.getCmd().getVideoList(getCamInfo().deviceBase.uuid);
                            AppLogger.i("getVideoList");
                        }
                    }
                });
    }

    @Override
    public void startPlayVideo(int type) {
        getView().onLivePrepare(type);
        Observable.just(getCamInfo().deviceBase.uuid)
                .subscribeOn(Schedulers.newThread())
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        //判断网络状况
                        final int net = NetUtils.getJfgNetType(getView().getContext());
                        AppLogger.i("play env state: " + net + " " + s);
                        if (net == 0) {
                            getView().onLiveStop(playType, JFGRules.PlayErr.ERR_NERWORK);
                            return false;
                        }
                        return !TextUtils.isEmpty(s);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        JfgCmdInsurance.getCmd().playVideo(s);
                        AppLogger.i("play video");
                    }
                });
    }

    @Override
    public void stopPlayVideo(int type) {
        Observable.just(getCamInfo().deviceBase.uuid)
                .subscribeOn(Schedulers.newThread())
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        //判断网络状况
                        AppLogger.i("stop play env state:" + s);
                        return !TextUtils.isEmpty(s);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        JfgCmdInsurance.getCmd().stopPlay(s);
                    }
                });
    }

    public Subscription fetchCamInfo() {
        //查询设备列表
        return RxBus.getUiInstance().toObservableSticky(RxUiEvent.BulkDeviceList.class)
                .subscribeOn(Schedulers.computation())
                .filter(new Func1<RxUiEvent.BulkDeviceList, Boolean>() {
                    @Override
                    public Boolean call(RxUiEvent.BulkDeviceList list) {
                        return getView() != null && list != null && list.allDevices != null
                                && beanCamInfo != null && beanCamInfo.deviceBase != null;
                    }
                })
                .flatMap(new Func1<RxUiEvent.BulkDeviceList, Observable<DpMsgDefine.DpWrap>>() {
                    @Override
                    public Observable<DpMsgDefine.DpWrap> call(RxUiEvent.BulkDeviceList list) {
                        for (DpMsgDefine.DpWrap wrap : list.allDevices) {
                            if (wrap.baseDpDevice != null
                                    && TextUtils.equals(wrap.baseDpDevice.uuid, beanCamInfo.deviceBase.uuid)) {
                                return Observable.just(wrap);
                            }
                        }
                        return null;
                    }
                })
                .filter(new Func1<DpMsgDefine.DpWrap, Boolean>() {
                    @Override
                    public Boolean call(DpMsgDefine.DpWrap dpWrap) {
                        return dpWrap != null && dpWrap.baseDpDevice != null;
                    }
                })
                .map(new Func1<DpMsgDefine.DpWrap, BeanCamInfo>() {
                    @Override
                    public BeanCamInfo call(DpMsgDefine.DpWrap dpWrap) {
                        BeanCamInfo info = new BeanCamInfo();
                        info.convert(dpWrap.baseDpDevice, dpWrap.baseDpMsgList);
                        someFlagUpdate(info);
                        beanCamInfo = info;
                        AppLogger.i("BeanCamInfo: " + new Gson().toJson(info));
                        return info;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new RxHelper.Filter<BeanCamInfo>("", getView() != null))
                .subscribe(new Action1<BeanCamInfo>() {
                    @Override
                    public void call(BeanCamInfo camInfoBean) {
                        //刷新 //如果设备变成待机模式
                        getView().onDeviceStandBy(camInfoBean.cameraStandbyFlag);
                        AppLogger.i("onDeviceStandBy: ");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("fetchCamInfo:" + throwable.getLocalizedMessage());
                    }
                });
//        RxBus.getCacheInstance().post(new RxUiEvent.QueryBulkDevice());
    }

    private void someFlagUpdate(BeanCamInfo newInfo) {
        //standby flag
        if (beanCamInfo != null && newInfo != null) {
            Observable.just(newInfo.cameraStandbyFlag)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            getView().onDeviceStandBy(aBoolean);
                        }
                    });
        }
    }

    @Override
    public BeanCamInfo getCamInfo() {
        if (this.beanCamInfo == null)
            this.beanCamInfo = Converter.convert(this.bean);
        return beanCamInfo;
    }

    @Override
    public void start() {
        unSubscribe(compositeSubscription);
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(rtcpNotifySub());
        compositeSubscription.add(resolutionNotifySub());
        compositeSubscription.add(videoDisconnectSub());
        compositeSubscription.add(robotDataSync());
        compositeSubscription.add(fetchCamInfo());
    }

    private Subscription robotDataSync() {
        return RxBus.getCacheInstance().toObservable(RxEvent.JFGRobotSyncData.class)
                .filter(new Func1<RxEvent.JFGRobotSyncData, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.JFGRobotSyncData jfgRobotSyncData) {
                        return getView() != null && beanCamInfo != null &&
                                TextUtils.equals(beanCamInfo.deviceBase.uuid,
                                        jfgRobotSyncData.identity);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<RxEvent.JFGRobotSyncData, Object>() {
                    @Override
                    public Object call(RxEvent.JFGRobotSyncData jfgRobotSyncData) {
                        beanCamInfo.cameraStandbyFlag = RandomUtils.getRandom(10) % 2 == 0;
                        getView().onDeviceStandBy(beanCamInfo.cameraStandbyFlag);
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("robotDataSync"))
                .subscribe();
    }

    @Override
    public void stop() {
        frameRateList.clear();
        unSubscribe(compositeSubscription);
        compositeSubscription = null;
    }
}
