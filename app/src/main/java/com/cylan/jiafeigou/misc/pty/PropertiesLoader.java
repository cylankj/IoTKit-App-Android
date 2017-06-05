package com.cylan.jiafeigou.misc.pty;

import android.text.TextUtils;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.google.gson.Gson;

import java.util.List;

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
        Product product = getProduct(pidOrOs);
        String content = product == null ? null : product.getPropertyMap().get(tag);
        return !TextUtils.isEmpty(content) && TextUtils.equals("1", content);
    }

    @Override
    public String property(int pidOrOs, String tag) {
        if (propertyFile == null) return null;
        Product product = getProduct(pidOrOs);
        return product != null ? product.getPropertyMap().get(tag) : null;
    }

    @Override
    public Product getProduct(int pidOrOs) {
        if (propertyFile == null) return null;
        List<Product> list = propertyFile.getpList();
        if (list == null) return null;
        for (Product p : list) {
            if (p.getOs() == pidOrOs || p.getPid() == pidOrOs)
                return p;
        }
        return null;
    }

}
