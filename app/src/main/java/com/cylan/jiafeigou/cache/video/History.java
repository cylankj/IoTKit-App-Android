package com.cylan.jiafeigou.cache.video;

import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 历史录像数据管理中心
 * Created by cylan-hunt on 16-12-6.
 */

public class History {

    private volatile static History history;
    /**
     * 数据集,不实现Lru逻辑
     */

    private HashMap<String, ArrayList<JFGVideo>> historyMap = new HashMap<>();

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

    public void cacheHistoryDataList(JFGHistoryVideo historyVideo) {
        if (historyVideo == null || historyVideo.list == null || historyVideo.list.size() == 0)
            return;
        long time = System.currentTimeMillis();
        String uuid = historyVideo.list.get(0).peer;
        ArrayList<JFGVideo> list = historyMap.get(uuid);
        if (list == null)
            list = new ArrayList<>();
        list.addAll(historyVideo.list);
        list = new ArrayList<>(new HashSet<>(list));
        Collections.sort(list);
        AppLogger.d("get historyList: " + uuid + (System.currentTimeMillis() - time));
        RxBus.getCacheInstance().post(new RxEvent.JFGHistoryVideoParseRsp(uuid));
    }

    public void clear() {
        historyMap.clear();
        history = null;
    }

    public ArrayList<JFGVideo> getHistoryList(String uuid) {
        return historyMap.get(uuid);
    }

}
