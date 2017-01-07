package com.cylan.jiafeigou.provider;


import android.support.v4.util.LongSparseArray;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.view.JFGSourceManager;

import java.util.List;

/**
 * Created by yzd on 16-12-28.
 */

public class DataSourceManager implements JFGSourceManager {

    //缓存所有的dp消息
    private LongSparseArray<List<JFGDPMsg>> mDataSourceCache = new LongSparseArray<>();


}
