package com.cylan.jiafeigou.cache.db.view;

import com.google.gson.Gson;

/**
 * Created by yanzhendong on 2017/3/6.
 */

public interface DBOption {
    String option();

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
        public boolean asc;
        public int limit;

        public SingleQueryOption(boolean asc, int limit) {
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

}
