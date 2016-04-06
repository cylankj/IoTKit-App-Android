package com.cylan.jiafeigou.entity;

import android.content.Context;

import com.cylan.jiafeigou.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.Serializable;


public class Update implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public final static String UTF8 = "UTF-8";

    private int ret;
    private String version;
    private String shortversion;
    private String url;
    private String desc;
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getShortversion() {
        return shortversion;
    }

    public void setShortversion(String shortversion) {
        this.shortversion = shortversion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static Update parse(Context ctx, String JSONString) throws IOException {
        Update update = null;
        try {
            JSONString = JSONString.toString().trim();
            JSONTokener jsonParser = new JSONTokener(JSONString);
            JSONObject js = (JSONObject) jsonParser.nextValue();

            update = new Update();
            update.setRet(js.getInt("ret"));
            update.setUrl(js.getString("url"));
            update.setVersion(js.getString("version"));
            update.setShortversion(js.getString("shortversion"));
            update.setDesc(ctx.getString(R.string.UPGRADE));
            update.setMsg(js.getString("msg"));

        } catch (JSONException e) {
            e.getStackTrace();
        } catch (Throwable e) {
            e.getStackTrace();
        }

        return update;
    }
}
