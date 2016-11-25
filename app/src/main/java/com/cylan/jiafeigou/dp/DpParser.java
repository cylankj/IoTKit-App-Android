package com.cylan.jiafeigou.dp;

import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.List;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-11-8.
 * dp数据解析器
 */

public class DpParser {
    private static final String TAG = "DpParser";

    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private static DpParser dpParser;

    private IParser deviceDpParser;

    private DpParser() {
        deviceDpParser = DpDeviceAssembler.getInstance();
    }

    public static DpParser getDpParser() {
        if (dpParser == null) {
            dpParser = new DpParser();
        }
        return dpParser;
    }

    public void registerDpParser() {
        AppLogger.i("DpParser: registerDpParser");
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
        }
        compositeSubscription = new CompositeSubscription(deviceDpParser.register());
    }


    public void unregisterDpParser() {
        AppLogger.e("DpParser: unregisterDpParser");
        if (compositeSubscription != null)
            compositeSubscription.unsubscribe();
    }


}
