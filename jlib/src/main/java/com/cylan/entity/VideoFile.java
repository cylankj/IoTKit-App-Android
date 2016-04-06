package com.cylan.entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class VideoFile {

    public static final String DIR_DATE = "date";
    public static final String DIR_LIST = "list";
    public static final String DIR_ALL = "all";
    public static final String DIR_GETALL = "getall";
    public static final String NAME = "name";
    public static final String THUMB = "thumb";
    public static final String TOKEN = "token";
    public static final String LENGTH = "length";
    public static final String LASTMODIFIED = "lastModified";
    public static final String PREFIX_THUMB = "/thumb=";
    public static final String PREFIX_DATE = "/date=";
    public static final String PREFIX_FILE = "/file=";
    public String fileName;
    public String thumbPath;
    public long length;
    public long lastModified;

    public VideoFile() {

    }

    public VideoFile(File f) {
        fileName = f.getName();
        length = f.length();
        thumbPath = fileName;
        lastModified = f.lastModified() / 1000;
    }

    public VideoFile(JSONObject json) {
        try {
            if (json.has(THUMB)) {
                thumbPath = json.getString(THUMB);
            } else {
                thumbPath = json.getString(TOKEN);
            }
            fileName = json.getString(NAME);
            length = json.getLong(LENGTH);
            lastModified = json.getLong(LASTMODIFIED);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(NAME, fileName);
            json.put(THUMB, thumbPath);
            json.put(LENGTH, length);
            json.put(LASTMODIFIED, lastModified);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String getThumbPath() {
        return PREFIX_THUMB + thumbPath;
    }

    public String getFilePath() {
        return PREFIX_FILE + fileName;
    }

}
