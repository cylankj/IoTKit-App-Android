package com.cylan.jiafeigou.misc.pty;

/**
 * Created by hds on 17-5-28.
 */

import java.util.HashMap;

/**
 * Created by hds on 17-5-24.
 */
public class Product {
    /**
     * 设备归属名称
     */
    private String serial;
    /**
     * 设备描述
     */
    private String product;

    private int pid = 0;
    private String cidModel;
    private String cidPrefix;
    private int os = 0;
    /**
     * 属性集合
     */
    private HashMap<String, String> propertyMap = new HashMap<>();

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public void setCidModel(String cidModel) {
        this.cidModel = cidModel;
    }

    public void setCidPrefix(String cidPrefix) {
        this.cidPrefix = cidPrefix;
    }

    public void setOs(int os) {
        this.os = os;
    }

    public String getSerial() {
        return serial;
    }

    public String getProduct() {
        return product;
    }

    public int getPid() {
        return pid;
    }

    public String getCidModel() {
        return cidModel;
    }

    public String getCidPrefix() {
        return cidPrefix;
    }

    public int getOs() {
        return os;
    }

    public HashMap<String, String> getPropertyMap() {
        return propertyMap;
    }

    public boolean isEmptyProduct() {
        return (serial == null || serial.length() == 0) && os == 0 && pid == 0 && (product == null || product.length() == 0)
                && (cidModel == null || cidModel.length() == 0) && (cidPrefix == null || cidPrefix.length() == 0);
    }

    @Override
    public String toString() {
        return "Product{" +
                "serial='" + serial + '\'' +
                ", product='" + product + '\'' +
                ", pid=" + pid +
                ", cidModel='" + cidModel + '\'' +
                ", cidPrefix='" + cidPrefix + '\'' +
                ", os=" + os +
                ", propertyMap=" + propertyMap +
                '}';
    }
}
