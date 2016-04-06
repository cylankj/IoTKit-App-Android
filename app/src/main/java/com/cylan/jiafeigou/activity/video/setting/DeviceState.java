package com.cylan.jiafeigou.activity.video.setting;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.engine.RequestMessage;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgClientMagGetInfo;
import com.cylan.jiafeigou.entity.msg.MsgStatusSdcardToClient;
import com.cylan.jiafeigou.utils.OEMConf;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.Utils;

import org.json.JSONArray;

import java.text.DecimalFormat;

public class DeviceState extends BaseActivity {
    protected MsgCidData mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mData = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        setContentView(R.layout.activity_device_msg);

        setTitle(R.string.EQUIPMENT_INFO);

        ((TextView) findViewById(R.id.device_code)).setText(mData.cid);
        ((TextView) findViewById(R.id.device_name)).setText(mData.mName());
        if (mData.os == Constants.OS_CAMERA_UCOS || mData.os == Constants.OS_CAMERA_UCOS_V2 || mData.os == Constants.OS_CAMERA_UCOS_V3) {

            findViewById(R.id.line1).setVisibility(View.GONE);
            findViewById(R.id.line3).setVisibility(View.GONE);
            findViewById(R.id.layout_device_mobilenet).setVisibility(View.GONE);
            findViewById(R.id.layout_device_dump).setVisibility(View.GONE);
        }

        if (!OEMConf.showModel()) {
            findViewById(R.id.layout_device_type).setVisibility(View.GONE);
            findViewById(R.id.device_type_line).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyApp.getIsLogin()) {
            if (mData.os == Constants.OS_MAGNET){
                MsgClientMagGetInfo getInfo = new MsgClientMagGetInfo("", mData.cid);
                JniPlay.SendBytes(getInfo.toBytes());
                Log.d("DeviceState", "info-->" + getInfo.toString());
            }else {
                JniPlay.SendBytes(RequestMessage.getDeviceInfo(mData.cid).toBytes());
            }
        }
    }


    void update(MsgStatusSdcardToClient mMsgStatusSdcardToClient) {
        try {
            ((TextView) findViewById(R.id.device_type)).setText(mMsgStatusSdcardToClient.model);
            int net = mMsgStatusSdcardToClient.net;
            String name = mMsgStatusSdcardToClient.name;
            if (net == mData.CID_NET_3G) {
                ((TextView) findViewById(R.id.device_wifi)).setText(getString(R.string.OFF));
                ((TextView) findViewById(R.id.device_mobilenet)).setText(name);
            } else if (net == mData.CID_NET_WIFI) {
                ((TextView) findViewById(R.id.device_wifi)).setText(name);
                ((TextView) findViewById(R.id.device_mobilenet)).setText(getString(R.string.OFF));
            } else {
                ((TextView) findViewById(R.id.device_wifi)).setText(getString(R.string.OFF));
                ((TextView) findViewById(R.id.device_mobilenet)).setText(getString(R.string.OFF));
            }
            ((TextView) findViewById(R.id.device_mac_address)).setText(mMsgStatusSdcardToClient.mac);

            if (mMsgStatusSdcardToClient.mac.equals("")) {
                String addrs = PreferenceUtil.getDeviceMacAddress(this);
                if (!addrs.equals("")) {
                    JSONArray ja = new JSONArray(addrs);
                    int len = ja.length();
                    for (int i = 0; i < len; i++) {
                        if (ja.getJSONObject(i).has(mData.cid)) {
                            ((TextView) findViewById(R.id.device_mac_address)).setText((String) ja.getJSONObject(i).get(mData.cid));
                            break;
                        }
                    }

                }
            }

            if (mData.os == Constants.OS_MAGNET){
                ((TextView) findViewById(R.id.device_dump)).setText( + (int) mMsgStatusSdcardToClient.battery + "%");
                ((TextView) findViewById(R.id.device_time)).setText(subTime(mMsgStatusSdcardToClient.uptime));
            }else {
                ((TextView) findViewById(R.id.device_dump)).setText((mMsgStatusSdcardToClient.power == 0 ? getString(R.string.CHARGING) : "") + (int) mMsgStatusSdcardToClient.battery + "%");
                ((TextView) findViewById(R.id.device_time)).setText(stringForTime((int) mMsgStatusSdcardToClient.uptime));
            }

            long totalStorage = mMsgStatusSdcardToClient.storage;
            long usedStorage = mMsgStatusSdcardToClient.used;
            float restStorage = ((float) totalStorage - usedStorage) / Utils.getGBUnit();
//            int rate = (int) (usedStorage * 100 / totalStorage);
            ((TextView) findViewById(R.id.device_canuse_memory)).setText(getString(R.string.REMAIN_SPACE,
                    new DecimalFormat("#0.0").format(restStorage)));


            ((TextView) findViewById(R.id.device_system_vision)).setText(mMsgStatusSdcardToClient.sys_version);
            ((TextView) findViewById(R.id.device_software_vision)).setText(mMsgStatusSdcardToClient.version);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        if (msgpackMsg.msgId == MsgpackMsg.CLIENT_STATUS_ACK) {
            MsgStatusSdcardToClient mMsgStatusSdcardToClient = (MsgStatusSdcardToClient) msgpackMsg;
            update(mMsgStatusSdcardToClient);
        }
    }

    private String stringForTime(int totalSeconds) {
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600 % 24;
        int dates = totalSeconds / 3600 / 24;
        return getString(R.string.STANBY_TIME, dates, hours, minutes);
    }

    private String subTime(long bindTime){
        long current = System.currentTimeMillis() / 1000;
        if (bindTime == 0)
            return stringForTime(0);
        long runningTime = current - bindTime;
        return stringForTime((int)runningTime);
    }

}
