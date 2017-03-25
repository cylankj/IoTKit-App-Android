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

    class MultiQueryOption extends BaseDBOption {
        public long timeStart;
        public long timeEnd;
        public boolean asc;//请求服务器数据的时候使用，向前查，向后查。

        public MultiQueryOption(long timeStart, long timeEnd, boolean asc) {
            this.timeStart = timeStart;
            this.timeEnd = timeEnd;
            this.asc = asc;
        }

        @Override
        public String toString() {
            return "MultiQueryOption{" +
                    "timeStart=" + timeStart +
                    ", timeEnd=" + timeEnd +
                    '}';
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
