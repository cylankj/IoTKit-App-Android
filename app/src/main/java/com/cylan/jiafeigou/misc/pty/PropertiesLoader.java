package com.cylan.jiafeigou.misc.pty;

import android.text.TextUtils;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.google.gson.Gson;

import java.util.Map;

import javax.inject.Singleton;

/**
 * Created by hds on 17-5-28.
 */

@Singleton
public class PropertiesLoader implements IProperty {

    private PropertyFile propertyFile;

    @Override
    public void initialize() {
        final String content = FileUtils.readAsset(ContextUtils.getContext().getAssets(),
                "properties.json");
        AppLogger.d("load properties: " + content.length());
        try {
            propertyFile = new Gson().fromJson(content, PropertyFile.class);
        } catch (Exception e) {
            AppLogger.e("initialize failed");
        }
    }

    @Override
    public boolean hasProperty(int pidOrOs, String tag) {
        if (propertyFile == null) return false;
        final int count = ListUtils.getSize(propertyFile.getpList());
        //效率比较低,有待优化
        for (int i = 0; i < count; i++) {
            Map<String, String> map = propertyFile.getpList().get(i);
            final String pid = map.get("Pid");
            final String os = map.get("os");
            if (!TextUtils.isEmpty(os) && TextUtils.equals(os, pidOrOs + ""))
                return true;
            if (!TextUtils.isEmpty(pid) && TextUtils.equals(pid, pidOrOs + ""))
                return true;
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
