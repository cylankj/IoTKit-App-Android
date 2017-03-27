package com.cylan.jiafeigou.base.module;

import android.util.SparseArray;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;

import java.util.ArrayList;

import static com.cylan.jiafeigou.base.module.DPDevice.ACCOUNT;
import static com.cylan.jiafeigou.base.module.DPDevice.CAMERA;
import static com.cylan.jiafeigou.base.module.DPDevice.DOORBELL;

/**
 * Created by yanzhendong on 2017/3/25.
 */

public class BasePropertyParser implements IPropertyParser {
    private static BasePropertyParser instance;
    private SparseArray<DPProperty> properties = new SparseArray<>();

    private BasePropertyParser() {
        init();
    }


    private void init() {
        properties.put(701, new DPProperty(Boolean.class, CAMERA));
        properties.put(602, new DPProperty(DpMsgDefine.DPWonderItem.class, ACCOUNT));
        properties.put(601, new DPProperty(String.class));
        properties.put(512, new DPProperty(DpMsgDefine.DPAlarm.class, CAMERA));
        properties.put(510, new DPProperty(Boolean.class, CAMERA));
        properties.put(509, new DPProperty(Integer.class, CAMERA));
        properties.put(508, new DPProperty(Boolean.class, CAMERA));
        properties.put(506, new DPProperty(DpMsgDefine.DPTimeLapse.class));
        properties.put(505, new DPProperty(DpMsgDefine.DPAlarm.class, CAMERA));
        properties.put(504, new DPProperty(DpMsgDefine.DPNotificationInfo.class, CAMERA));
        properties.put(503, new DPProperty(Integer.class));
        properties.put(502, new DPProperty(DpMsgDefine.DPAlarmInfo.class, CAMERA));
        properties.put(501, new DPProperty(Boolean.class, CAMERA));
        properties.put(402, new DPProperty(Integer.class, DOORBELL));
        properties.put(401, new DPProperty(DpMsgDefine.DPBellCallRecord.class, DOORBELL));
        properties.put(304, new DPProperty(Integer.class, CAMERA, DOORBELL));
        properties.put(303, new DPProperty(Integer.class, CAMERA));
        properties.put(302, new DPProperty(Integer.class, CAMERA, DOORBELL));
        properties.put(301, new DPProperty(Boolean.class, CAMERA, DOORBELL));
        properties.put(220, new DPProperty(String.class, CAMERA, DOORBELL));
        properties.put(219, new DPProperty(DpMsgDefine.DPBindLog.class, CAMERA, DOORBELL));
        properties.put(218, new DPProperty(DpMsgDefine.DpSdcardFormatRsp.class, CAMERA));
        properties.put(217, new DPProperty(Boolean.class, CAMERA));
        properties.put(216, new DPProperty(Boolean.class, CAMERA, DOORBELL));
        properties.put(215, new DPProperty(Boolean.class, CAMERA));
        properties.put(214, new DPProperty(DpMsgDefine.DPTimeZone.class, CAMERA, DOORBELL));
        properties.put(213, new DPProperty(Integer.class, CAMERA, DOORBELL));
        properties.put(212, new DPProperty(String.class, CAMERA, DOORBELL));
        properties.put(211, new DPProperty(Integer.class, CAMERA, DOORBELL));
        properties.put(210, new DPProperty(Integer.class, CAMERA, DOORBELL));
        properties.put(209, new DPProperty(Boolean.class, CAMERA, DOORBELL));
        properties.put(208, new DPProperty(String.class, CAMERA, DOORBELL));
        properties.put(207, new DPProperty(String.class, CAMERA, DOORBELL));
        properties.put(206, new DPProperty(Integer.class, CAMERA));
        properties.put(222, new DPProperty(DpMsgDefine.DPSdcardSummary.class, CAMERA));
        properties.put(206, new DPProperty(Integer.class, CAMERA, DOORBELL));
        properties.put(205, new DPProperty(Boolean.class, CAMERA, DOORBELL));
        properties.put(204, new DPProperty(DpMsgDefine.DPSdStatus.class, CAMERA));
        properties.put(202, new DPProperty(String.class, CAMERA, DOORBELL));
        properties.put(201, new DPProperty(DpMsgDefine.DPNet.class, CAMERA, DOORBELL));
    }

    public static BasePropertyParser getInstance() {
        if (instance == null) {
            synchronized (BasePropertyParser.class) {
                if (instance == null) {
                    instance = new BasePropertyParser();
                }
            }
        }
        return instance;
    }


    @Override
    public boolean accept(int pid, int msgId) {
        DPProperty property = properties.get(msgId);
        return property != null && property.accept(DPDevice.belong(pid));
    }

    @Override
    public <T extends DataPoint> T parser(int msgId, byte[] bytes, long version) {
        T result;
        try {
            DPProperty property = properties.get(msgId);
            if (property == null) return null;
            Object value = DpUtils.unpackData(bytes, property.type());
            if (!(value instanceof DataPoint)) {
                result = (T) new DpMsgDefine.DPPrimary(value);
            } else {
                result = (T) value;
            }
            result.msgId = msgId;
            result.version = version;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    /**
     * @return true 说明是属性,false,说明不是属性,属性是唯一的
     * ,在数据库中每一个 msgId 的属性只能有一份,
     * 而一个非属性则能是多个的
     */
    @Override
    public boolean isProperty(int msgId) {
        return false;
    }
}
