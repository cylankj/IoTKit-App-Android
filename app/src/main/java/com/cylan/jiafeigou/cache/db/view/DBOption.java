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
        public static final SingleQueryOption DESC_15_LIMIT = new SingleQueryOption(false, 15);

        public SingleQueryOption(boolean asc, int limit) {
            this(0, asc, limit);
        }

        public SingleQueryOption(int type, boolean asc, int limit) {
            this.type = type;
            this.asc = asc;
            this.limit = limit;
        }
    }

    final class SimpleMultiDpQueryOption extends BaseDBOption {
        public int limit = 1;
        public boolean asc;

        public SimpleMultiDpQueryOption(int limit, boolean asc) {
            this.limit = limit;
            this.asc = asc;
        }
    }

    final class UpdateOption extends BaseDBOption {

    }

    class MultiQueryOption extends BaseDBOption {
        public long timeStart;
        public boolean asc;//请求服务器数据的时候使用，向前查，向后查。
        public boolean isMaxTime;
        public boolean useMaxLimit = true;

        public MultiQueryOption(long timeStart, boolean asc, boolean isMaxTime) {
            this.timeStart = timeStart;
            this.asc = asc;
            this.isMaxTime = isMaxTime;
        }

        public MultiQueryOption(long timeStart, boolean asc, boolean isMaxTime, boolean useMaxLimit) {
            this.timeStart = timeStart;
            this.asc = asc;
            this.isMaxTime = isMaxTime;
            this.useMaxLimit = useMaxLimit;
        }

        @Override
        public String toString() {
            return "MultiQueryOption{" +
                    "timeStart=" + timeStart +
                    ", asc=" + asc +
                    ", isMaxTime=" + isMaxTime +
                    '}';
        }
    }

    class SingleSharedOption extends BaseDBOption {
        public int type;
        public int flag;
        public String filePath;

        public SingleSharedOption(int type, int flag, String filePath) {
            this.type = type;
            this.flag = flag;
            this.filePath = filePath;
        }
    }

    class DeviceOption extends BaseDBOption {
        public int rawDeviceOrder;
        public long lastLowBatteryTime;

        public DeviceOption(int order) {
            this.rawDeviceOrder = order;
        }
    }

    class CamMultiDateOption extends BaseDBOption {
        public int days;

        public CamMultiDateOption(int days) {
            this.days = days;
        }

        public static final CamMultiDateOption CAMERA_15_DAYS = new CamMultiDateOption(15);

        public static final CamMultiDateOption BELL_7_DAYS = new CamMultiDateOption(7);

        public static final CamMultiDateOption CAMERA_FACE_ANY_DAYS = new CamMultiDateOption(-1);
    }
}
