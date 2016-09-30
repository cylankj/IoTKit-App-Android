package com.cylan.jiafeigou.support.download.net;

import android.content.Context;

/**
 * Created by hunt on 16-4-26.
 */
public class NetConfig {

    public final static int TYPE_NONE = -1;
    public final static int TYPE_WIFI = 0;
    public final static int TYPE_MOBILE = 1;
    public final static int TYPE_ALL = 2;

    public static class Builder {
        Context context;
        private int allowNetType;

        public int getAllowNetType() {
            return allowNetType;
        }

        public Builder setAllowNetType(int allowNetType) {
            this.allowNetType = allowNetType;
            return this;
        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Context getContext() {
            return context;
        }
    }
}
