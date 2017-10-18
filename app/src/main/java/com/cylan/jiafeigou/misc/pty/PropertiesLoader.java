package com.cylan.jiafeigou.misc.pty;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.google.gson.Gson;

import java.util.HashMap;
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
    private PropertyFile propertyFile;
    private PropertyFile defaultPropertyFile;
    private PropertyFile sharePropertyFile;
    //取一次，就保存下来。缓存着。
    private ArrayMap<Integer, Map<String, String>> cacheMap = new ArrayMap<>();
    private ArrayMap<Integer, Map<String, String>> defaultPropertyCacheMap = new ArrayMap<>();

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


    @Override
    public void initialize() {
        try {
            String content = FileUtils.readAsset(ContextUtils.getContext().getAssets(),
                    "attribute_table.json");
            propertyFile = new Gson().fromJson(content, PropertyFile.class);
            AppLogger.d("load properties: " + content.length() + "," + propertyFile.getVersion());
        } catch (Exception e) {
            AppLogger.e("initialize failed: " + e.getLocalizedMessage());
            throw new IllegalArgumentException("attribute_table.json文件有错");
        }
        try {
            String content = FileUtils.readAsset(ContextUtils.getContext().getAssets(),
                    "attribute_table_default.json");
            defaultPropertyFile = new Gson().fromJson(content, PropertyFile.class);
            AppLogger.d("load properties: " + content.length() + "," + propertyFile.getVersion());
        } catch (Exception e) {
            AppLogger.e("initialize failed: " + e.getLocalizedMessage());
            throw new IllegalArgumentException("attribute_table_default.json文件有错");
        }
        try {
            String content = FileUtils.readAsset(ContextUtils.getContext().getAssets(),
                    "attribute_table_shared.json");
            sharePropertyFile = new Gson().fromJson(content, PropertyFile.class);
            AppLogger.d("load properties: " + content.length() + "," + sharePropertyFile.getVersion());
        } catch (Exception e) {
            AppLogger.i("initialize failed: " + e.getLocalizedMessage());
            throw new IllegalArgumentException("attribute_table_shared.json文件有错");
        }
    }

    @Override
    public boolean hasProperty(int pidOrOs, String tag) {
        return hasProperty(pidOrOs, tag, false);
    }

    /**
     * @param file
     * @param pidOrOs
     * @return
     */
    private Map<String, String> accessCacheMap(PropertyFile file, int pidOrOs) {
        return accessCacheMap(cacheMap, file, pidOrOs);
    }

    private Map<String, String> accessCacheMap(final ArrayMap<Integer, Map<String, String>> cacheMap, PropertyFile file, int pidOrOs) {
        Map<String, String> tmpMap = cacheMap.get(pidOrOs);
        final String sPid = String.valueOf(pidOrOs);
        if (TextUtils.isEmpty(sPid)) {
            throw new IllegalArgumentException("pidOrOs cannot be empty!!!");
        }
        if (tmpMap == null) {
            final int count = ListUtils.getSize(file.getpList());
            //每次遍历,效率比较低,有待优化
            for (int i = 0; i < count; i++) {
                tmpMap = file.getpList().get(i);
                final String pid = tmpMap.get("PID");
                final String os = tmpMap.get("OS");
                if (TextUtils.equals(sPid, pid) || TextUtils.equals(sPid, os)) {
                    cacheMap.put(pidOrOs, tmpMap);
                    return tmpMap;
                }
            }
        }
        if (tmpMap == null) {
            tmpMap = new HashMap<>();
            AppLogger.e("fxxx,err happend");
        }
        return tmpMap;
    }

    @Override
    public boolean hasProperty(int pidOrOs, String tag, boolean share) {
        tag = tag.toUpperCase();
        if (share) {
            if (sharePropertyFile == null) {
                return false;
            }
            final Map<String, String> map = accessCacheMap(sharePropertyFile, pidOrOs);
            final String p = map.get(tag);
            return !TextUtils.isEmpty(p) && !TextUtils.equals("0", p);
        } else {
            if (propertyFile == null) {
                return false;
            }
            final Map<String, String> map = accessCacheMap(propertyFile, pidOrOs);
            final String p = map.get(tag);
            return !TextUtils.isEmpty(p) && !TextUtils.equals("0", p);
        }
    }

    @Override
    public String property(int pidOrOs, String tag) {
        return property(pidOrOs, tag, false);
    }

    @Override
    public String property(int pidOrOs, String tag, boolean share) {
        tag = tag.toUpperCase();
        if (share) {
            if (sharePropertyFile == null) {
                return "";
            }
            Map<String, String> map = accessCacheMap(sharePropertyFile, pidOrOs);
            return map.get(tag);
        } else {
            if (propertyFile == null) {
                return "";
            }
            Map<String, String> map = accessCacheMap(propertyFile, pidOrOs);
            return map.get(tag);
        }
    }

    @Override
    public String defaultProperty(int pidOrOs, String tag) {
        final Map<String, String> tmpMap = accessCacheMap(defaultPropertyCacheMap, defaultPropertyFile, pidOrOs);
        if (tmpMap == null) {
            return null;
        }
        return tmpMap.get(tag.toUpperCase());
    }

    @Override
    public boolean isSerial(String serial, int pidOrOs) {
        if (propertyFile == null) {
            return false;
        }
        serial = serial.toUpperCase();
        Map<String, List<Integer>> map = propertyFile.getSerialMap();
        List<Integer> list = map == null ? null : map.get(serial);
        return list != null && list.contains(pidOrOs);
    }

    @Override
    public int getOSType(String cid) {
        if (propertyFile == null) {
            return 0;
        }
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
