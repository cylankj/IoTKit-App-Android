package com.cylan.jiafeigou.provider;


import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.BellDevice;
import com.cylan.jiafeigou.base.module.CameraDevice;
import com.cylan.jiafeigou.base.module.EfamilyDevice;
import com.cylan.jiafeigou.base.module.JFGDevice;
import com.cylan.jiafeigou.base.module.MagDevice;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.dp.DataPoint;
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
    private Map<String, JFGDevice> mCachedDeviceMap = new HashMap<>();//和uuid相关的数据缓存
    private LongSparseArray<DataPoint> mCachedGenericMap = new LongSparseArray<>();//和uuid无关,和当前账号相关的数据缓存
    private JFGAccount mJFGAccount;
    private static DataSourceManager mDataSourceManager;

    private boolean isOnline;


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
    }

    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public <T extends JFGDevice> T getJFGDevice(String uuid) {
        return (T) mCachedDeviceMap.get(uuid);
    }

    @Override
    public List<JFGDevice> getAllJFGDevice() {
        List<JFGDevice> result = new ArrayList<>(mCachedDeviceMap.size());
        for (Map.Entry<String, ? extends JFGDevice> entry : mCachedDeviceMap.entrySet()) {
            result.add(entry.getValue());
        }
        return result;
    }

    public List<JFGDevice> getJFGDeviceByPid(int... pids) {
        if (pids == null) return null;

        List<JFGDevice> result = new ArrayList<>();
        for (Map.Entry<String, JFGDevice> device : mCachedDeviceMap.entrySet()) {
            for (int pid : pids) {
                if (device.getValue().pid == pid) {
                    result.add(device.getValue());
                    break;
                }
            }
        }
        return result;
    }

    public List<String> getJFGDeviceUUIDByPid(int... pids) {
        if (pids == null) return null;
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, JFGDevice> device : mCachedDeviceMap.entrySet()) {
            for (int pid : pids) {
                if (device.getValue() != null && device.getValue().pid == pid) {
                    result.add(device.getValue().uuid);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void cacheJFGDevices(com.cylan.entity.jniCall.JFGDevice... devices) {
        for (com.cylan.entity.jniCall.JFGDevice device : devices) {
            JFGDevice temp = mCachedDeviceMap.get(device.uuid);
            if (temp != null) {//已经存在了,则更新即可
                temp.setDevice(device);
            } else {//不存在,则添加
                JFGDevice jfgDevice = create(device);
                if (jfgDevice != null) mCachedDeviceMap.put(device.uuid, jfgDevice);
            }
        }
        syncAllJFGDeviceProperty();
    }


    //主动发起请求,来获取设备所有的属性
    public void syncAllJFGDeviceProperty() {
        if (mCachedDeviceMap.size() == 0) return;
        for (Map.Entry<String, JFGDevice> entry : mCachedDeviceMap.entrySet()) {
            syncJFGDeviceProperty(entry.getKey());
        }
    }

    public void syncJFGDeviceProperty(String uuid) {
        if (TextUtils.isEmpty(uuid) || mJFGAccount == null) return;
        JFGDevice device = mCachedDeviceMap.get(uuid);
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
    public void cacheJFGAccount(JFGAccount account) {
        mJFGAccount = account;
        syncAllJFGDeviceProperty();
    }

    @Override
    public JFGAccount getJFGAccount() {
        return mJFGAccount;
    }

    @Override
    public void cacheRobotoGetDataRsp(RobotoGetDataRsp dataRsp) {
        final String identity = dataRsp.identity;
        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : dataRsp.map.entrySet()) {
            if (entry.getValue() == null) continue;
            boolean changed = false;
            for (JFGDPMsg dp : entry.getValue()) {
                if (TextUtils.isEmpty(identity)) {//账号相关数据,
                    // TODO: 2017/1/8 账号相关数据的处理

                } else {//uuid相关数据
                    JFGDevice device = mCachedDeviceMap.get(identity);
                    if (device != null) {
                        changed |= device.setValue(dp, dataRsp.seq);
                    }
                }
            }

            //每一个响应都需要被通知,即使没有数据变化,以免客户端无限等待
            RxEvent.GetDataResponse response = new RxEvent.GetDataResponse();
            response.changed = changed;
            response.seq = dataRsp.seq;
            response.msgId = entry.getKey();
            RxBus.getCacheInstance().post(response);
        }
    }

    @Override
    public void cacheRobotoSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {
        JFGDevice device = mCachedDeviceMap.get(s);
        if (device != null) {
            boolean changed = false;
            for (JFGDPMsg msg : arrayList) {
                changed |= device.setValue(msg);
            }
            if (changed) {
                RxBus.getCacheInstance().postSticky(new RxEvent.DeviceSyncRsp().setUuid(s));
            }
        }
    }

    @Override
    public <T> T getValue(String uuid, long msgId) {
        return getValue(uuid, msgId, -1);
    }

    public <T> T getValue(String uuid, long msgId, long seq) {
        JFGDevice device = mCachedDeviceMap.get(uuid);
        if (device != null) {
            return device.getValue(msgId, seq);
        }
        return null;
    }

    private JFGDevice create(com.cylan.entity.jniCall.JFGDevice device) {
        JFGDevice result = null;
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
                result = new CameraDevice().setDevice(device);
                break;

            //门铃设备
            case OS_DOOR_BELL:
                result = new BellDevice().setDevice(device);
                break;

            //中控设备
            case OS_EFAML:
                result = new EfamilyDevice().setDevice(device);
                break;
            case OS_TEMP_HUMI:
                break;
            case OS_IR:
                break;

            //门磁设备
            case OS_MAGNET:
                result = new MagDevice().setDevice(device);
                break;
            case OS_AIR_DETECTOR:
                break;

            case OS_DOOR_BELL_CAM:
                break;
            case OS_DOOR_BELL_V2:
                break;
            default:
                result = new JFGDevice() {
                };
        }
        return result;
    }
}
