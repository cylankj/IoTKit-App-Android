package com.cylan.jiafeigou.rx;

import com.cylan.jiafeigou.dp.DpMsgDefine;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public class RxUiEvent {

    public static final class QueryBulkDevice {
    }

    public static final class BulkDeviceList {
        public ArrayList<DpMsgDefine.DpWrap> allDevices;

        @Override
        public String toString() {
            return "BulkDeviceList{" +
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