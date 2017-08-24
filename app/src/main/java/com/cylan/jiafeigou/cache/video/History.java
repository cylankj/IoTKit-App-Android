package com.cylan.jiafeigou.cache.video;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.wheel.ex.DataExt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 历史录像数据管理中心
 * Created by cylan-hunt on 16-12-6.
 */

public class History {

    private volatile static History history;
    /**
     * 保存历史录像的日历
     * uuid+day,Long
     */
    private HashMap<String, Long> dateMap = new HashMap<>();
    private final Object lock = new Object();

    /**
     * 数据集,不实现Lru逻辑
     */

    public static History getHistory() {
        if (history == null)
            history = new History();
        return history;
    }


    private History() {
        RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class)
                .subscribeOn(Schedulers.io())
                .map(setDataRsp -> {
                    int size = ListUtils.getSize(setDataRsp.rets);
                    for (int i = 0; i < size; i++) {
                        JFGDPMsgRet ret = setDataRsp.rets.get(i);
                        if (ret.id == 218 && ret.ret == 0) {
                            //格式化了sd卡.
                            clearHistoryFile(setDataRsp.uuid);
                        }
                    }
                    return null;
                })
                .retry()
                .subscribe();
    }

    /**
     * 非分享账号
     * 可以利用上 218这个消息,{@link com.cylan.jiafeigou.dp.DpMsgMap#ID_218_DEVICE_FORMAT_SDCARD}
     *
     * @param device
     * @return
     */
    public boolean queryHistory(Device device) {
        if (device == null || TextUtils.isEmpty(device.uuid)
                || !TextUtils.isEmpty(device.shareAccount)) {
            AppLogger.w("go back:" + device);
            return false;
        }
        try {
            BaseApplication.getAppComponent().getCmd().getVideoList(device.uuid);
            AppLogger.w("getVideoList");
            return true;
        } catch (JfgException e) {
            AppLogger.e("uuid is null: " + e.getLocalizedMessage());
            return false;
        }
    }

    public ArrayList<Long> getDateList(String uuid) {
        synchronized (lock) {
            if (TextUtils.isEmpty(uuid)) return null;
            ArrayList<Long> longs = new ArrayList<>();
            Iterator<String> key = dateMap.keySet().iterator();
            while (key.hasNext()) {
                String next = key.next();
                if (next.startsWith(uuid)) {
                    longs.add(dateMap.get(next));
                }
            }
            return longs;
        }
    }

    /**
     * 历史数据从dataSource传过来.
     * 1.新的数据列表过来后,先清空db中这段时间的记录,然后存db
     *
     * @return
     */

    public void cacheHistoryDataList(JFGHistoryVideo historyVideo) {
        Observable.just(historyVideo)
                .subscribeOn(Schedulers.io())
                .filter(ret -> {
                    if (ListUtils.isEmpty(ret.list)) {
                        RxBus.getCacheInstance().post(new RxEvent.HistoryBack(true));
                        //清空
                        return false;
                    }
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .flatMap(historyVideo1 -> {
                    //设备端已经排好序了.
                    long time = System.currentTimeMillis();
                    String uuid = historyVideo.list.get(0).peer;
                    ArrayList<Long> timeList = new ArrayList<>();
                    ArrayList<JFGVideo> list = historyVideo1.list;
                    ArrayList<HistoryFile> historyFiles = new ArrayList<>(ListUtils.getSize(list));
                    for (JFGVideo video : list) {
                        timeList.add(video.beginTime);
                        HistoryFile file = new HistoryFile();
                        file.duration = video.duration;
                        file.time = video.beginTime;
                        file.uuid = video.peer;
                        historyFiles.add(file);
                        String dateInShort = uuid + TimeUtils.getDay(file.time * 1000L);
                        if (!dateMap.containsKey(dateInShort)) {
                            dateMap.put(dateInShort, file.time * 1000L);
                        }
                    }
                    list.clear();//需要清空
                    Collections.reverse(historyFiles);//来个降序
                    Collections.reverse(timeList);
                    try {
                        long timeStart = timeList.get(0), timeEnd = timeList.get(ListUtils.getSize(timeList) - 1);
                        AppLogger.w(String.format(Locale.getDefault(), "before insert uuid:%s,timeStart:%s,timeEnd:%s,performance:%s",
                                uuid, timeStart, timeEnd, (System.currentTimeMillis() - time)));
                        return Observable.just(new Helper(uuid, timeList, historyFiles));
                    } catch (Exception e) {
                        AppLogger.e("err: " + MiscUtils.getErr(e));
                        return Observable.just(new Helper());
                    }
                })
                .subscribeOn(Schedulers.io())
                .filter(helper -> !TextUtils.isEmpty(helper.uuid) && ListUtils.getSize(helper.files) > 0)
                .map(helper -> {
                    long timeStart = helper.timeList.get(0);
                    long timeEnd = helper.timeList.get(ListUtils.getSize(helper.timeList) - 1);
                    BaseApplication.getAppComponent().getDBHelper().deleteHistoryFile(helper.uuid, Math.min(timeStart, timeEnd),
                            Math.max(timeStart, timeEnd))
                            .subscribeOn(Schedulers.io())
                            .map(aBoolean -> {
                                AppLogger.w("delete hisFile:" + aBoolean + ",hisFile:" + ListUtils.getSize(helper.files));
                                BaseApplication.getAppComponent().getDBHelper().saveHistoryFile(helper.files)
                                        .subscribe(ret -> {
                                            AppLogger.w("save hisFile tx");
                                            RxBus.getCacheInstance().post(new RxEvent.JFGHistoryVideoParseRsp(helper.uuid)
                                                    .setFileList(helper.files).setTimeList(helper.timeList));
                                        }, AppLogger::e);
                                return null;
                            })
                            .subscribe(ret -> AppLogger.w("save history good?"),
                                    throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
                    return null;
                })
                .subscribe(ret -> {
                }, throwable -> AppLogger.e("err: " + MiscUtils.getErr(throwable)));
    }

    public void clear() {
        history = null;
    }


    public boolean clearHistoryFile(String uuid) {
        synchronized (lock) {
            DataExt.getInstance().clean();
            Iterator<String> keySet = dateMap.keySet().iterator();
            List<String> keyList = new ArrayList<>();
            while (keySet.hasNext()) {
                String key = keySet.next();
                if (key != null && key.startsWith(uuid)) {
                    keyList.add(key);
                }
            }
            for (String key : keyList) {
                dateMap.remove(key);
            }

            BaseApplication.getAppComponent().getDBHelper().deleteHistoryFile(uuid, 0, Integer.MAX_VALUE)
                    .subscribeOn(Schedulers.io())
                    .subscribe(ret -> {
                    }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
        }
        return true;
    }

    private static class Helper {
        private String uuid;
        private ArrayList<Long> timeList;
        private ArrayList<HistoryFile> files;

        public Helper() {

        }

        public Helper(String uuid, ArrayList<Long> timeList, ArrayList<HistoryFile> files) {
            this.uuid = uuid;
            this.files = files;
            this.timeList = timeList;
        }
    }
}
