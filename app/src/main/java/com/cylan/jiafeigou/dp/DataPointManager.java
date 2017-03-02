package com.cylan.jiafeigou.dp;

import android.util.Log;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public class DataPointManager implements IParser, IDataPoint {
    private static boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "DataPointManager:";

    private static DataPointManager instance;
    /**
     * uuid
     */
    private HashMap<String, JFGDevice> jfgDeviceMap = new HashMap<>();
    /**
     * String----->uuid+id
     * object: 可以是BaseValue
     */
    private HashMap<String, BaseValue> bundleMap = new HashMap<>();
    /**
     * 可以是HashSet<BaseValue>,HashSet是无序的
     */
    private HashMap<String, TreeSet<BaseValue>> bundleSetMap = new HashMap<>();
    //硬编码,注册list类型的id
    private static final HashMap<Long, Integer> mapObject = new HashMap<>();

    /**
     * 未读消息书存放地方,UnreadCount的list存放的是同一个id
     * String:uuid+id.
     * Pair<count(条数),时间>
     */
    private HashMap<String, Pair<Integer, BaseValue>> unreadMap = new HashMap<>();

    @Override
    public Subscription[] register() {
        return new Subscription[]{};
    }


    static {
        mapObject.put(505L, 505);
//        mapObject.put(204L, 204);
        mapObject.put(222L, 222);
        mapObject.put(401L, DpMsgMap.ID_401_BELL_CALL_STATE);//门铃呼叫历史记录,List类型
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

    /**
     * 存放Set类型
     *
     * @param uuid
     * @param baseValue
     * @return
     */
    private boolean putSetValue(String uuid, BaseValue baseValue) {
        boolean isSet = isSetType(baseValue.getId());
        if (!isSet) {
            AppLogger.e("you go the wrong way: " + baseValue.getId());
            return false;
        }
        TreeSet<BaseValue> set = bundleSetMap.get(uuid + baseValue.getId());
        if (set == null) {
            set = new TreeSet<>();
        }
        if (set.contains(baseValue)) return false;//已经包含.id和version相同
        set.add(baseValue);
        bundleSetMap.put(uuid + baseValue.getId(), set);
        if (DEBUG) Log.d(TAG, "putSetValue: " + uuid + " " + baseValue);
        return true;
    }

    public boolean putValue(String uuid, BaseValue baseValue) {
        boolean update = false;
        synchronized (DataPointManager.class) {
            boolean isSetType = isSetType(baseValue.getId());
            if (DEBUG) Log.d(TAG, "isSetType: " + isSetType + " " + baseValue);
            if (isSetType) {
                return putSetValue(uuid, baseValue);
            } else {
                BaseValue o = bundleMap.get(uuid + baseValue.getId());
                if (o != null) {
                    if (o.getVersion() < baseValue.getVersion()) {
                        //如果是
                        bundleMap.remove(uuid + baseValue.getId());
                        update = true;
                    }
                }
                bundleMap.put(uuid + baseValue.getId(), baseValue);
            }
        }
        return update;
    }

    private Object removeId(String uuid, long id) {
        if (DEBUG) Log.d(TAG, "removeId: " + uuid + " " + id + " set:" + mapObject.containsKey(id));
        if (isSetType(id)) return bundleSetMap.remove(uuid + id);
        return bundleMap.remove(uuid + id);
    }


    @Override
    public void cacheDevice(JFGDevice... jfgDevice) {
        if (jfgDevice == null || jfgDevice.length == 0) {
            jfgDeviceMap.clear();
            return;
        }
        for (JFGDevice device : jfgDevice) {
            jfgDeviceMap.put(device.uuid, device);
        }
    }

    @Override
    public boolean remove(String uuid) {
        boolean result = jfgDeviceMap.remove(uuid) != null;
        AppLogger.i(TAG + uuid + " " + result);
        return result;
    }

    @Override
    public JFGDevice getJFGDevice(String uuid) {
//        Log.d(TAG, "getJFGDevice: " + uuid + " " + jfgDeviceMap.get(uuid));
        return jfgDeviceMap.get(uuid);
    }

    @Override
    public void cacheUnread(String uuid, Pair<Integer, BaseValue> pair) {
        if (unreadMap != null) unreadMap.put(uuid, pair);
    }

    @Override
    public boolean updateJFGDevice(JFGDevice device) {
        String finalKey = device.uuid;
        jfgDeviceMap.remove(finalKey);
        return jfgDeviceMap.put(finalKey, device) != null;
    }

    @Override
    public ArrayList<JFGDevice> getAllJFGDevice() {
        Iterator<String> keySet = jfgDeviceMap.keySet().iterator();
        ArrayList<JFGDevice> allList = new ArrayList<>();
        while (keySet.hasNext()) {
            String key = keySet.next();
            allList.add(jfgDeviceMap.get(key));
        }
        AppLogger.d("getAllJFGDevice:" + allList.size());
        return allList;
    }

    @Override
    public boolean insert(String uuid, BaseValue baseValue) {
        return putValue(uuid, baseValue);
    }

    @Override
    public boolean update(String uuid, BaseValue baseValue, boolean sync) {
        boolean result = putValue(uuid, baseValue);
        if (sync) {
            try {
                byte[] data = null;
                Object value = baseValue.getValue();
                if (value != null && value instanceof DataPoint)
                    data = ((DataPoint) value).toBytes();
                else data = DpUtils.pack(value);
                JfgCmdInsurance.getCmd().robotSetData(uuid,
                        DpUtils.getList((int) baseValue.getId(),
                                data,
                                baseValue.getVersion()));

                if (DEBUG && sync) Log.d(TAG, "setDevice: " + value);
            } catch (Exception e) {
                AppLogger.e("" + e.getLocalizedMessage());
            }
        }
        return result;
    }

    @Override
    public boolean deleteAll(String uuid) {
        boolean remove = false;
        Set<String> set0 = bundleMap.keySet();
        for (String s : set0) {
            if (s.contains(uuid)) {
                remove |= bundleMap.remove(s) != null;
            }
        }
        set0 = bundleSetMap.keySet();
        for (String s : set0) {
            if (s.contains(uuid)) {
                remove |= bundleSetMap.remove(s) != null;
            }
        }
        try {
            remove |= JfgCmdInsurance.getCmd().unBindDevice(uuid) == 0;
        } catch (JfgException e) {
            remove = false;
            AppLogger.e("" + e.getLocalizedMessage());
        }
        return remove;
    }

    @Override
    public boolean deleteJFGDevice(String uuid) {
        boolean ret = jfgDeviceMap.remove(uuid) != null;
        try {
            JfgCmdInsurance.getCmd().unBindDevice(uuid);
        } catch (JfgException e) {
            ret = false;
        }
        return ret;
    }

    @Override
    public Object delete(String uuid, long id) {
        return removeId(uuid, id);
    }

    @Override
    public Object delete(String uuid, long id, long version) {
        boolean isSet = isSetType(id);
        if (isSet) {
            TreeSet<BaseValue> set = bundleSetMap.get(uuid + id);
            if (set != null) {
                BaseValue value = new BaseValue();
                value.setId(id);
                value.setVersion(version);
                return set.remove(value);
            }
        } else {
            return bundleMap.remove(uuid + id);
        }
        if (DEBUG) Log.d(TAG, "delete: " + uuid + " " + id + " set:" + isSet);
        return null;
    }

    @Override
    public BaseValue fetchLocal(String uuid, long id) {
        try {
            if (isSetType(id)) AppLogger.e("this id is ArrayType: " + id);
            Log.d(TAG, "contains: " + bundleMap.get(uuid + id));
            return bundleMap.get(uuid + id);
        } catch (ClassCastException c) {
            AppLogger.e(String.format("id:%s %s", id, c.getLocalizedMessage()));
            return null;
        }
    }

    @Override
    public BaseValue fetchLocal(String uuid, long id, boolean topOne) {
        if (!topOne) return fetchLocal(uuid, id);
        boolean isArray = mapObject.containsKey(id);
        if (isArray) {
            TreeSet<BaseValue> set = bundleSetMap.get(uuid + id);
            if (set == null || set.size() == 0)
                return null;
            else {
                ArrayList<BaseValue> list = new ArrayList<>(set);
                Collections.sort(list);
                return list.get(0);
            }
        } else return bundleMap.get(uuid + id);
    }

    @Override
    public boolean deleteAll(String uuid, long id, ArrayList<Long> versions) {
        if (!isSetType(id)) AppLogger.e("this id is not ArrayType: " + id);
        Object result = bundleSetMap.remove(uuid + id);
        if (DEBUG) Log.d(TAG, "deleteAll: " + uuid + " " + id + " " + (result != null));
        deleteRobot(uuid, id, versions);
        return result != null;
    }

    private void deleteRobot(String uuid, long id, ArrayList<Long> versions) {
        if (versions == null || versions.size() == 0)
            return;
        ArrayList<JFGDPMsg> msgs = new ArrayList<>();
        for (long version : versions) {
            JFGDPMsg msg = new JFGDPMsg();
            msg.id = id;
            msg.version = version;
            msgs.add(msg);
        }
        long req = 0;
        try {
            req = JfgCmdInsurance.getCmd().robotDelData(uuid, msgs, 0);
        } catch (JfgException e) {
            e.printStackTrace();
        }
        if (DEBUG) Log.d(TAG, "deleteRobot: " + req + " " + msgs);
    }

    @Override
    public ArrayList<BaseValue> fetchLocalList(String uuid, long id) {
        try {
            TreeSet<BaseValue> set = bundleSetMap.get(uuid + id);
            if (set != null) {
                return new ArrayList<>(set);
            }
        } catch (ClassCastException c) {
            AppLogger.e(String.format("id:%s is not registered in DataPointManager#mapObject,%s", id, c.getLocalizedMessage()));
            return null;
        }
        return null;
    }

    @Override
    public boolean isSetType(long id) {
        return mapObject.containsKey(id);
    }

    @Override
    public Pair<Integer, BaseValue> fetchUnreadCount(String uuid, long id) {
        Pair<Integer, BaseValue> pair = unreadMap.get(uuid + id);
        //如果有数据,直接返回,没有数据做检查,异步响应.
        if (pair == null) {
            unreadReq(uuid, id);
            return null;
        }
        return unreadMap.get(uuid + id);
    }

    private void unreadReq(String uuid, long id) {
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    //为空,尝试一次新的请求.
                    ArrayList<JFGDPMsg> list = new ArrayList<>();
                    JFGDPMsg msg = new JFGDPMsg((int) id, 0);//取最新的.
                    list.add(msg);
                    ArrayList<Long> idList = new ArrayList<>();
                    idList.add(id);
                    try {
                        //先查询数据,这里默认响应的顺序也是  robotGetDataRsp再到RobotCountDataRsp,接到
                        //RobotCountDataRsp,就可以做消息数广播了.
                        robotGetData(uuid, list, 1, false, 0);
                        JfgCmdInsurance.getCmd().robotCountData(uuid, idList, 0);
                    } catch (JfgException e) {
                        AppLogger.e("" + e.getLocalizedMessage());
                    }
                    AppLogger.i(TAG + ",fetchUnreadCount:" + uuid + " " + id);

                });
    }

    @Override
    public boolean markAsRead(String uuid, long id) throws JfgException {
        ArrayList<Long> idList = new ArrayList<>();
        idList.add(id);
        JfgCmdInsurance.getCmd().robotCountDataClear(uuid, idList, 0);
        return unreadMap.remove(uuid + id) != null;
    }

    @Override
    public long robotGetData(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) throws JfgException {
        return JfgCmdInsurance.getCmd().robotGetData(peer, queryDps, limit, asc, timeoutMs);
    }

    @Override
    public void clear() {
        if (DEBUG) Log.d(TAG, "clear: ");
        bundleMap.clear();
        jfgDeviceMap.clear();
        bundleSetMap.clear();
        unreadMap.clear();
    }

}
