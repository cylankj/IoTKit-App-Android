package com.cylan.jiafeigou.misc;

import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-11-28.
 */

public class Converter {

    public static ArrayList<CamMessageBean> convert(String uuid, ArrayList<DataPoint> baseValueList) {
        ArrayList<CamMessageBean> beanArrayList = new ArrayList<>();
        if (baseValueList == null)
            return beanArrayList;
        for (DataPoint base : baseValueList) {
            CamMessageBean bean = new CamMessageBean();
            bean.time = base.version;
            bean.id = base.id;
            bean.version = base.version;
            if (base.id == DpMsgMap.ID_505_CAMERA_ALARM_MSG && base.getValue(DpMsgMap.ID_505_CAMERA_ALARM_MSG) != null) {
                bean.alarmMsg = base.getValue(DpMsgMap.ID_505_CAMERA_ALARM_MSG);
            } else if (base.id == DpMsgMap.ID_222_SDCARD_SUMMARY) {
                bean.sdcardSummary = base.getValue(DpMsgMap.ID_222_SDCARD_SUMMARY);
            }
            beanArrayList.add(bean);
        }
        return beanArrayList;
    }

}