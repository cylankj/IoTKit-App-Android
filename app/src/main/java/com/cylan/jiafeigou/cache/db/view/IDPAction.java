package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/3.
 */

public interface IDPAction extends IAction {
    IDPAction SHARED = new DPSharedAction();
    IDPAction QUERY = new DPQueryAction();
    IDPAction AVAILABLE = new DPAvaliabeAction();

    class DPSharedAction extends BaseAction {
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

    class DPQueryAction extends BaseAction {
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

    class DPAvaliabeAction extends BaseAction {

        public DPAvaliabeAction() {
            super("DELETED");
        }

        @Override
        public OP OP() {
            return OP.NOT_EQ;
        }
    }
}
