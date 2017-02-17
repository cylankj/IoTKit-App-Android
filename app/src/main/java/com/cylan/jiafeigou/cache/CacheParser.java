package com.cylan.jiafeigou.cache;

import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.dp.DataPointManager;
import com.cylan.jiafeigou.dp.IParser;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-11-8.
 * dp数据解析器
 */

public class CacheParser {
    private static final String TAG = "CacheParser";

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private static CacheParser dpParser;

    /**
     * 历史视频数据
     */
    private IParser historyCache;

    private IParser dataPoint;

    private CacheParser() {
        historyCache = History.getHistory();
        DataPointManager manager = DataPointManager.getInstance();
        dataPoint = manager;
        GlobalDataProxy.getInstance().setDataPointManager(manager);
    }

    public static CacheParser getDpParser() {
        if (dpParser == null) {
            dpParser = new CacheParser();
        }
        return dpParser;
    }

    public void registerDpParser() {
        AppLogger.i("CacheParser: registerDpParser");
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
        }
        compositeSubscription = new CompositeSubscription();
        Subscription[] sub = historyCache.register();
        if (sub != null) {
            for (Subscription s : sub) {
                compositeSubscription.add(s);
            }
        }
        sub = dataPoint.register();
        if (sub != null) {
            for (Subscription s : sub) {
                compositeSubscription.add(s);
            }
        }
    }


    public void unregisterDpParser() {
        AppLogger.e("CacheParser: unregisterDpParser");
        if (compositeSubscription != null)
            compositeSubscription.unsubscribe();
        if (historyCache != null)
            historyCache.clear();
        if (dataPoint != null)
            dataPoint.clear();
    }


}
