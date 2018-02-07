package com.cylan.jiafeigou.module;

import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by yanzhendong on 2017/12/25.
 */

public class HistoryManager {
    private static HistoryManager instance;
    private ConcurrentHashMap<String, HashSet<HistoryObserver>> historyObserverHashMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, TreeSet<JFGVideo>> historyVideoMap = new ConcurrentHashMap<>();
    private HashSet<String> notifyQueen = new HashSet<>();
    private ReentrantLock lock = new ReentrantLock();
    private Comparator<JFGVideo> comparator = new Comparator<JFGVideo>() {
        @Override
        public int compare(JFGVideo o1, JFGVideo o2) {
            return (int) (o1.beginTime - o2.beginTime);
        }
    };

    public static HistoryManager getInstance() {
        if (instance == null) {
            synchronized (HistoryManager.class) {
                if (instance == null) {
                    instance = new HistoryManager();
                }
            }
        }
        return instance;
    }

    public void clearHistory(String uuid) {
        historyVideoMap.remove(uuid);
    }

    public boolean hasHistoryObserver(String uuid) {
        return historyObserverHashMap.get(uuid) != null;
    }


    public interface HistoryObserver {
        void onHistoryChanged(Collection<JFGVideo> history);
    }

    public void addHistoryObserver(String uuid, HistoryObserver observer) {
        HashSet<HistoryObserver> historyObservers = historyObserverHashMap.get(uuid);
        if (historyObservers == null) {
            historyObservers = new HashSet<>();
            historyObserverHashMap.put(uuid, historyObservers);
        }
        historyObservers.add(observer);
        TreeSet<JFGVideo> jfgVideos = historyVideoMap.get(uuid);
        if (jfgVideos != null && jfgVideos.size() > 0) {
            observer.onHistoryChanged(getHistory(uuid));
        }
    }

    public void removeHistoryObserver(String uuid, HistoryObserver observer) {
        HashSet<HistoryObserver> historyObservers = historyObserverHashMap.get(uuid);
        if (historyObservers != null) {
            historyObservers.remove(observer);
        }
    }

    public boolean hasHistory(String uuid) {
        TreeSet<JFGVideo> jfgVideos = historyVideoMap.get(uuid);
        return jfgVideos != null && jfgVideos.size() > 0;
    }

    public TreeSet<JFGVideo> getHistory(String uuid) {
        lock.lock();
        try {
            ArrayList<JFGVideo> jfgVideos = new ArrayList<>(historyVideoMap.get(uuid));
            TreeSet<JFGVideo> treeSet = new TreeSet<>(comparator);
            treeSet.addAll(jfgVideos);
            return treeSet;
        } finally {
            lock.unlock();
        }
    }

    public JFGVideo getMinHistory(String uuid) {
        TreeSet<JFGVideo> jfgVideos = historyVideoMap.get(uuid);
        return jfgVideos != null ? jfgVideos.first() : null;
    }

    public void cacheHistory(JFGHistoryVideo historyVideo) {
        if (historyVideo.list != null && historyVideo.list.size() > 0) {
            HandlerThreadUtils.post(new Runnable() {
                @Override
                public void run() {
                    lock.lock();
                    try {
                        notifyQueen.clear();
                        for (JFGVideo video : historyVideo.list) {
                            TreeSet<JFGVideo> jfgVideos = historyVideoMap.get(video.peer);
                            if (jfgVideos == null) {
                                jfgVideos = new TreeSet<>(comparator);
                                historyVideoMap.put(video.peer, jfgVideos);
                            }
                            jfgVideos.add(video);
                            notifyQueen.add(video.peer);
                        }

                        for (String peer : notifyQueen) {
                            HashSet<HistoryObserver> historyObservers = historyObserverHashMap.get(peer);
                            if (historyObservers != null) {
                                try {
                                    for (HistoryObserver historyObserver : historyObservers) {
                                        historyObserver.onHistoryChanged(new ArrayList<>(historyVideoMap.get(peer)));
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            });
        }
    }

    public void cacheHistory(byte[] bytes) {
        HistoryV2Manager.getInstance().cacheHistory(bytes);
    }

    public void fetchHistoryV1(String uuid) {
        try {
            Command.getInstance().getVideoList(uuid);
//            Command.getInstance().getVideoListV2(uuid, (int) (System.currentTimeMillis() / 1000), 1, 365);
        } catch (JfgException e) {
            e.printStackTrace();
            AppLogger.e(e);
        }
    }

    public void fetchHistoryV2(String uuid, int beginTime, int way, int num) {
        fetchHistoryV1(uuid);
//        HistoryV2Manager.getInstance().fetchHistoryV2(uuid, beginTime, way, num);
    }

    private static class HistoryV2Manager {

        private static class PendingQueryAction {
            public String uuid;
            public int beginTime;
            public int way;
            public int num;
        }

        private static HistoryV2Manager instance;
        private TreeSet<PendingQueryAction> pendingQueryActions = new TreeSet<>();

        public static HistoryV2Manager getInstance() {
            if (instance == null) {
                synchronized (HistoryV2Manager.class) {
                    if (instance == null) {
                        instance = new HistoryV2Manager();
                    }
                }
            }
            return instance;
        }

        void fetchHistoryV2(String uuid, int beginTime, int way, int num) {
            try {
                Command.getInstance().getVideoListV2(uuid, beginTime, way, num);
            } catch (JfgException e) {
                e.printStackTrace();
                AppLogger.e(e);
            }
        }

        private void executePendingQueryAction() {

        }

        private void decidePendingQueryAction(String caller, Map<Integer, List<DpMsgDefine.Unit>> dataMap) {
        }

        private void notifyHistoryChangedIfNeeded(String caller, Map<Integer, List<DpMsgDefine.Unit>> dataMap) {

        }

        void cacheHistory(byte[] bytes) {
            DpMsgDefine.UniversalDataBaseRsp rsp = DpUtils.unpackDataWithoutThrow(bytes, DpMsgDefine.UniversalDataBaseRsp.class, null);
            if (rsp == null) {
                //不管怎么样 pendingQueryAction 都要执行
                executePendingQueryAction();
                return;
            }
            if (rsp.way == 1) {
                //日期参数回来,有些设备坑了，一次只能返回两天。所以只能动态加载了。
                decidePendingQueryAction(rsp.caller, rsp.dataMap);
            } else {
                //按照分钟的查询回来
                notifyHistoryChangedIfNeeded(rsp.caller, rsp.dataMap);
                executePendingQueryAction();
            }
            AppLogger.w("save hisFile tx:" + Arrays.toString(bytes));
        }
    }
}
