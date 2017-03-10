package com.cylan.jiafeigou.misc;

import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-11-28.
 */

public class Converter {

    public static ArrayList<CamMessageBean> convert(ArrayList<DataPoint> baseValueList, int regionType) {
        ArrayList<CamMessageBean> beanArrayList = new ArrayList<>();
        if (baseValueList == null)
            return beanArrayList;
        for (DataPoint base : baseValueList) {
            CamMessageBean bean = new CamMessageBean();
            bean.time = base.dpMsgVersion;
            bean.id = base.dpMsgId;
            bean.version = base.dpMsgVersion;
            bean.regionType = regionType;
            if (base.dpMsgId == DpMsgMap.ID_505_CAMERA_ALARM_MSG) {
                bean.alarmMsg = (DpMsgDefine.DPAlarm) base;
            } else if (base.dpMsgId == DpMsgMap.ID_222_SDCARD_SUMMARY) {
                bean.sdcardSummary = (DpMsgDefine.DPSdcardSummary) base;
            }
            beanArrayList.add(bean);
        }
        return beanArrayList;
    }

}