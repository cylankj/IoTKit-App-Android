package com.cylan.jiafeigou.n.view.panorama;

import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.adapter.PanoramaAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.FileUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.download.DownloadInfo;
import com.lzy.okserver.download.DownloadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by yanzhendong on 2017/5/10.
 */

public class PanoramaAlbumPresenter extends BasePresenter<PanoramaAlbumContact.View> implements PanoramaAlbumContact.Presenter {

    @Override
    public void onViewAttached(PanoramaAlbumContact.View view) {
        super.onViewAttached(view);
        DownloadManager.getInstance().setTargetFolder(JConstant.PANORAMA_MEDIA_PATH + File.separator + sourceManager.getAccount().getAccount() + File.separator + uuid);
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        DownloadManager.getInstance().stopAllTask();
    }

    @Override
    public void onStart() {
        super.onStart();
        fetch(0, 2);
    }

    @Override
    public void fetch(int time, int fetchLocation) {//0:本地;1:设备;2:本地+设备
        Observable<List<PanoramaAlbumContact.PanoramaItem>> observable = null;
        if (fetchLocation == 0) {
            observable = loadFromLocal(time);
        } else if (fetchLocation == 1) {
            observable = loadFromServer(time);
        } else if (fetchLocation == 2) {
            observable = loadLocalAndServer(time);
        }
        if (observable != null) {
            Subscription subscribe = observable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(items -> {
                        mView.onAppend(items, time == 0);
                    }, e -> {
                        AppLogger.e(e.getMessage());
                    });
            registerSubscription(subscribe);
        }
    }

    private Observable<List<PanoramaAlbumContact.PanoramaItem>> loadFromServer(int time) {
        return BasePanoramaApiHelper.getInstance().getFileList(0, time == 0 ? (int) (System.currentTimeMillis() / 1000) : time, 20)
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .map(files -> {
                    List<PanoramaAlbumContact.PanoramaItem> result = new ArrayList<>();
                    if (files != null && files.files != null) {
                        PanoramaAlbumContact.PanoramaItem item;
                        for (String file : files.files) {
                            item = new PanoramaAlbumContact.PanoramaItem(file);
                            item.location = 1;
                            result.add(item);
                            //自动下载逻辑
                            item.downloadInfo = DownloadManager.getInstance().getDownloadInfo(uuid + "/images/" + item.fileName);
                            if (item.downloadInfo != null) {
                                String path = item.downloadInfo.getTargetPath();
                                if (!FileUtils.isFileExist(path) && item.downloadInfo.getState() == 4) {
                                    DownloadManager.getInstance().removeTask(item.downloadInfo.getTaskKey());
                                    item.downloadInfo = null;
                                }
                            }
                            String deviceIp = BasePanoramaApiHelper.getInstance().getDeviceIp();
                            if (deviceIp != null) {
                                String url = deviceIp + "/images/" + item.fileName;
                                GetRequest request = OkGo.get(url);
                                DownloadManager.getInstance().addTask(uuid + "/images/" + item.fileName, request, new PanoramaAdapter.MyDownloadListener());
                                if (item.downloadInfo == null) {
                                    item.downloadInfo = DownloadManager.getInstance().getDownloadInfo(uuid + "/images/" + item.fileName);
                                }
                            }
                        }
                    }
                    return result;
                });
    }

    private Observable<List<PanoramaAlbumContact.PanoramaItem>> loadLocalAndServer(int time) {
        return loadFromLocal(time)
                .zipWith(loadFromServer(time), (items, items2) -> {
                    List<PanoramaAlbumContact.PanoramaItem> result = new ArrayList<>(items2);
                    List<PanoramaAlbumContact.PanoramaItem> remove = new ArrayList<>();
                    for (PanoramaAlbumContact.PanoramaItem item : items2) {//fromServer
                        int location = item.location;
                        for (PanoramaAlbumContact.PanoramaItem panoramaItem : items) {//fromLocal
                            if (TextUtils.equals(item.fileName, panoramaItem.fileName)) {
                                location = 2;
                                remove.add(panoramaItem);
                                break;
                            }
                        }
                        item.location = location;
                    }
                    items.removeAll(remove);
                    result.addAll(items);
                    Collections.sort(result, (o1, o2) -> o2.time == o1.time ? o2.location - o1.location : o2.time - o1.time);
                    List<PanoramaAlbumContact.PanoramaItem> panoramaItems = result.subList(0, 20);
                    return panoramaItems;
                });
    }

    private Observable<List<PanoramaAlbumContact.PanoramaItem>> loadFromLocal(int time) {
        return Observable.just(DownloadManager.getInstance().getAllTask())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(items -> {
                    List<PanoramaAlbumContact.PanoramaItem> result = new ArrayList<>();
                    if (items != null && items.size() > 0) {
                        Collections.sort(items, (item1, item2) -> {
                            int item1Time = Integer.parseInt(item1.getFileName().split("\\.")[0].split("_")[0]);
                            int item2Time = Integer.parseInt(item2.getFileName().split("\\.")[0].split("_")[0]);
                            return item2Time - item1Time;
                        });
                        int finalTime = time == 0 ? Integer.MAX_VALUE : time;
                        PanoramaAlbumContact.PanoramaItem panoramaItem;
                        for (DownloadInfo item : items) {
                            int itemTime = Integer.parseInt(item.getFileName().split("\\.")[0].split("_")[0]);
                            if (itemTime >= finalTime) continue;
                            if (item.getState() == 4 && FileUtils.isFileExist(item.getTargetPath()) && result.size() < 20) {
                                panoramaItem = new PanoramaAlbumContact.PanoramaItem(item.getFileName());
                                panoramaItem.location = 0;
                                panoramaItem.downloadInfo = item;
                                result.add(panoramaItem);
                            }
                        }
                    }
                    return result;
                });
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
