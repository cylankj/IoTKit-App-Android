package cylan.scanbinding;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hunt on 16-4-6.
 */
public class ScanConfigure {

    private Builder builder;

    private ScanConfigure(Builder builder) {
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
         * 字符过滤器
         */
        private List<String> filterList;
        /**
         * 开始扫描,3s内没有结果返回.则关闭wifi,再打开.
         */
        private int scanTimeOut = 3000;

        /**
         * 尝试次数
         */
        private int retryCount = 0;

        private Context context;
        /**
         * 关闭再打开wifi
         */
        @Deprecated
        private boolean turnOffWifiBeforeScan = false;

        private Builder(Context context) {
            this.context = context;
        }

        public ScanConfigure build() {
            return new ScanConfigure(this);
        }


        public Context getContext() {
            return context;
        }

        public List<String> getFilterList() {
            return filterList;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public int getScanTimeOut() {
            return scanTimeOut;
        }

        @Deprecated
        public boolean isTurnOffWifiBeforeScan() {
            return turnOffWifiBeforeScan;
        }

        public Builder appendFilter(String filter) {
            if (TextUtils.isEmpty(filter)) {
                throw new NullPointerException("filter is null");
            }
            if (filterList == null)
                filterList = new ArrayList<>();
            if (!filterList.contains(filter))
                filterList.add(filter);
            return this;
        }

        public Builder setRetryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder setScanTimeOut(int timeOut) {
            this.scanTimeOut = timeOut;
            return this;
        }

        public Builder setTurnOffWifiBeforeScan(boolean turnOffWifiBeforeScan) {
            this.turnOffWifiBeforeScan = turnOffWifiBeforeScan;
            return this;
        }
    }
}
