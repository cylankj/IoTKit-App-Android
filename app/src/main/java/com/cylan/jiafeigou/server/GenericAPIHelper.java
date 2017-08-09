package com.cylan.jiafeigou.server;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.pty.PropertiesLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanzhendong on 2017/8/9.
 */

public class GenericAPIHelper {

    private static GenericAPIHelper instance;

    public static GenericAPIHelper getInstance() {
        if (instance == null) {
            synchronized (GenericAPIHelper.class) {
                if (instance == null) {
                    instance = new GenericAPIHelper();
                }
            }
        }
        return instance;
    }

//        svSettingDeviceClearRecord.setVisibility(productProperty.isSerial("BELL", device.pid) ? View.VISIBLE : View.INVISIBLE);
//        svSettingDeviceWifi.setVisibility(productProperty.hasProperty(device.pid, "WIFI") ? View.VISIBLE : View.GONE);
//        svSettingSafeProtection.setVisibility(productProperty.hasProperty(device.pid, "PROTECTION") ? View.VISIBLE : View.GONE);
//        svSettingDeviceAutoRecord.setVisibility(productProperty.hasProperty(device.pid, "AUTORECORD") ? View.VISIBLE : View.GONE);
//        svSettingDevicePIR.setVisibility(productProperty.hasProperty(device.pid, "INFRAREDVISION") ? View.VISIBLE : View.GONE);
//        svSettingDeviceSDCard.setVisibility(productProperty.hasProperty(device.pid, "SD") ? View.VISIBLE : View.GONE);
//        sbtnSetting110v.setVisibility(productProperty.hasProperty(device.pid, "NTSC") ? View.VISIBLE : View.GONE);
//        svSettingDeviceRotate.setVisibility(productProperty.hasProperty(device.pid, "HANGUP") ? View.VISIBLE : View.GONE);
//        svSettingDeviceStandbyMode.setVisibility(productProperty.hasProperty(device.pid, "STANDBY") ? View.VISIBLE : View.GONE);
//        svSettingDeviceLedIndicator.setVisibility(productProperty.hasProperty(device.pid, "LED") ? View.VISIBLE : View.GONE);
////        svSettingDeviceMobileNetwork.setVisibility(productProperty.hasProperty(device.pid, "") ? View.VISIBLE : View.GONE);
//        svSettingDeviceSoftAp.setVisibility(productProperty.hasProperty(device.pid, "AP") ? View.VISIBLE : View.GONE);
//        svSettingDeviceWiredMode.setVisibility(productProperty.hasProperty(device.pid, "WIREDMODE") ? View.VISIBLE : View.GONE);
//        sbtnSettingSight.setVisibility(productProperty.hasProperty(device.pid, "VIEWANGLE") ? View.VISIBLE : View.GONE);

    public void getDeviceSetting(String uuid, boolean share) {
        PropertiesLoader propertiesLoader = PropertiesLoader.getInstance();
        int osType = propertiesLoader.getOSType(uuid);
        ArrayList<JFGDPMsg> queryParams = new ArrayList<>();

        if (propertiesLoader.hasProperty(osType, PropertyConstant.PROTECTION, share)) {
            //
            queryParams.add(new JFGDPMsg(DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, 0));
        }

        if (propertiesLoader.hasProperty(osType, PropertyConstant.AUTORECORD, share)) {
            queryParams.add(new JFGDPMsg(DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD, 0));
        }

        if (propertiesLoader.hasProperty(osType, PropertyConstant.INFRAREDVISION, share)) {
            // TODO: 2017/8/9 暂时不知道
        }

        if (propertiesLoader.hasProperty(osType, PropertyConstant.SD, share)) {
            queryParams.add(new JFGDPMsg(DpMsgMap.ID_204_SDCARD_STORAGE, 0));
        }

        if (propertiesLoader.hasProperty(osType, PropertyConstant.NTSC, share)) {
            queryParams.add(new JFGDPMsg(DpMsgMap.ID_216_DEVICE_VOLTAGE, 0));
        }




    }

    public void getHomePageList(List<String> uuids) {

    }

    public void getCameraMainInfo(String uuid) {

    }


}
