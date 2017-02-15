package com.cylan.jiafeigou.misc;

import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-11-28.
 */

public class Converter {

    public static ArrayList<CamMessageBean> convert(String uuid, ArrayList<BaseValue> baseValueList) {
        ArrayList<CamMessageBean> beanArrayList = new ArrayList<>();
        if (baseValueList == null)
            return beanArrayList;
        for (BaseValue base : baseValueList) {
            CamMessageBean bean = new CamMessageBean();
            bean.time = base.getVersion();
            bean.id = base.getId();
            bean.version = base.getVersion();
            if (base.getId() == DpMsgMap.ID_505_CAMERA_ALARM_MSG && base.getValue() != null) {
                bean.alarmMsg = base.getValue();
            } else if (base.getId() == DpMsgMap.ID_222_SDCARD_SUMMARY) {
                bean.content = base.getValue();
            }
            beanArrayList.add(bean);
        }
        return beanArrayList;
    }

}