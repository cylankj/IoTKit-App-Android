package com.cylan.jiafeigou.misc;

import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeWonderfulPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.utils.RandomUtils;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-11-28.
 */

public class Convertor {

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

    public static ArrayList<CamMessageBean> convert(String uuid, ArrayList<DpMsgDefine.DpMsg> dpMsgList) {
        ArrayList<CamMessageBean> beanArrayList = new ArrayList<>();
        if (dpMsgList == null)
            return beanArrayList;
        for (DpMsgDefine.DpMsg dpMsg : dpMsgList) {
            CamMessageBean bean = new CamMessageBean();
            bean.time = dpMsg.version;
            DpMsgDefine.AlarmMsg msg = (DpMsgDefine.AlarmMsg) dpMsg.o;
            bean.urlList = getUrlList(uuid,
                    dpMsg.version,
                    msg.type,
                    msg.fileIndex);
            bean.viewType = bean.urlList.size() > 0 ? 1 : 0;
            beanArrayList.add(bean);
        }
        return beanArrayList;
    }

    private static ArrayList<String> getUrlList(String uuid, long time, int type, int index) {
        ArrayList<String> list = new ArrayList<>();
        int randomCount = RandomUtils.getRandom(7);
        for (int i = 0; i < 3; i++) {
            if ((randomCount >> i & 0x01) == 1) {
                StringBuilder builder = new StringBuilder();
                builder.append(time)
                        .append("_")
                        .append(i + 1)
                        .append(".jpg");
//                String url = JfgCmdInsurance.getCmd().getCloudUrlByType(JfgEnum.JFG_URL.WARNING, type, builder.toString(), uuid);
                int len = HomeWonderfulPresenterImpl.pics.length;
                String url = HomeWonderfulPresenterImpl.pics[RandomUtils.getRandom(len)];
                list.add(url);
            }
        }
        return list;
    }

}