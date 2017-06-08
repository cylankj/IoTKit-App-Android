package com.cylan.jiafeigou.n.view.panorama;

import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.adapter.PanoramaAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.download.DownloadInfo;
import com.lzy.okserver.download.DownloadManager;
import com.lzy.okserver.download.db.DownloadDBManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by yanzhendong on 2017/5/10.
 */

public class PanoramaAlbumPresenter extends BasePresenter<PanoramaAlbumContact.View> implements PanoramaAlbumContact.Presenter {
    private Subscription fetchSubscription;
    private Subscription deleteSubscription;

    @Override
    public void onViewAttached(PanoramaAlbumContact.View view) {
        super.onViewAttached(view);
        DownloadManager.getInstance().setTargetFolder(JConstant.PANORAMA_MEDIA_PATH + File.separator + uuid);
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        DownloadManager.getInstance().stopAllTask();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkSDCardAndInit();
    }

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(monitorPanoramaAPI());
    }

    private Subscription monitorPanoramaAPI() {
        return BasePanoramaApiHelper.getInstance().monitorPanoramaApi()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(api -> {
                    mView.onViewModeChanged(api.ApiType == 0 ? 2 : 0, api.ApiType == -1);
                }, e -> {
                });
    }


    public void checkSDCardAndInit() {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().getSdInfo()
                .timeout(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (ret != null && ret.sdIsExist == 0) {//sd 卡不存在
                        mView.onSDCardCheckResult(0);
                    } else {
                        mView.onSDCardCheckResult(1);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        registerSubscription(subscribe);
    }

    @Override
    public void fetch(int time, int fetchLocation) {//0:本地;1:设备;2:本地+设备
        if (fetchSubscription != null && fetchSubscription.isUnsubscribed()) {
            fetchSubscription.unsubscribe();
        }
        if (fetchLocation == 0) {
            fetchSubscription = loadFromLocal(time)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(items -> {
                        mView.onAppend(items, time == 0, true);
                    }, e -> {
                        AppLogger.e(e.getMessage());
                        ToastUtil.showNegativeToast("获取设备文件列表超时");
                    });
        } else if (fetchLocation == 1) {
            fetchSubscription = loadFromServer(time)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(items -> {
                        mView.onAppend(items, time == 0, true);
                    }, e -> {
                        AppLogger.e(e.getMessage());
                        ToastUtil.showNegativeToast("获取设备文件列表超时");
                    });
        } else if (fetchLocation == 2) {
            fetchSubscription = loadFromLocal(time)
                    .observeOn(AndroidSchedulers.mainThread())
//                    .map(items -> {
//                        mView.onAppend(items, time == 0, false);
//                        return items;
//                    })
                    .observeOn(Schedulers.io())
                    .flatMap(items -> loadFromServer(time).map(items1 -> {
                        items1.addAll(items);
                        Map<String, PanoramaAlbumContact.PanoramaItem> sort = new HashMap<>();
                        for (PanoramaAlbumContact.PanoramaItem panoramaItem : items1) {
                            PanoramaAlbumContact.PanoramaItem panoramaItem1 = sort.get(panoramaItem.fileName);
                            if (panoramaItem1 == null) {
                                sort.put(panoramaItem.fileName, panoramaItem);
                            } else {
                                panoramaItem1.location = 2;
                            }
                        }
                        List<PanoramaAlbumContact.PanoramaItem> result = new ArrayList<>(sort.values());
                        Collections.sort(result, (o1, o2) -> o2.time == o1.time ? o2.location - o1.location : o2.time - o1.time);
                        return result.subList(0, result.size() > 20 ? 20 : result.size());
                    }))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(items -> {
                        mView.onAppend(items, time == 0, true);
                    }, e -> {
                        AppLogger.e(e.getMessage());
                    });
        }
        registerSubscription(fetchSubscription);
    }

    private Observable<List<PanoramaAlbumContact.PanoramaItem>> loadFromServer(int time) {
        return BasePanoramaApiHelper.getInstance().getFileList(0, time == 0 ? (int) (System.currentTimeMillis() / 1000) : time, 20)
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))//访问网络设置超时时间,访问本地不用设置超时时间
                .map(files -> {
                    List<PanoramaAlbumContact.PanoramaItem> result = new ArrayList<>();
                    if (files != null && files.files != null) {
                        String deviceIp = BasePanoramaApiHelper.getInstance().getDeviceIp();
                        PanoramaAlbumContact.PanoramaItem item;
                        for (String file : files.files) {
                            item = new PanoramaAlbumContact.PanoramaItem(file);
                            item.location = 1;
                            result.add(item);
                            String taskKey = PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, item.fileName);
                            //自动下载逻辑
                            item.downloadInfo = DownloadManager.getInstance().getDownloadInfo(taskKey);
                            if (item.downloadInfo != null) {
                                String path = item.downloadInfo.getTargetPath();
                                if (!FileUtils.isFileExist(path) && item.downloadInfo.getState() == 4) {
                                    DownloadManager.getInstance().removeTask(item.downloadInfo.getTaskKey());
                                    item.downloadInfo = null;
                                }
                            }
                            if (deviceIp != null && item.type == 0) {
                                String url = deviceIp + "/images/" + item.fileName;
                                GetRequest request = OkGo.get(url);
                                DownloadInfo downloadInfo = DownloadManager.getInstance().getDownloadInfo(taskKey);
                                if (downloadInfo != null) {
                                    downloadInfo.setRequest(request);
                                    downloadInfo.setUrl(request.getBaseUrl());
                                    DownloadDBManager.INSTANCE.replace(downloadInfo);
                                }
                                DownloadManager.getInstance().addTask(taskKey, request, new PanoramaAdapter.MyDownloadListener());
                                item.downloadInfo = DownloadManager.getInstance().getDownloadInfo(taskKey);
                            }
                        }
                    }
                    return result;
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
                            int item1Time = parseTime(item1.getFileName());
                            int item2Time = parseTime(item2.getFileName());
                            return item2Time - item1Time;
                        });
                        int finalTime = time == 0 ? Integer.MAX_VALUE : time;
                        PanoramaAlbumContact.PanoramaItem panoramaItem;
                        for (DownloadInfo item : items) {
                            int itemTime = parseTime(item.getFileName());
                            if (itemTime >= finalTime) continue;
                            boolean endsWith = item.getTargetPath() != null && item.getTargetPath().endsWith(File.separator + uuid + File.separator + item.getFileName());
                            if (item.getState() == 4 && FileUtils.isFileExist(item.getTargetPath()) && result.size() < 20 && endsWith) {
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

    private int parseTime(String fileName) {
        if (fileName == null) return 0;
        return Integer.parseInt(fileName.split("\\.")[0].split("_")[0]);
    }

    @Override
    public void deletePanoramaItem(List<PanoramaAlbumContact.PanoramaItem> items, int mode) {
        if (deleteSubscription != null && deleteSubscription.isUnsubscribed()) {
            deleteSubscription.unsubscribe();
        }
        if (mode == 0) {//本地
            deleteSubscription = Observable.create((Observable.OnSubscribe<List<PanoramaAlbumContact.PanoramaItem>>) subscriber -> {
                for (PanoramaAlbumContact.PanoramaItem item : items) {
                    DownloadManager.getInstance().removeTask(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, item.fileName), true);
                }
                subscriber.onNext(items);
                subscriber.onCompleted();
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> mView.onDelete(result), e -> {
                        AppLogger.e(e.getMessage());
                    });
        } else if (mode == 1) {//设备
            deleteSubscription = BasePanoramaApiHelper.getInstance().delete(1, convert(items))
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
                    .subscribe(result -> mView.onDelete(result), e -> {
                        AppLogger.e(e);
                    });
        } else if (mode == 2) {//本地+设备
            deleteSubscription = Observable.create((Observable.OnSubscribe<List<PanoramaAlbumContact.PanoramaItem>>) subscriber -> {
                for (PanoramaAlbumContact.PanoramaItem item : items) {
                    DownloadManager.getInstance().removeTask(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, item.fileName), true);
                }
                subscriber.onNext(items);
                subscriber.onCompleted();
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap(ret -> BasePanoramaApiHelper.getInstance().delete(1, convert(items)))
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
                    .subscribe(result -> mView.onDelete(result), e -> {
                        AppLogger.e(e);
                    });
        }
    }

    private List<String> convert(List<PanoramaAlbumContact.PanoramaItem> items) {
        List<String> result = new ArrayList<>(items.size());
        for (PanoramaAlbumContact.PanoramaItem item : items) {
            result.add(item.fileName);
        }
        return result;
    }
}
