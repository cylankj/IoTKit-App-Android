package com.cylan.jiafeigou.rx;

import com.cylan.jiafeigou.dp.DpMsgDefine;

import java.util.List;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public class RxUiEvent {

    public static final class QueryBulkDevice {
    }

    public static final class BulkDeviceList {
        public List<DpMsgDefine.DpWrap> allDevices;
    }

    @Deprecated//不应该再使用了,sticky Event只是根据 ClassType来区分,所有非常容易覆盖.
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
