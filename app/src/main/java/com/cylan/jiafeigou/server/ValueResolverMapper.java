package com.cylan.jiafeigou.server;

import org.msgpack.value.Value;

/**
 * Created by yanzhendong on 2017/8/17.
 */

public interface ValueResolverMapper {

    /**
     * 用来解析201 net 消息
     *
     * @Index(0) public int net = 0;
     * @Index(1) public String ssid;
     */
    class NetValueResolver {
        private NetValueResolver() {
        }

        public static String getKey(String uuid) {
            return uuid + "-" + 201;
        }

        public static int getNet(Value value, int defaultValue) {
            if (value == null || !value.isArrayValue()) {
                return defaultValue;//结构体信息错误
            }
            Value net = value.asArrayValue().getOrNilValue(0);
            return net.isIntegerValue() ? net.asIntegerValue().asInt() : 0;
        }

        public static String getSSID(Value value, String defaultValue) {
            if (value == null || !value.isArrayValue()) {
                return defaultValue;//结构体信息错误
            }
            Value ssid = value.asArrayValue().getOrNilValue(1);
            return ssid.isStringValue() ? ssid.asStringValue().asString() : defaultValue;
        }
    }

}
