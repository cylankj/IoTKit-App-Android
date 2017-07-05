package com.cylan.jiafeigou.base.view;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.dp.DataPoint;

import java.util.ArrayList;

/**
 * Created by yanzhendong on 2017/3/25.
 */

public interface IPropertyParser {

    boolean accept(int pid, int msgId);

    <T extends DataPoint> T parser(int msgId, byte[] bytes, long version);

    ArrayList<JFGDPMsg> getQueryParameters(int pid);

    ArrayList<JFGDPMsg> getQueryParameters(int pid, int level);

    ArrayList<JFGDPMsg> getAllQueryParameters();

    boolean isProperty(int msgId);
}
