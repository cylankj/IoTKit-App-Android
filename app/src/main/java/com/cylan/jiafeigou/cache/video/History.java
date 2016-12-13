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
import rx.functions.Func1;
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
                .map(new Func1<JFGHistoryVideo, ArrayList<JFGVideo>>() {
                    @Override
                    public ArrayList<JFGVideo> call(JFGHistoryVideo jfgHistoryVideo) {
                        return jfgHistoryVideo.list;
                    }
                })
                .map(new Func1<ArrayList<JFGVideo>, Object>() {
                    @Override
                    public Object call(ArrayList<JFGVideo> list) {
                        synchronized (object) {
                            if (dataList == null)
                                dataList = new ArrayList<>();
                            dataList.addAll(list);
                            dataList = new ArrayList<>(new HashSet<>(dataList));
                            Collections.sort(dataList);
                        }
                        return null;
                    }
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
                .map(new Func1<RxEvent.JFGHistoryVideoReq, Object>() {
                    @Override
                    public Object call(RxEvent.JFGHistoryVideoReq jfgHistoryVideoReq) {
                        try {
                            JfgCmdInsurance.getCmd().getVideoList(jfgHistoryVideoReq.uuid);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                        AppLogger.i(IHistory + jfgHistoryVideoReq.uuid);
                        return null;
                    }
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
