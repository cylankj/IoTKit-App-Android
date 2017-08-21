package com.cylan.jiafeigou.base.module;

import android.util.Log;
import android.util.SparseArray;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;

import java.util.ArrayList;

import static com.cylan.jiafeigou.base.module.DPDevice.CAMERA;
import static com.cylan.jiafeigou.base.module.DPDevice.DOORBELL;
import static com.cylan.jiafeigou.base.module.DPDevice.PROPERTY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_1001_CAM_505_UNREAD_COUNT;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_1002_CAM_512UNREAD_COUNT_V2;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_1003_CAM_222_UNREAD_COUNT;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_1004_BELL_UNREAD_COUNT;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_1005_BELL_UNREAD_COUNT_V2;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_201_NET;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_202_MAC;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_203_SD_FORMAT_RSP;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_204_SDCARD_STORAGE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_205_CHARGING;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_206_BATTERY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_207_DEVICE_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_208_DEVICE_SYS_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_209_LED_INDICATOR;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_210_UP_TIME;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_211_APP_UPLOAD_LOG;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_212_DEVICE_UPLOAD_LOG;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_213_DEVICE_P2P_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_214_DEVICE_TIME_ZONE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_215_DEVICE_RTMP;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_216_DEVICE_VOLTAGE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_219_DEVICE_BIND_LOG;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_220_SDK_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_222_SDCARD_SUMMARY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_223_MOBILE_NET;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_301_DEVICE_MIC;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_302_DEVICE_SPEAKER;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_401_BELL_CALL_STATE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_402_BELL_VOICE_MSG;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_501_CAMERA_ALARM_FLAG;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_502_CAMERA_ALARM_INFO;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_503_CAMERA_ALARM_SENSITIVITY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_505_CAMERA_ALARM_MSG;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_506_CAMERA_TIME_LAPSE_PHOTOGRAPHY;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_508_CAMERA_STANDBY_FLAG;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_509_CAMERA_MOUNT_MODE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_510_CAMERA_COORDINATE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_511_CAMERAWARNANDWONDER;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_512_CAMERA_ALARM_MSG_V3;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_513_CAM_RESOLUTION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_514_CAM_WARNINTERVAL;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_515_CAM_ObjectDetect;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_601_ACCOUNT_STATE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_701_SYS_PUSH_FLAG;

/**
 * Created by yanzhendong on 2017/3/25.
 *
 * @deprecated 即将废弃 请使用 MessageMapper
 */

public class BasePropertyParser implements IPropertyParser {
    private SparseArray<DPProperty> properties = new SparseArray<>();
    public static BasePropertyParser instance;

    public static BasePropertyParser getInstance() {
        return instance;
    }

    public BasePropertyParser() {
        instance = this;
        init();
    }

