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

    @Inject
    public PropertiesLoader() {
    }

    private PropertyFile propertyFile;

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
    }

    @Override
    public boolean hasProperty(int pidOrOs, String tag) {
        tag = tag.toUpperCase();
        if (propertyFile == null) return false;
        final int count = ListUtils.getSize(propertyFile.getpList());
        //每次遍历,效率比较低,有待优化
        for (int i = 0; i < count; i++) {
            Map<String, String> map = propertyFile.getpList().get(i);
            final String pid = map.get("PID");
            final String os = map.get("OS");
            if (!TextUtils.isEmpty(os) && TextUtils.equals(os, pidOrOs + "")) {
                final String tagValue = map.get(tag);
                return !TextUtils.isEmpty(tagValue) && TextUtils.equals(tagValue, "1");
            }
            if (!TextUtils.isEmpty(pid) && TextUtils.equals(pid, pidOrOs + "")) {
                final String tagValue = map.get(tag);
                return !TextUtils.isEmpty(tagValue) && TextUtils.equals(tagValue, "1");
            }
        }
        return false;
    }

    @Override
    public String property(int pidOrOs, String tag) {
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

    @Override
    public boolean isSerial(String serial, int pidOrOs) {
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
