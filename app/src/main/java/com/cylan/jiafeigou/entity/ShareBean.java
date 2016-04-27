package com.cylan.jiafeigou.entity;

import com.cylan.support.DswLog;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShareBean implements Serializable {
    private String account;
    private String bind_time;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getBind_time() {
        return bind_time;
    }

    public void setBind_time(String bind_time) {
        this.bind_time = bind_time;
    }

    public static List<ShareBean> parseBean(JSONArray ja) {
        try {
            List<ShareBean> list = new ArrayList<ShareBean>();
            for (int i = 0; i < ja.length(); i++) {
                ShareBean sb = new ShareBean();
                sb.setAccount(ja.getJSONObject(i).getString("account"));
                //sb.setBind_time(ja.getJSONObject(i).getString("bind_time"));
                list.add(sb);
            }

            return list;
        } catch (JSONException e) {
            DswLog.ex(e.toString());
        }
        return null;

    }
}
