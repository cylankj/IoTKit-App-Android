package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/6.
 */

public enum DBAction {
    SAVED(), DELETED(), SHARED(), AVAILABLE(OP.NOT_EQ, DELETED.name()), QUERY();
    private OP op;//op 只是在查询时有用,并不会保存到数据库
    private String action;

    DBAction(OP op, String action) {
        this.op = op;
        this.action = action;
    }

    DBAction() {
        this.op = OP.EQ;
        this.action = name();
    }

    public OP op() {
        return this.op;
    }

    public String action() {
        return this.action;
    }

    public enum OP {
        BETWEEN, EQ, GE, GT, IN, IS_NOTNULL, IS_NULL, LE, LIKE, LT, NOT_EQ, NOT_IN
    }
}
