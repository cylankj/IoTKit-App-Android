package com.cylan.jiafeigou.misc.pty;

import android.text.TextUtils;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by hds on 17-5-28.
 */

@Singleton
public class PropertiesLoader implements IProperty {

    private static PropertiesLoader instance;


    @Inject
    public PropertiesLoader() {
        instance = this;
    }

    public static PropertiesLoader getInstance() {
        if (instance == null) {
            synchronized (PropertiesLoader.class) {
                if (instance == null) {
                    instance = new PropertiesLoader();
                    instance.initialize();
                }
            }
        }
        return instance;
    }

    private PropertyFile propertyFile;
    private PropertyFile sharePropertyFile;

    @Override
    public void initialize() {
        final String content = FileUtils.readAsset(ContextUtils.getContext().getAssets(),
                "properties.json");
        try {
            propertyFile = new Gson().fromJson(content, PropertyFile.class);
            AppLogger.d("load properties: " + content.length() + "," + propertyFile.getVersion());
        } catch (Exception e) {
            AppLogger.e("initialize failed: " + e.getLocalizedMessage());
            throw new IllegalArgumentException("properties.json文件有错");
        }
        final String shareContent = FileUtils.readAsset(ContextUtils.getContext().getAssets(),
                "shareProperties.json");
        try {
            sharePropertyFile = new Gson().fromJson(shareContent, PropertyFile.class);
            AppLogger.d("load properties: " + content.length() + "," + sharePropertyFile.getVersion());
        } catch (Exception e) {
            AppLogger.e("initialize failed: " + e.getLocalizedMessage());
//            throw new IllegalArgumentException("properties.json文件有错");
        }
    }

    @Override
    public boolean hasProperty(int pidOrOs, String tag) {
        return hasProperty(pidOrOs, tag, false);
    }

    @Override
    public boolean hasProperty(int pidOrOs, String tag, boolean share) {
        tag = tag.toUpperCase();
        if (share) {
            if (sharePropertyFile == null) return false;
            final int count = ListUtils.getSize(sharePropertyFile.getpList());
            //每次遍历,效率比较低,有待优化
            for (int i = 0; i < count; i++) {
                Map<String, String> map = propertyFile.getpList().get(i);
                final String pid = map.get("PID");
                final String os = map.get("OS");
                if (!TextUtils.isEmpty(os) && TextUtils.equals(os, pidOrOs + "")) {
                    final String tagValue = map.get(tag);
                    return !TextUtils.isEmpty(tagValue) && !TextUtils.equals(tagValue, "0");
                }
                if (!TextUtils.isEmpty(pid) && TextUtils.equals(pid, pidOrOs + "")) {
                    final String tagValue = map.get(tag);
                    return !TextUtils.isEmpty(tagValue) && !TextUtils.equals(tagValue, "0");
                }
            }
            return false;
        } else {
            if (propertyFile == null) return false;
            final int count = ListUtils.getSize(propertyFile.getpList());
            //每次遍历,效率比较低,有待优化
            for (int i = 0; i < count; i++) {
                Map<String, String> map = propertyFile.getpList().get(i);
                final String pid = map.get("PID");
                final String os = map.get("OS");
                if (!TextUtils.isEmpty(os) && TextUtils.equals(os, pidOrOs + "")) {
                    final String tagValue = map.get(tag);
                    return !TextUtils.isEmpty(tagValue) && !TextUtils.equals(tagValue, "0");
                }
                if (!TextUtils.isEmpty(pid) && TextUtils.equals(pid, pidOrOs + "")) {
                    final String tagValue = map.get(tag);
                    return !TextUtils.isEmpty(tagValue) && !TextUtils.equals(tagValue, "0");
                }
            }
            return false;
        }
    }

    @Override
    public String property(int pidOrOs, String tag) {
        return property(pidOrOs, tag, false);
    }

    @Override
    public String property(int pidOrOs, String tag, boolean share) {
        if (share) {
            tag = tag.toUpperCase();
            if (sharePropertyFile == null) return "";
            final int count = ListUtils.getSize(sharePropertyFile.getpList());
            //效率比较低,有待优化
            for (int i = 0; i < count; i++) {
                Map<String, String> map = sharePropertyFile.getpList().get(i);
                final String pid = map.get("PID");
                final String os = map.get("OS");
                if (!TextUtils.isEmpty(os) && TextUtils.equals(os, pidOrOs + "")) {
                    return map.get(tag);
                }
                if (!TextUtils.isEmpty(pid) && TextUtils.equals(pid, pidOrOs + ""))
                    return map.get(tag);
            }
            return "";
        } else {
            tag = tag.toUpperCase();
            if (propertyFile == null) return "";
            final int count = ListUtils.getSize(propertyFile.getpList());
            //效率比较低,有待优化
            for (int i = 0; i < count; i++) {
                Map<String, String> map = propertyFile.getpList().get(i);
                final String pid = map.get("PID");
                final String os = map.get("OS");
                if (!TextUtils.isEmpty(os) && TextUtils.equals(os, pidOrOs + "")) {
                    return map.get(tag);
                }
                if (!TextUtils.isEmpty(pid) && TextUtils.equals(pid, pidOrOs + ""))
                    return map.get(tag);
            }
            return "";
        }
    }

    @Override
    public boolean isSerial(String serial, int pidOrOs) {
        if (propertyFile == null) return false;
        serial = serial.toUpperCase();
        if (propertyFile == null) return false;
        Map<String, List<Integer>> map = propertyFile.getSerialMap();
        List<Integer> list = map == null ? null : map.get(serial);
        return list != null && list.contains(pidOrOs);
    }

    public int getOSType(String cid) {
        if (propertyFile == null) return 0;
        //效率比较低,有待优化
        final int count = ListUtils.getSize(propertyFile.getpList());
        for (int i = 0; i < count; i++) {
            Map<String, String> map = propertyFile.getpList().get(i);
            final String cidprefix = map.get("CIDPREFIX");
            if (cid.startsWith(cidprefix)) {
                return Integer.parseInt(map.get("OS"));
            }
        }
        return 0;

    }
}
