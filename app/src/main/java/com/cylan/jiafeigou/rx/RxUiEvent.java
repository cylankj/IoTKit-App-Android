package com.cylan.jiafeigou.rx;

import com.cylan.entity.jniCall.JFGDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public class RxUiEvent {

    public static final class QueryBulkDevice {
    }

    public static final class BulkDeviceList {
        public List<JFGDevice> bulkList = new ArrayList<>();
    }
}
