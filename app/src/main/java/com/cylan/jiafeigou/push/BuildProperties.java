package com.cylan.jiafeigou.push;

import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by hds on 17-4-24.
 */

public class BuildProperties {
    private final Properties properties;

    private BuildProperties() throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
    }

    public boolean containsKey(final Object key) {
        return properties.containsKey(key);
    }

    public boolean containsValue(final Object value) {
        return properties.containsValue(value);
    }

    public Set<Map.Entry<Object, Object>> entrySet() {
        return properties.entrySet();
    }

    public String getProperty(final String name) {
        return properties.getProperty(name);
    }

    public String getProperty(final String name, final String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public Enumeration<Object> keys() {
        return properties.keys();
    }

    public Set<Object> keySet() {
        return properties.keySet();
    }

    public int size() {
        return properties.size();
    }

    public Collection<Object> values() {
        return properties.values();
    }

    public static BuildProperties newInstance() throws IOException {
        return new BuildProperties();
    }

    // 检测MIUI
    public static final class MIUIUtils {

        private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
        private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
        private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

        public static boolean isMIUI() {
            try {
                //BuildProperties 是一个工具类，下面会给出代码
                final BuildProperties prop = BuildProperties.newInstance();
                return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                        || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                        || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
            } catch (final IOException e) {
                return false;
            }
        }
    }

    //检测EMUI
    public static final class EMUIUtils {

        private static final String KEY_EMUI_VERSION_CODE = "ro.build.hw_emui_api_level";

        public static boolean isEMUI() {
            try {
                //BuildProperties 是一个工具类，下面会给出代码
                final BuildProperties prop = BuildProperties.newInstance();
                return prop.getProperty(KEY_EMUI_VERSION_CODE, null) != null;
            } catch (final IOException e) {
                return false;
            }
        }
    }
    // 检测Flyme

    public static final class FlymeUtils {
        public static boolean isFlyme() {
            try {
                // Invoke Build.hasSmartBar()
                final Method method = Build.class.getMethod("hasSmartBar");
                return method != null;
            } catch (final Exception e) {
                return false;
            }
        }
    }
}
