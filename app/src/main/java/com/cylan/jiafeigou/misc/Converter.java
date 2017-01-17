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

//    public static BeanCamInfo convert(DeviceBean bean) {
//        BaseBean baseBean = new BaseBean();
//        baseBean.alias = bean.alias;
//        baseBean.shareAccount = bean.shareAccount;
//        baseBean.sn = bean.sn;
//        baseBean.uuid = bean.uuid;
//        baseBean.pid = bean.pid;
//        BeanCamInfo info = new BeanCamInfo();
//        info.convert(baseBean, bean.dataList);
//        return info;
//    }

    public static DpMsgDefine.DpMsg convert(Object o, int msgId, long version) {
        DpMsgDefine.DpMsg base = new DpMsgDefine.DpMsg();
        base.version = version;
        base.msgId = msgId;
        base.o = o;
        return base;
    }

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
                bean.alarmMsg = (DpMsgDefine.DPAlarm) base.getValue();
            } else if (base.getId() == DpMsgMap.ID_222_SDCARD_SUMMARY) {
                bean.content = (DpMsgDefine.DPSdcardSummary) base.getValue();
            }
            beanArrayList.add(bean);
        }
        return beanArrayList;
    }

}