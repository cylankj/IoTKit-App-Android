package com.cylan.jiafeigou.misc;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * @author hds
 * @date 17-10-20
 */

public class VisitorLoader {


    private static long getMaxTimeFromList(DpMsgDefine.VisitorList visitorList) {
        if (visitorList.total == -1) {
            return 0;
        }
        final int count = ListUtils.getSize(visitorList.dataList);
        if (count < 1) {
            return 0;
        }
        return visitorList.dataList.get(count - 1).lastTime;
    }

    private static long getMaxTimeFromList(DpMsgDefine.StrangerVisitorList visitorList) {
        if (visitorList.total == -1) {
            return 0;
        }
        final int count = ListUtils.getSize(visitorList.strangerVisitors);
        if (count < 1) {
            return 0;
        }
        return visitorList.strangerVisitors.get(count - 1).lastTime;
    }

    public static Observable<byte[]> getDataByte(final String uuid, int msgType, long timeSec) {
        final String sessionId = Command.getInstance().getSessionId();
        AppLogger.d("sessionId:" + sessionId);
        try {
            DpMsgDefine.ReqContent reqContent = new DpMsgDefine.ReqContent();
            reqContent.uuid = uuid;
            reqContent.timeSec = timeSec;
            final long seq =  Command.getInstance().sendUniservalDataSeq(msgType, DpUtils.pack(reqContent));
            return RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp.class)
                    .filter(rsp -> rsp.seq == seq)
                    .map(ret -> {
                        AppLogger.d("收到了?" + ret.data);
                        return ret.data;
                    })
                    .subscribeOn(Schedulers.io());
        } catch (JfgException e) {
            e.printStackTrace();
        }
        return Observable.just(new byte[]{});
    }

    /**
     * 加载所有访客列表
     *
     * @return
     */
    public static Observable<DpMsgDefine.VisitorList> loadAllVisitorList(final String uuid) {
//        final DpMsgDefine.VisitorList visitorList = new DpMsgDefine.VisitorList();
//        visitorList.total = -1;
        return Observable.just("")
                .flatMap(s -> {
//                    final long timeSec = getMaxTimeFromList(visitorList);
                    return getDataByte(uuid, 5, 0)
                            .flatMap(bytes -> {
                                DpMsgDefine.VisitorList list = DpUtils.unpackDataWithoutThrow(bytes, DpMsgDefine.VisitorList.class, null);
                                AppLogger.d("收到数据？" + (BuildConfig.DEBUG ? list : null));
//                                if (list != null && list.total > 0) {
//                                    visitorList.total = list.total;
//                                    final int cnt = ListUtils.getSize(list.dataList);
//                                    if (visitorList.dataList == null) {
//                                        visitorList.dataList = new ArrayList<>();
//                                    }
//                                    if (cnt > 0) {
//                                        visitorList.dataList.addAll(list.dataList);
//                                    }
//                                    if (ListUtils.getSize(visitorList.dataList) != list.total) {
//                                        //还不是全部数据
//                                        throw new IllegalArgumentException("go_n_get");
//                                    } else {
//                                        return Observable.just(visitorList);
//                                    }
//                                }
                                return Observable.just(list);
                            });
                });
//                .retry((integer, throwable) -> {
//                    AppLogger.d("可能继续获取剩余的数据?" + throwable.getLocalizedMessage());
//                    return !TextUtils.isEmpty(throwable.getLocalizedMessage()) && throwable.getLocalizedMessage().contains("go_n_get");
//                });
    }

    public static Observable<DpMsgDefine.StrangerVisitorList>
    loadAllStrangerList(String uuid) {
//        final DpMsgDefine.StrangerVisitorList visitorList = new DpMsgDefine.StrangerVisitorList();
//        visitorList.total = -1;
        return Observable.just("")
                .flatMap(s -> {
//                    final long timeSec = getMaxTimeFromList(visitorList);
                    return getDataByte(uuid, 6, 0)
                            .flatMap(bytes -> {
                                DpMsgDefine.StrangerVisitorList list = DpUtils.unpackDataWithoutThrow(bytes, DpMsgDefine.StrangerVisitorList.class, null);
//                                if (list != null && list.total > 0) {
//                                    visitorList.total = list.total;
//                                    final int cnt = ListUtils.getSize(list.strangerVisitors);
//                                    if (visitorList.strangerVisitors == null) {
//                                        visitorList.strangerVisitors = new ArrayList<>();
//                                    }
//                                    if (cnt > 0) {
//                                        visitorList.strangerVisitors.addAll(list.strangerVisitors);
//                                    }
//                                    if (ListUtils.getSize(visitorList.strangerVisitors) != list.total) {
//                                        //还不是全部数据
//                                        throw new IllegalArgumentException("go_n_get");
//                                    } else {
//                                        return Observable.just(visitorList);
//                                    }
//                                }
                                return Observable.just(list);
                            });
                });
//                .retry((integer, throwable) -> {
//                    AppLogger.d("继续获取剩余的数据");
//                    return throwable.getLocalizedMessage().contains("go_n_get");
//                });
    }

}
