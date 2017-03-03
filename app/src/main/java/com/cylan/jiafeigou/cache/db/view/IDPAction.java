package com.cylan.jiafeigou.cache.db.view;

import com.google.gson.Gson;

/**
 * Created by yanzhendong on 2017/3/3.
 */

public interface IDPAction {
    IDPAction DELETED = new DPDeleteAction();
    IDPAction SAVED = new DPSavedAction();
    IDPAction SHARED = new DPSharedAction();
    IDPAction QUERY = new DPQueryAction();

    String action();

    String ACTION();

    abstract class BaseDPAction implements IDPAction {
        public String action;
        private static Gson parser = new Gson();

        public BaseDPAction(String action) {
            this.action = action;
        }

        @Override
        public String action() {
            return parser.toJson(this);
        }

        @Override
        public String ACTION() {
            return action;
        }

        public static <T extends IDPAction> T $(String action, Class<T> clz) {
            return parser.fromJson(action, clz);
        }
    }

    class DPDeleteAction extends BaseDPAction {
        public DPDeleteAction() {
            super("DELETED");
        }
    }

    class DPSharedAction extends BaseDPAction {
        public int type;
        public int flag;

        public DPSharedAction(int type, int flag) {
            super("SHARED");
            this.type = type;
            this.flag = flag;
        }

        public DPSharedAction() {
            super("SHARED");
        }
    }

    class DPQueryAction extends BaseDPAction {
        public boolean asc = false;
        public int limit = 20;

        public DPQueryAction() {
            super("QUERY");
        }

        public DPQueryAction(boolean asc, int limit) {
            super("QUERY");
            this.asc = asc;
            this.limit = limit;
        }
    }

    class DPSavedAction extends BaseDPAction {

        public DPSavedAction() {
            super("SAVED");
        }
    }
}
