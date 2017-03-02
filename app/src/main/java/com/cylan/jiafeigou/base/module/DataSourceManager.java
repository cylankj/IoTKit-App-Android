package com.cylan.jiafeigou.base.module;


import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.BaseDPHelper;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cylan.jiafeigou.misc.JConstant.OS_AIR_DETECTOR;
import static com.cylan.jiafeigou.misc.JConstant.OS_ANDROID_PHONE;
import static com.cylan.jiafeigou.misc.JConstant.OS_CAMARA_ANDROID_SERVICE;
import static com.cylan.jiafeigou.misc.JConstant.OS_CAMERA_ANDROID;
import static com.cylan.jiafeigou.misc.JConstant.OS_CAMERA_ANDROID_4G;
import static com.cylan.jiafeigou.misc.JConstant.OS_CAMERA_CC3200;
import static com.cylan.jiafeigou.misc.JConstant.OS_CAMERA_PANORAMA_GUOKE;
import static com.cylan.jiafeigou.misc.JConstant.OS_CAMERA_PANORAMA_HAISI;
import static com.cylan.jiafeigou.misc.JConstant.OS_CAMERA_PANORAMA_QIAOAN;
import static com.cylan.jiafeigou.misc.JConstant.OS_CAMERA_UCOS;
import static com.cylan.jiafeigou.misc.JConstant.OS_CAMERA_UCOS_V2;
import static com.cylan.jiafeigou.misc.JConstant.OS_CAMERA_UCOS_V3;
import static com.cylan.jiafeigou.misc.JConstant.OS_DOOR_BELL;
import static com.cylan.jiafeigou.misc.JConstant.OS_DOOR_BELL_CAM;
import static com.cylan.jiafeigou.misc.JConstant.OS_DOOR_BELL_V2;
import static com.cylan.jiafeigou.misc.JConstant.OS_EFAML;
import static com.cylan.jiafeigou.misc.JConstant.OS_IOS_PHONE;
import static com.cylan.jiafeigou.misc.JConstant.OS_IR;
import static com.cylan.jiafeigou.misc.JConstant.OS_MAGNET;
import static com.cylan.jiafeigou.misc.JConstant.OS_PC;
import static com.cylan.jiafeigou.misc.JConstant.OS_SERVER;
import static com.cylan.jiafeigou.misc.JConstant.OS_TEMP_HUMI;

/**
 * Created by yzd on 16-12-28.
 */

public class DataSourceManager implements JFGSourceManager {
    private final String TAG = getClass().getName();
    /**
     * 只缓存当前账号下的数据,一旦注销将会清空所有的缓存,内存缓存方式
     */
    private Map<String, JFGDPDevice> mCachedDeviceMap = new HashMap<>();//和uuid相关的数据缓存
    private JFGDPAccount mJFGAccount;//账号相关的数据全部保存到这里面
    private static DataSourceManager mDataSourceManager;
    private boolean isOnline;
    private JFGDPDevice mFakeDevice = new JFGDPDevice() {
    };

    private DataSourceManager() {
    }

    public static DataSourceManager getInstance() {
        if (mDataSourceManager == null) {
            synchronized (DataSourceManager.class) {
                if (mDataSourceManager == null) {
                    mDataSourceManager = new DataSourceManager();
                }
            }
        }
        return mDataSourceManager;
    }

