package com.cylan.jiafeigou.n.view.panorama;

import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;


/**
 * Created by yanzhendong on 2017/5/10.
 */

public class PanoramaAlbumPresenter extends BasePresenter<PanoramaAlbumContact.View> implements PanoramaAlbumContact.Presenter {
    @Override
    public void onStart() {
        super.onStart();
        refresh(false);
    }

    @Override
    public void refresh(boolean asc) {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().getFileList(0, (int) (System.currentTimeMillis() / 1000), 20)
                .map(files -> {
                    AppLogger.e("设备文件列表:" + new Gson().toJson(files));
                    List<PanoramaAlbumContact.PanoramaItem> result = new ArrayList<>();
                    if (files != null && files.files != null) {
                        PanoramaAlbumContact.PanoramaItem item;
                        for (String file : files.files) {
                            item = new PanoramaAlbumContact.PanoramaItem(file);
                            result.add(item);
                        }
                    }
                    return result;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    mView.onAppend(items, true);
                }, e -> {
                    AppLogger.e(e);
                });
        registerSubscription(subscribe);
    }


    @Override
    public void downloadFile(String fileName) {

    }

    @Override
    public void deletePanoramaItem(List<PanoramaAlbumContact.PanoramaItem> items) {
        BasePanoramaApiHelper.getInstance().delete(1, convert(items))
                .map(ret -> {
                    List<PanoramaAlbumContact.PanoramaItem> failed = new ArrayList<>();
                    for (PanoramaAlbumContact.PanoramaItem item : items) {
                        for (String file : ret.files) {
                            if (TextUtils.equals(file, item.fileName)) {
                                failed.add(item);
                            }
                        }
                    }
                    items.removeAll(failed);
                    return items;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    mView.onDelete(result);
                }, e -> {
                    AppLogger.e(e);
                });
    }

    private List<String> convert(List<PanoramaAlbumContact.PanoramaItem> items) {
        List<String> result = new ArrayList<>(items.size());
        for (PanoramaAlbumContact.PanoramaItem item : items) {
            result.add(item.fileName);
        }
        return result;
    }
}
