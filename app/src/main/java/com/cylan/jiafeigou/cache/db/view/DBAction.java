package com.cylan.jiafeigou.cache.db.view;

import android.text.TextUtils;

/**
 * Created by yanzhendong on 2017/3/6.
 */

public enum DBAction {
    SAVED, DELETED, SHARED, QUERY, CAM_MULTI_QUERY, SIMPLE_MULTI_QUERY, CAM_DATE_QUERY, CLEARED, MULTI_UPDATE, AVAILABLE(OP.NOT_EQS, DELETED.name() + "," + CLEARED.name()) {
        @Override
        public boolean accept(DBAction action) {
            String[] actions = action().split(",");
            for (String s : actions) {
                if (TextUtils.equals(action.action, s)) {
                    return false;
                }
            }
            return true;
        }
    }, UNBIND;
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

    public boolean accept(DBAction action) {
        return action == this;
    }

    public OP op() {
        return this.op;
    }

    public String action() {
        return this.action;
    }

    public enum OP {
        BETWEEN, EQ, GE, GT, IN, IS_NOTNULL, IS_NULL, LE, LIKE, LT, NOT_EQ, NOT_IN, NOT_EQS
    }
}
