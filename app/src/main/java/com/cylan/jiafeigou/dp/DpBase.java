package com.cylan.jiafeigou.dp;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.n.mvp.model.param.BaseParam;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-11-28.
 */

public abstract class DpBase<T extends BaseParam> implements IParser {


    protected T parameters;

    protected abstract ArrayList<JFGDPMsg> getParameters();
}
