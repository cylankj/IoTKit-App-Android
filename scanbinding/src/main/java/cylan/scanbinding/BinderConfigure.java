package cylan.scanbinding;

import android.content.Context;

/**
 * Created by hunt on 16-4-6.
 */
public class BinderConfigure {

    private Builder builder;

    private BinderConfigure(Builder builder) {
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        /**
         * 开始扫描,3s内没有结果返回.则关闭wifi,再打开.
         */
        private int scanTimeOut = 3000;

        /**
         * 尝试次数
         */
        private int retryCount = 0;

        private Context context;

        private Builder(Context context) {
            this.context = context;
        }

        public BinderConfigure build() {
            return new BinderConfigure(this);
        }


        public Context getContext() {
            return context;
        }


        public int getRetryCount() {
            return retryCount;
        }

        public int getScanTimeOut() {
            return scanTimeOut;
        }


        public Builder setRetryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder setScanTimeOut(int timeOut) {
            this.scanTimeOut = timeOut;
            return this;
        }
    }
}
