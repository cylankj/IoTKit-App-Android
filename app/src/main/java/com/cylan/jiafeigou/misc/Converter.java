package com.cylan.jiafeigou.misc;

import com.cylan.entity.JfgEnum;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-11-28.
 */

public class Converter {

    public static BeanCamInfo convert(DeviceBean bean) {
        BaseBean baseBean = new BaseBean();
        baseBean.alias = bean.alias;
        baseBean.shareAccount = bean.shareAccount;
        baseBean.sn = bean.sn;
        baseBean.uuid = bean.uuid;
        baseBean.pid = bean.pid;
        BeanCamInfo info = new BeanCamInfo();
        info.convert(baseBean, bean.dataList);
        return info;
    }

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
                DpMsgDefine.AlarmMsg msg = (DpMsgDefine.AlarmMsg) base.getValue();
                bean.urlList = getUrlList(uuid,
                        msg.time,
                        msg.type,
                        msg.fileIndex);
                bean.viewType = bean.urlList.size() > 0 ? 1 : 0;
            } else if (base.getId() == DpMsgMap.ID_222_SDCARD_SUMMARY) {
                bean.content = (DpMsgDefine.SdcardSummary) base.getValue();
            }
            beanArrayList.add(bean);
        }
        return beanArrayList;
    }

    private static ArrayList<String> getUrlList(String uuid, long time, int type, int index) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if ((index >> i & 0x01) == 1) {
                StringBuilder builder = new StringBuilder();
                builder.append(time)
                        .append("_")
                        .append(i + 1)
                        .append(".jpg");
                try {
                    String url = JfgCmdInsurance.getCmd().getCloudUrlByType(JfgEnum.JFG_URL.WARNING,
                            type, builder.toString(), uuid);
                    list.add(url);
                } catch (JfgException e) {
                    AppLogger.e(String.format("err:%s", e.getLocalizedMessage()));
                }
            }
        }
        return list;
    }

}