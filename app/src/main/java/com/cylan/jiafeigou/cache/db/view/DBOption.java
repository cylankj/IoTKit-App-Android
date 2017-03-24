package com.cylan.jiafeigou.cache.db.view;

import com.google.gson.Gson;

/**
 * Created by yanzhendong on 2017/3/6.
 */

public interface DBOption {
    String option();

    DBOption NO_OPTION = new BaseDBOption();

    class BaseDBOption implements DBOption {
        protected static Gson parser = new Gson();

        @Override
        public String option() {
            return parser.toJson(this);
        }

        public static <T extends DBOption> T option(String option, Class<T> clz) {
            return parser.fromJson(option, clz);
        }


    }

    class SingleQueryOption extends BaseDBOption {
        public int type;//查询方式:0:robotGetData,1:robotGetDataByTime ,default:0
        public boolean asc;
        public int limit;

        public static final SingleQueryOption ONE_BY_TIME = new SingleQueryOption(1, false, 1);
        public static final SingleQueryOption DESC_20_LIMIT = new SingleQueryOption(false, 20);
        public static final SingleQueryOption ASC_20_LIMIT = new SingleQueryOption(true, 20);

        public SingleQueryOption(boolean asc, int limit) {
            this(0, asc, limit);
        }

        public SingleQueryOption(int type, boolean asc, int limit) {
            this.type = type;
            this.asc = asc;
            this.limit = limit;
        }
    }

    class SingleSharedOption extends BaseDBOption {
        public int type;
        public int flag;

        public SingleSharedOption(int type, int flag) {
            this.type = type;
            this.flag = flag;
        }
    }

    class RawDeviceOrderOption extends BaseDBOption {
        public int rawDeviceOrder;

        public RawDeviceOrderOption(int order) {
            this.rawDeviceOrder = order;
        }
    }
}
