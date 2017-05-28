package com.cylan.jiafeigou.misc.pty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hds on 17-5-28.
 */

public class PropertyFile {

    private List<Product> pList = new ArrayList<>();
    private String version;

    public List<Product> getpList() {
        return pList;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setpList(List<Product> pList) {
        this.pList = pList;
    }
}
