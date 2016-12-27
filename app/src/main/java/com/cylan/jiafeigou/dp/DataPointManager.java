package com.cylan.jiafeigou.dp;

import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public class DataPointManager implements IParser, IDataPoint {

    private static final String TAG = "DataPointManager";

    private static DataPointManager instance;
    private Map<Long, Long> querySeqMap = new HashMap<>();

    @Override
    public Subscription[] register() {
        return new Subscription[]{fullDataPointAssembler()};
    }

    @Override
    public void clear() {
        bundleMap.clear();
    }

    /**
     * object: 可以是HashSet<BaseValue>,可以是BaseValue
     */
    private HashMap<String, Object> bundleMap = new HashMap<>();
    //硬编码,注册list类型的id
    private static final HashMap<Long, Integer> mapObject = new HashMap<>();


    static {
        mapObject.put(505L, 505);
        mapObject.put(204L, 204);
    }

    public static DataPointManager getInstance() {
        if (instance == null) {
            synchronized (DataPointManager.class) {
                if (instance == null)
                    instance = new DataPointManager();
            }
        }
        return instance;
    }

    private DataPointManager() {
    }

    private boolean putHashSetValue(String uuid, BaseValue baseValue) {
        boolean isSet = mapObject.containsKey(baseValue.getId());
        if (!isSet) {
            AppLogger.e("you go the wrong way: " + baseValue.getId());
            return false;
        }
        Object o = bundleMap.get(uuid);
        HashSet<BaseValue> set;
        if (o != null && o instanceof HashSet) {
            set = cast(o);
            set.add(baseValue);
        } else {
            set = new HashSet<>();
            set.add(baseValue);
            o = set;
        }
        bundleMap.put(uuid, o);
        return true;
    }

    private boolean putValue(String uuid, BaseValue baseValue) {
        boolean update = false;
        synchronized (DataPointManager.class) {
            boolean isSetType = mapObject.containsKey(baseValue.getId());
            if (isSetType) {
                return putHashSetValue(uuid, baseValue);
            } else {
                Object o = bundleMap.get(uuid);
                if (o != null && o instanceof BaseValue) {
                    if (((BaseValue) o).getVersion() < baseValue.getVersion()) {
                        bundleMap.remove(uuid);
                        update = true;
                    }
                }
                bundleMap.put(uuid, baseValue);
            }
        }
        return update;
    }

    private Object removeId(String uuid, long id) {
        return bundleMap.remove(uuid + id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Set<?>> T cast(Object obj) {
        return (T) obj;
    }

    private Subscription fullDataPointAssembler() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .subscribeOn(Schedulers.computation())
                .map(new Func1<RobotoGetDataRsp, Integer>() {
                    @Override
                    public Integer call(RobotoGetDataRsp dpDataRsp) {
                        final String identity = dpDataRsp.identity;
                        Log.d(TAG, "fullDataPointAssembler: " + identity);
                        boolean needNotify = false;
                        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : dpDataRsp.map.entrySet()) {
                            if (entry.getValue() == null) continue;
                            for (JFGDPMsg dp : entry.getValue()) {
                                try {
                                    if (dp == null) continue;
                                    Object o = DpUtils.unpackData(dp.packValue, DpMsgMap.ID_2_CLASS_MAP.get((int) dp.id));
                                    BaseValue value = new BaseValue();
                                    value.setId(dp.id);
                                    value.setVersion(dp.version);
                                    value.setValue(o);
                                    boolean changed = putValue(identity + dp.id, value);
                                    needNotify |= changed;
                                    Log.d(TAG, "put: " + dp.id + " " + changed + " " + needNotify);
                                } catch (Exception e) {
                                    AppLogger.i("dp is null: " + dp.id + ".." + e.getLocalizedMessage());
                                }
                            }
                        }
                        if (needNotify && querySeqMap.containsKey(dpDataRsp.seq)) {
                            Log.e(TAG, "file update: " + dpDataRsp.seq);
                            querySeqMap.remove(dpDataRsp.seq);
                            RxBus.getCacheInstance().post(dpDataRsp.seq);
                        }
                        return null;
                    }
                })
                //此retry能跳过当前一次的exception
                .retry(new RxHelper.RxException<>(TAG + "deviceDpSub"))
                .subscribe();
    }

    @Override
    public boolean insert(String uuid, BaseValue baseValue) {
        return putValue(uuid, baseValue);
    }

    @Override
    public boolean update(String uuid, BaseValue baseValue) {
        return putValue(uuid, baseValue);
    }

    @Override
    public Object delete(String uuid, long id) {
        return removeId(uuid, id);
    }

    @Override
    public Object delete(String uuid, long id, long version) {
        boolean isSet = mapObject.containsKey(id);
        if (isSet) {
            Object o = bundleMap.get(uuid + id);
            if (o != null && o instanceof HashSet) {
                HashSet<BaseValue> set = cast(o);
                BaseValue value = new BaseValue();
                value.setId(id);
                value.setVersion(version);
                return set.remove(value);
            }
        } else {
            return bundleMap.remove(uuid + id);
        }
        AppLogger.i("delete: " + id + ",version:" + version);
        return null;
    }

    @Override
    public BaseValue fetchLocal(String uuid, long id) {
        try {
            return (BaseValue) bundleMap.get(uuid + id);
        } catch (ClassCastException c) {
            AppLogger.e(String.format("id:%s is not registered in DataPointManager#mapObject,%s", id, c.getLocalizedMessage()));
            return null;
        }
    }

    @Override
    public ArrayList<BaseValue> fetchLocalList(String uuid, long id) {
        try {
            Object o = bundleMap.get(uuid + id);
            if (o != null && o instanceof HashSet) {
                HashSet<BaseValue> set = cast(o);//bundleMap中存的是HashSet,set的key不会重复.
                return new ArrayList<>(set);
            }
        } catch (ClassCastException c) {
            AppLogger.e(String.format("id:%s is not registered in DataPointManager#mapObject,%s", id, c.getLocalizedMessage()));
            return null;
        }
        return null;
    }

    @Override
    public long robotGetData(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) throws JfgException {
        long seq = JfgCmdInsurance.getCmd().robotGetData(peer, queryDps, limit, asc, timeoutMs);
        querySeqMap.put(seq, seq);
        return seq;
    }

}