    public void setOnline(boolean online) {
        isOnline = online;
//        if (!(getLoginState = online)) {//没有登录的话则清除所有的缓存
//            mCachedDeviceMap.clear();
//            mJFGAccount = null;
//        }
        //什么也不做,防止程序崩溃
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public <T extends JFGDPDevice> T getJFGDevice(String uuid) {

        JFGDPDevice device = mCachedDeviceMap.get(uuid);
//        if (device == null&& BuildConfig.DEBUG) throw new IllegalArgumentException("天啊,它真的发生了,你是不是又在乱传参数???");
        return device == null ? null : getValueWithAccountCheck((T) device.$());
    }

    @Override
    public List<JFGDPDevice> getAllJFGDevice() {
        List<JFGDPDevice> result = new ArrayList<>(mCachedDeviceMap.size());
        for (Map.Entry<String, ? extends DataPoint> entry : mCachedDeviceMap.entrySet()) {
            result.add(getJFGDevice(entry.getKey()));
        }
        return getValueWithAccountCheck(result);
    }

    public List<JFGDPDevice> getJFGDeviceByPid(int... pids) {
        if (pids == null) return null;

        List<JFGDPDevice> result = new ArrayList<>();
        for (Map.Entry<String, JFGDPDevice> device : mCachedDeviceMap.entrySet()) {
            for (int pid : pids) {
                if (device.getValue().pid == pid) {
                    result.add(getJFGDevice(device.getKey()));
                    break;
                }
            }
        }
        return getValueWithAccountCheck(result);
    }

    public List<String> getJFGDeviceUUIDByPid(int... pids) {
        if (pids == null) return null;
        List<String> result = new ArrayList<>();
        List<JFGDPDevice> devices = getJFGDeviceByPid(pids);
        for (JFGDPDevice device : devices) {
            result.add(device.uuid);
        }
        return getValueWithAccountCheck(result);
    }

    @Override
    public void cacheJFGDevices(com.cylan.entity.jniCall.JFGDevice... devices) {
        for (com.cylan.entity.jniCall.JFGDevice device : devices) {
            JFGDPDevice temp = mCachedDeviceMap.get(device.uuid);
            if (temp != null) {//已经存在了,则更新即可
                temp.setDevice(device);
            } else {//不存在,则添加
                JFGDPDevice jfgDevice = create(device);
                if (jfgDevice != null) mCachedDeviceMap.put(device.uuid, jfgDevice);
            }
        }
        syncAllJFGDeviceProperty();
    }

    @Override
    public void cacheJFGAccount(com.cylan.entity.jniCall.JFGAccount account) {
        mJFGAccount = new JFGDPAccount().setAccount(account);
        syncAllJFGDeviceProperty();
    }


    //主动发起请求,来获取设备所有的属性
    public void syncAllJFGDeviceProperty() {
        if (mCachedDeviceMap.size() == 0) return;
        for (Map.Entry<String, JFGDPDevice> entry : mCachedDeviceMap.entrySet()) {
            syncJFGDeviceProperty(entry.getKey());
        }
    }

    @Override
    public <T extends DataPoint> List<T> getValueBetween(String uuid, long msgId, long startVersion, long endVersion) {
        List<T> result = new ArrayList<>();
        DpMsgDefine.DPSet<T> origin = getValue(uuid, msgId);
        for (T t : origin.value) {
            if (t.version >= startVersion && t.version < endVersion) {
                result.add(t);
            }
        }
        return result;
    }

    public void syncJFGDeviceProperty(String uuid) {
        if (TextUtils.isEmpty(uuid) || mJFGAccount == null) return;
        JFGDPDevice device = mCachedDeviceMap.get(uuid);
        if (device != null) {
            ArrayList<JFGDPMsg> parameters = device.getQueryParameters(false);
            try {
                JfgCmdInsurance.getCmd().robotGetData(uuid, parameters, 1, false, 0);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public JFGDPAccount getJFGAccount() {
        return mJFGAccount;
    }

    @Override
    public void cacheRobotoGetDataRsp(RobotoGetDataRsp dataRsp) {
        final String identity = dataRsp.identity;
        JFGDPDevice device = mCachedDeviceMap.get(identity);
        boolean changed = false;
        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : dataRsp.map.entrySet()) {
            if (entry.getValue() == null) continue;
            changed = false;
            for (JFGDPMsg dp : entry.getValue()) {
                BaseDPHelper.getInstance().saveDPByte(identity, dp.version, (int) dp.id, dp.packValue).subscribe();
                if (device != null) {//优先尝试写入device中
                    changed |= device.setValue(dp, dataRsp.seq);
                    continue;
                }
                if (mJFGAccount != null) {//到这里说明无法将数据写入device中,则写入到account中
                    changed |= mJFGAccount.setValue(dp, dataRsp.seq);
                    if (changed) mJFGAccount.version = System.currentTimeMillis();
                }
            }

            //每一个响应都需要被通知,即使没有数据变化,以免客户端无限等待
            RxEvent.GetDataResponse response = new RxEvent.GetDataResponse();
            response.changed = changed;
            response.seq = dataRsp.seq;
            response.msgId = entry.getKey();
            RxBus.getCacheInstance().post(response);
        }

        RxEvent.ParseResponseCompleted completed = new RxEvent.ParseResponseCompleted();
        completed.seq = dataRsp.seq;
        RxBus.getCacheInstance().post(completed);
        if (changed) {
            long version = System.currentTimeMillis();
            if (device != null) device.version = version;
            else if (mJFGAccount != null) mJFGAccount.version = version;
        }

    }

    @Override
    public void cacheRobotoSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {
        JFGDPDevice device = mCachedDeviceMap.get(s);
        if (device != null) {
            boolean changed = false;
            for (JFGDPMsg msg : arrayList) {
                changed |= device.setValue(msg);
            }
            if (changed) {
                device.version = System.currentTimeMillis();
                RxBus.getCacheInstance().postSticky(new RxEvent.DeviceSyncRsp().setUuid(s));
            }
        }
    }

    @Override
    public <T extends DataPoint> T getValue(String uuid, long msgId) {
        return getValue(uuid, msgId, -1);
    }

    public <T extends DataPoint> T getValue(String uuid, long msgId, long seq) {
        T result = null;
        JFGDPDevice device = mCachedDeviceMap.get(uuid);
        if (device != null) {
            //这里优先从根据UUID从device中获取数据
            result = device.$().getValue(msgId, seq);
        }
        if (result == null && mJFGAccount != null) {
            //如果无法从device中获取值,则从account中获取
            result = mJFGAccount.$().getValue(msgId, seq);
        }
        return getValueWithAccountCheck(result);
    }

    public <T> T getValueWithAccountCheck(T value) {
//        if (mJFGAccount == null || !getLoginState) {
//            return null;
//        }
        return value;
    }


    private JFGDPDevice create(com.cylan.entity.jniCall.JFGDevice device) {
        JFGDPDevice result = null;
        switch (device.pid) {
            case OS_SERVER:
                break;
            case OS_IOS_PHONE:
                break;
            case OS_PC:
                break;
            case OS_ANDROID_PHONE:
                break;

            //摄像头设备
            case OS_CAMARA_ANDROID_SERVICE:
            case OS_CAMERA_ANDROID:
            case OS_CAMERA_ANDROID_4G:
            case OS_CAMERA_CC3200:
            case OS_CAMERA_UCOS:
            case OS_CAMERA_PANORAMA_HAISI:
            case OS_CAMERA_PANORAMA_QIAOAN:
            case OS_CAMERA_PANORAMA_GUOKE:
            case OS_CAMERA_UCOS_V2:
            case OS_CAMERA_UCOS_V3:
                result = new JFGCameraDevice().setDevice(device);
                break;

            //门铃设备
            case OS_DOOR_BELL:
                result = new JFGDoorBellDevice().setDevice(device);
                break;

            //中控设备
            case OS_EFAML:
                result = new JFGEFamilyDevice().setDevice(device);
                break;
            case OS_TEMP_HUMI:
                break;
            case OS_IR:
                break;

            //门磁设备
            case OS_MAGNET:
                result = new JFGMagnetometerDevice().setDevice(device);
                break;
            case OS_AIR_DETECTOR:
                break;

            case OS_DOOR_BELL_CAM:
                break;
            case OS_DOOR_BELL_V2:
                break;
            default:
                result = new JFGDPDevice() {
                };
        }
        return result;
    }
}
