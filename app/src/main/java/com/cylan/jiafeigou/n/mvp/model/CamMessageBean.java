package com.cylan.jiafeigou.n.mvp.model;

import java.util.ArrayList;

/**
 * Created by hunt on 16-5-14.
 */
public class CamMessageBean {

    public int id = 0;

    /**
     * 直接类型，不需要转型。
     */
    public ArrayList<String> urlList;

    public long time;

    public String content;

    public int viewType = 0;


}
