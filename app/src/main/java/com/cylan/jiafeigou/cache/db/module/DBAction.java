package com.cylan.jiafeigou.cache.db.module;

import static com.cylan.jiafeigou.cache.db.module.DBAction.Kind.KIND_STATE;
import static com.cylan.jiafeigou.cache.db.module.DBAction.Kind.KIND_TAG;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public enum DBAction {
    /**
     * TAG Action
     **/
    SAVED(KIND_TAG),
    DELETED(KIND_TAG),
    SHARED(KIND_TAG),

    /**
     * STATE Action
     **/
    SUCCESS(KIND_STATE),
    NOT_CONFIRM(KIND_STATE);
    private Kind kind;

    public Kind kind() {
        return kind;
    }

    DBAction(Kind kind) {
        this.kind = kind;
    }

    public enum Kind {
        KIND_TAG, KIND_STATE;
    }
}
