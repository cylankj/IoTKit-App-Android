package com.cylan.jiafeigou.entity;

import com.cylan.jiafeigou.entity.msg.MsgCidData;

import java.io.Serializable;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-12-10
 * Time: 16:58
 */

public class RelatedbellBean implements Serializable {
    private MsgCidData info;
    private boolean isChoose;

    public MsgCidData getInfo() {
        return info;
    }

    public void setInfo(MsgCidData info) {
        this.info = info;
    }

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }
}
