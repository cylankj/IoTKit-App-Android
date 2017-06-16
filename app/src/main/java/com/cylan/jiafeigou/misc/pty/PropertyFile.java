package com.cylan.jiafeigou.misc.pty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hds on 17-5-28.
 */

public class PropertyFile {

    private List<Map<String, String>> pList = new ArrayList<>();
    private String version;

    public void setVersion(String version) {
        this.version = version;
    }

    public void setpList(List<Map<String, String>> pList) {
        this.pList = pList;
    }

    public String getVersion() {
        return version;
    }

    public List<Map<String, String>> getpList() {
        return pList;
    }
}
