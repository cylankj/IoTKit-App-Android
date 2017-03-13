package com.cylan.jiafeigou.cache;

import android.graphics.Bitmap;
import android.net.wifi.ScanResult;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MD5Util;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

/**
 * Created by cylan-hunt on 16-7-9.
 */
public class SimpleCache {

    private static SimpleCache instance;

    private SimpleCache() {
    }

    public static SimpleCache getInstance() {
        if (instance == null)
            instance = new SimpleCache();
        return instance;
    }

    public WeakReference<List<ScanResult>> getWeakScanResult() {
        return weakScanResult;
    }


    public void setWeakScanResult(List<ScanResult> weakScanResult) {
        if (weakScanResult == null)
            return;
        this.weakScanResult = new WeakReference<>(weakScanResult);
    }

    private WeakReference<List<ScanResult>> weakScanResult;
    private WeakReference<HashMap<String, Bitmap>> previewThumbnailCache;

    public void addCache(String key, Bitmap bitmap) {
        if (bitmap == null) {
            AppLogger.e("you add a null bitmap to cache ");
            return;
        }
        if (previewThumbnailCache == null || previewThumbnailCache.get() == null) {
            previewThumbnailCache = new WeakReference<>(new HashMap<>());
        }
        key = MD5Util.lowerCaseMD5(key);
        previewThumbnailCache.get().put(key, bitmap);
    }

    public Bitmap getSimpleBitmapCache(String key) {
        if (previewThumbnailCache == null || previewThumbnailCache.get() == null)
            return null;
        return previewThumbnailCache.get().get(MD5Util.lowerCaseMD5(key));
    }
}