    /**
     * set类型不需要指定设备类型。
     */
    private void init() {
        //ip
        properties.put(227, new DPProperty(String.class, DPProperty.LEVEL_HOME, DOORBELL, CAMERA));
        //客户端配置/设备端查询 设备是否使用有线网络
        properties.put(226, new DPProperty(boolean.class, DPProperty.LEVEL_HOME, DOORBELL, CAMERA));
        //设备端是否存在可用的有线网络。
        //设备端插入和拔出网线均会上报该消息。
        properties.put(225, new DPProperty(int.class, DPProperty.LEVEL_HOME, CAMERA));

        properties.put(ID_1005_BELL_UNREAD_COUNT_V2, new DPProperty(int.class, DPProperty.LEVEL_HOME, DOORBELL));
        properties.put(ID_1004_BELL_UNREAD_COUNT, new DPProperty(int.class, DPProperty.LEVEL_HOME, DOORBELL));

        properties.put(ID_1003_CAM_222_UNREAD_COUNT, new DPProperty(int.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_1002_CAM_512UNREAD_COUNT_V2, new DPProperty(int.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_1001_CAM_505_UNREAD_COUNT, new DPProperty(int.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));

        properties.put(ID_701_SYS_PUSH_FLAG, new DPProperty(boolean.class));//set
        properties.put(606, new DPProperty(DpMsgDefine.DPShareItem.class));
        properties.put(ID_602_ACCOUNT_WONDERFUL_MSG, new DPProperty(DpMsgDefine.DPWonderItem.class));//set
        properties.put(ID_601_ACCOUNT_STATE, new DPProperty(DpMsgDefine.DPMineMesg.class));
        properties.put(ID_515_CAM_ObjectDetect, new DPProperty(int[].class, DPProperty.LEVEL_HOME, CAMERA, DOORBELL, PROPERTY));
        properties.put(ID_514_CAM_WARNINTERVAL, new DPProperty(int.class, DPProperty.LEVEL_HOME, CAMERA, DOORBELL, PROPERTY));
//        properties.put(ID_513_CAM_RESOLUTION, new DPProperty(int.class, CAMERA, DOORBELL));//清晰度,分辨率
        properties.put(ID_512_CAMERA_ALARM_MSG_V3, new DPProperty(DpMsgDefine.DPAlarm.class));//set
        properties.put(ID_511_CAMERAWARNANDWONDER, new DPProperty(long.class));//set
        properties.put(ID_510_CAMERA_COORDINATE, new DPProperty(DpMsgDefine.DpCoordinate.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_509_CAMERA_MOUNT_MODE, new DPProperty(String.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_508_CAMERA_STANDBY_FLAG, new DPProperty(DpMsgDefine.DPStandby.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_506_CAMERA_TIME_LAPSE_PHOTOGRAPHY, new DPProperty(DpMsgDefine.DPTimeLapse.class));
        properties.put(ID_505_CAMERA_ALARM_MSG, new DPProperty(DpMsgDefine.DPAlarm.class));//set
        properties.put(ID_504_CAMERA_ALARM_NOTIFICATION, new DPProperty(DpMsgDefine.DPNotificationInfo.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_503_CAMERA_ALARM_SENSITIVITY, new DPProperty(int.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_502_CAMERA_ALARM_INFO, new DPProperty(DpMsgDefine.DPAlarmInfo.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_501_CAMERA_ALARM_FLAG, new DPProperty(boolean.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_402_BELL_VOICE_MSG, new DPProperty(int.class, DPProperty.LEVEL_HOME, PROPERTY, DOORBELL));
        properties.put(ID_401_BELL_CALL_STATE, new DPProperty(DpMsgDefine.DPBellCallRecord.class));//set
        properties.put(ID_304_DEVICE_CAMERA_ROTATE, new DPProperty(int.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_303_DEVICE_AUTO_VIDEO_RECORD, new DPProperty(int.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
//        properties.put(ID_302_DEVICE_SPEAKER, new DPProperty(int.class, PROPERTY, CAMERA, DOORBELL));
//        properties.put(ID_301_DEVICE_MIC, new DPProperty(boolean.class, PROPERTY, CAMERA, DOORBELL));
        properties.put(228, new DPProperty(DpMsgDefine.DPBaseUpgradeStatus.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_223_MOBILE_NET, new DPProperty(int.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));//
        properties.put(ID_222_SDCARD_SUMMARY, new DPProperty(DpMsgDefine.DPSdcardSummary.class));//set
//        properties.put(ID_220_SDK_VERSION, new DPProperty(String.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
//        properties.put(ID_219_DEVICE_BIND_LOG, new DPProperty(DpMsgDefine.DPBindLog.class, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_218_DEVICE_FORMAT_SDCARD, new DPProperty(int.class, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_217_DEVICE_MOBILE_NET_PRIORITY, new DPProperty(boolean.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_216_DEVICE_VOLTAGE, new DPProperty(boolean.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
//        properties.put(ID_215_DEVICE_RTMP, new DPProperty(boolean.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA));
        properties.put(ID_214_DEVICE_TIME_ZONE, new DPProperty(DpMsgDefine.DPTimeZone.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
//        properties.put(ID_213_DEVICE_P2P_VERSION, new DPProperty(int.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
//        properties.put(ID_212_DEVICE_UPLOAD_LOG, new DPProperty(String.class, PROPERTY, CAMERA, DOORBELL));
//        properties.put(ID_211_APP_UPLOAD_LOG, new DPProperty(int.class, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_210_UP_TIME, new DPProperty(int.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_209_LED_INDICATOR, new DPProperty(boolean.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_208_DEVICE_SYS_VERSION, new DPProperty(String.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_207_DEVICE_VERSION, new DPProperty(String.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_206_BATTERY, new DPProperty(int.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_205_CHARGING, new DPProperty(boolean.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_204_SDCARD_STORAGE, new DPProperty(DpMsgDefine.DPSdStatus.class, PROPERTY, CAMERA, DOORBELL));
        //格式化响应.结构和204一样.
        properties.put(ID_203_SD_FORMAT_RSP, new DPProperty(DpMsgDefine.DPSdStatus.class, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_202_MAC, new DPProperty(String.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(ID_201_NET, new DPProperty(DpMsgDefine.DPNet.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
        properties.put(305, new DPProperty(DpMsgDefine.DPAutoRecordWatcher.class, DPProperty.LEVEL_HOME, PROPERTY, CAMERA, DOORBELL));
    }

    @Override
    public boolean accept(int pid, int msgId) {
        DPProperty property = properties.get(msgId);
        return property != null && property.accept(DPDevice.belong(pid));
    }

    @Override
    public <T extends DataPoint> T parser(int msgId, byte[] bytes, long version) {
        T result = null;
        try {
            DPProperty property = properties.get(msgId);
            if (property == null) return null;
            Object value = DpUtils.unpackData(bytes, property.type());
            if (!(value instanceof DataPoint)) {
                result = (T) new DpMsgDefine.DPPrimary(value);
            } else {
                result = (T) value;
            }
            result.setMsgId(msgId);
            result.setVersion(version);
        } catch (Exception e) {
            Log.d("parser", "parser:" + msgId + " " + e.getLocalizedMessage());
        }
        return result == null ? repair(msgId, bytes, version) : result;
    }


    private <T extends DataPoint> T repair(int msgId, byte[] bytes, long version) {
        T result = null;
        try {
            if (msgId == 204) {
                DpMsgDefine.DPSdStatusInt unpack = DpUtils.unpackData(bytes, DpMsgDefine.DPSdStatusInt.class);
                DpMsgDefine.DPSdStatus status = new DpMsgDefine.DPSdStatus();
                status.total = unpack.total;
                status.used = unpack.used;
                status.err = unpack.err;
                status.hasSdcard = unpack.hasSdcard == 1;
                result = (T) status;
                result.setMsgId(msgId);
                result.setVersion(version);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public ArrayList<JFGDPMsg> getQueryParameters(int pid) {
        ArrayList<JFGDPMsg> result = new ArrayList<>();
        DPProperty property;
        JFGDPMsg msg;
        for (int i = 0; i < properties.size(); i++) {
            property = properties.valueAt(i);
            if (property.accept(DPDevice.belong(pid))) {
                msg = new JFGDPMsg(properties.keyAt(i), 0);
                result.add(msg);
            }
        }
        return result;
    }

    @Override
    public ArrayList<JFGDPMsg> getQueryParameters(int pid, int level) {
        ArrayList<JFGDPMsg> result = new ArrayList<>();
        DPProperty property;
        JFGDPMsg msg;
        for (int i = 0; i < properties.size(); i++) {
            property = properties.valueAt(i);
            if (property.accept(DPDevice.belong(pid)) && property.getPropertyLevel() == level) {
                msg = new JFGDPMsg(properties.keyAt(i), 0);
                result.add(msg);
            }
        }
        return result;
    }

    @Override
    public ArrayList<JFGDPMsg> getAllQueryParameters() {
        return getQueryParameters(-1);
    }

    /**
     * @return true 说明是属性,false,说明不是属性,属性是唯一的
     * ,在数据库中每一个 msgId 的属性只能有一份,
     * 而一个非属性则能是多个的
     */
    @Override
    public boolean isProperty(int msgId) {
        DPProperty property = properties.get(msgId);
        return property != null && property.isProperty();
    }
}
