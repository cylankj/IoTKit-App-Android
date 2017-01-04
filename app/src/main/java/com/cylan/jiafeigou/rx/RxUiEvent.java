package com.cylan.jiafeigou.rx;

import com.cylan.jiafeigou.dp.DpMsgDefine;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public class RxUiEvent {

    public static final class BulkDeviceListReq {
        @Override
        public String toString() {
            return "BulkDeviceListReq";
        }
    }

    public static final class BulkDeviceListRsp {
        public ArrayList<DpMsgDefine.DpWrap> allDevices;

        @Override
        public String toString() {
            return "BulkDeviceListRsp{" +
                    "allDevices=" + allDevices +
                    '}';
        }
    }


    public static final class SingleDevice {
        public DpMsgDefine.DpWrap dpMsg;

        @Override
        public String toString() {
            return "SingleDevice{" +
                    "dpMsg=" + dpMsg +
                    '}';
        }
    }
}