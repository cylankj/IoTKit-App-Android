package com.cylan.jiafeigou.misc.pty;

import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.google.gson.Gson;

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
        if (propertyFile == null) return false;
        final int count = ListUtils.getSize(propertyFile.getpList());
        //每次遍历,效率比较低,有待优化
        for (int i = 0; i < count; i++) {
            Map<String, String> map = propertyFile.getpList().get(i);
            final String pid = map.get("Pid");
            final String os = map.get("os");
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
        if (propertyFile == null) return "";
        final int count = ListUtils.getSize(propertyFile.getpList());
        //效率比较低,有待优化
        for (int i = 0; i < count; i++) {
            Map<String, String> map = propertyFile.getpList().get(i);
            final String pid = map.get("Pid");
            final String os = map.get("os");
            if (!TextUtils.isEmpty(os) && TextUtils.equals(os, pidOrOs + "")) {
                return map.get(tag);
            }
            if (!TextUtils.isEmpty(pid) && TextUtils.equals(pid, pidOrOs + ""))
                return map.get(tag);
        }
        return "";
    }


}
