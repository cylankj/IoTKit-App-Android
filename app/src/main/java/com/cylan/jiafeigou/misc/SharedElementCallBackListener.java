package com.cylan.jiafeigou.misc;

import android.app.SharedElementCallback;
import android.view.View;

import java.util.List;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-9-7.
 */
public interface SharedElementCallBackListener {
    void onSharedElementCallBack(List<String> names, Map<String, View> sharedElements);

    void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots);

    void onSharedElementArrived(List<String> sharedElementNames, List<View> sharedElements, SharedElementCallback.OnSharedElementsReadyListener listener);

    void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots);
}
