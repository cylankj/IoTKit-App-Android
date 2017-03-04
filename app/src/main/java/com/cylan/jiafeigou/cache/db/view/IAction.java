package com.cylan.jiafeigou.cache.db.view;

import com.google.gson.Gson;

/**
 * Created by yanzhendong on 2017/3/4.
 */

public interface IAction {
    String action();

    String ACTION();

    IAction DELETED = new DeleteAction();
    IAction SAVED = new SavedAction();

    OP OP();

    enum OP {
        BETWEEN, EQ, GE, GT, IN, IS_NOTNULL, IS_NULL, LE, LIKE, LT, NOT_EQ, NOT_IN
    }

    class BaseAction implements IDPAction {
        protected String action;
        private static Gson parser = new Gson();

        public BaseAction(String action) {
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

        @Override
        public OP OP() {
            return OP.EQ;
        }

        public static <T extends IDPAction> T $(String action, Class<T> clz) {
            return parser.fromJson(action, clz);
        }
    }

    class SavedAction extends BaseAction {

        public SavedAction() {
            super("SAVED");
        }
    }

    class DeleteAction extends BaseAction {
        public DeleteAction() {
            super("DELETED");
        }
    }
}
