package com.cylan.jiafeigou.cache.video;

import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * 历史录像数据管理中心
 * Created by cylan-hunt on 16-12-6.
 */

public class History implements IHistory {

    private volatile static History history;
    /**
     * 数据集,不实现Lru逻辑
     */
    private ArrayList<JFGVideo> dataList;
    private final Object object = new Object();

    public static History getHistory() {
        if (history == null)
            history = new History();
        return history;
    }

    private History() {

    }

    /**
     * 历史数据从dataSource传过来.
     *
     * @return
     */
    private Subscription onDataList() {
        return RxBus.getCacheInstance().toObservable(JFGHistoryVideo.class)
                .subscribeOn(Schedulers.computation())
                .map((JFGHistoryVideo jfgHistoryVideo) -> (jfgHistoryVideo.list))
                .map((ArrayList<JFGVideo> list) -> {
                    synchronized (object) {
                        if (dataList == null)
                            dataList = new ArrayList<>();
                        dataList.addAll(list);
                        dataList = new ArrayList<>(new HashSet<>(dataList));
                        Collections.sort(dataList);
                        AppLogger.i(String.format(IHistory, dataList.size()));
                    }
                    return null;
                })
                .retry(new RxHelper.ExceptionFun<>("onDataList"))
                .subscribe();
    }

    /**
     * 查询历史数据
     *
     * @return
     */
    private Subscription onQueryDataList() {
        return RxBus.getCacheInstance().toObservable(RxEvent.JFGHistoryVideoReq.class)
                .subscribeOn(Schedulers.newThread())
                .map((RxEvent.JFGHistoryVideoReq jfgHistoryVideoReq) -> {
                    try {
                        JfgCmdInsurance.getCmd().getVideoList(jfgHistoryVideoReq.uuid);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    AppLogger.i(String.format(IHistory, jfgHistoryVideoReq.uuid));
                    return null;
                })
                .retry(new RxHelper.ExceptionFun<>("onQueryDataList"))
                .subscribe();
    }

    @Override
    public void clear() {
        if (dataList != null)
            dataList.clear();
        history = null;
    }

    @Override
    public Subscription[] register() {
        return new Subscription[]{onDataList(), onQueryDataList()};
    }

}
