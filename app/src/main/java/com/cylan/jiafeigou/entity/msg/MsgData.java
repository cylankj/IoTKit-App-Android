package com.cylan.jiafeigou.entity.msg;

import com.cylan.jiafeigou.R;
import com.cylan.support.DswLog;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.utils.Utils;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:59
 */
@Message
public class MsgData implements Serializable, Comparable<MsgData> {
    @Index(0)
    public int push_type;
    @Index(1)
    public String cid;
    @Index(2)
    public String account;
    @Index(3)
    public long time;
    @Index(4)
    public String version;
    @Index(5)
    public String binding_account;
    @Index(6)
    public int err;
    @Index(7)
    public String alias;
    @Index(8)
    public int count;
    @Index(9)
    public int type;
    @Index(10)
    public String title;
    @Index(11)
    public List<String> urllist;
    @Index(12)
    public String share_account;
    @Index(13)
    public long video_time;
    @Index(14)
    public int os;
    @Index(15)
    public float methanal; //甲醛
    @Index(16)
    public float pm10; //pm10
    @Index(17)
    public float pm25;
    @Index(18)
    public int temp; //温度
    @Index(19)
    public float humi; //湿度

    @Ignore
    @Override
    public int compareTo(MsgData another) {
        return this.time < another.time ? 1 : -1;
    }

    @Ignore
    public String getContent(){
        String content = null;
        switch (push_type) {
            case ClientConstants.PUSH_TYPE_WARN:
                content = MyApp.getContext().getString(R.string.MSG_WARNING);
                break;

            case ClientConstants.PUSH_TYPE_TEMP_HUMI:
                break;
            case ClientConstants.PUSH_TYPE_WARN_ON:
                content = MyApp.getContext().getString(R.string.MSG_WARN_ON, "");
                break;
            case ClientConstants.PUSH_TYPE_HELLO:
                content = MyApp.getContext().getString((os == com.cylan.publicApi.Constants.OS_DOOR_BELL) ? R.string.DOOR_BIND : R.string.MSG_BIND);
                break;
            case ClientConstants.PUSH_TYPE_WARN_OFF:
                content = MyApp.getContext().getString(R.string.MSG_WARN_OFF, "");
                break;

            case ClientConstants.PUSH_TYPE_SYSTEM:
                break;

            case ClientConstants.PUSH_TYPE_LOW_BATTERY:
                content = MyApp.getContext().getString(R.string.MSG_LOW_BATTERY, "");
                break;
            case ClientConstants.PUSH_TYPE_SDCARD_OFF:
                content = MyApp.getContext().getString(R.string.MSG_SD_OFF, "");
                break;
            case ClientConstants.PUSH_TYPE_UNHELLO:
                content = String.format(MyApp.getContext().getString(R.string.MSG_UNBIND), cid);
                break;
            case ClientConstants.PUSH_TYPE_NEW_VERSION:

                content = String.format(MyApp.getContext().getString(R.string.MSG_SYSTEM_UPGRADE), version);

                break;
            case ClientConstants.PUSH_TYPE_WARN_REPORT:

//                String time = mSimpleDateFormat1.format(new Date(getReport_time() * 1000));
//                content = String.format((time + ctx.getString(R.string.day_report)), getReport_num());

                break;
            case ClientConstants.PUSH_TYPE_SDCARD_ON:
                content = MyApp.getContext().getString(err== 0 ? R.string.MSG_SD_ON : R.string.MSG_SD_ON_1);
                break;
            case ClientConstants.PUSH_TYPE_REBIND:
                content = String.format(MyApp.getContext().getString(R.string.MSG_REBIND), Utils.phoneNumchange(binding_account));
                break;
            case ClientConstants.PUSH_TYPE_SHARE:
                content = String.format(MyApp.getContext().getString(R.string.MSG_SHARE), Utils.phoneNumchange(share_account));
                break;
            case ClientConstants.PUSH_TYPE_UNSHARE:
                content = String.format(MyApp.getContext().getString(R.string.MSG_UNSHARE), Utils.phoneNumchange(share_account));
                break;
            case ClientConstants.PUSH_TYPE_MAGNET_ON:
//                content = String.format(ctx.getString(R.string.EFAMILY_MAGNET_ON), getAlias());
                break;
            case ClientConstants.PUSH_TYPE_MAGNET_OFF:
//                content = String.format(ctx.getString(R.string.EFAMILY_MAGNET_OFF), getAlias());
                break;
            case ClientConstants.PUSH_TYPE_IR:
//                content = String.format(ctx.getString(R.string.push_type_ir), getAlias());
                break;
            case ClientConstants.PUSH_TYPE_AIR_DETECTOR:
                //content = String.format(ctx.getString(R.string.push_type_ir_off), getAlias());
                break;
        }

        return content;
    }


    @Ignore
    public static List<MsgData> parseMsgSysData(List<MsgSystemData> array) {
        List<MsgData> list = new ArrayList<>();
        try {
            for (int i = 0; i < array.size(); i++) {
                MsgData info = new MsgData();
                info.time=array.get(i).time;//
                info.push_type=array.get(i).push_type;//
                info.title=array.get(i).title;//
                info.os=array.get(i).os;//
                info.systemContent=array.get(i).cnt;
                list.add(info);
            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
        return list;
    }

    @Ignore
    public String systemContent;

}
