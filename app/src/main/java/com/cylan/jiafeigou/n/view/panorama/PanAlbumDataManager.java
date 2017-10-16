package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.cache.db.impl.PanFileDownloader;
import com.cylan.jiafeigou.cache.db.module.DownloadFile;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by holy on 2017/3/18.
 */

public class PanAlbumDataManager {

    private static PanAlbumDataManager instance;


    private PanAlbumDataManager() {
    }

    public static PanAlbumDataManager getInstance() {
        if (instance == null) {
            synchronized (PanAlbumDataManager.class) {
                if (instance == null) {
                    instance = new PanAlbumDataManager();
                }
            }
        }
        return instance;
    }

    /**
     * 开始时间
     *
     * @param time
     * @param count
     * @return
     */
    public Observable<List<DownloadFile>> loadFileList(String uuid, int time, boolean lt, int count) {
        return PanFileDownloader.getDownloader().getFileFrom(uuid, time, lt, count);
    }

    public int getFileSize(String fileName) {
        DownloadFile f = downloadingFileMap.get(fileName);
        return f == null ? 0 : f.fileSize;
    }

    /**
     * 存放列表
     *
     * @param fileName
     * @param md5
     * @return
     */
    public Observable<Long> insertFile(String uuid, String fileName, byte[] md5, int fileSize) {
        DownloadFile file = new DownloadFile();
        file.fileName = fileName;
        file.uuid = uuid;
        file.fileSize = fileSize;
        file.time = MiscUtils.getValueFrom(fileName);
        file.md5 = md5;
        return PanFileDownloader.getDownloader().getFileFrom(uuid, fileName)
                .subscribeOn(Schedulers.io())
                .flatMap(downloadFile -> {
                    if (downloadFile != null) {
                        if (!Arrays.equals(downloadFile.md5, md5)) {
                            return PanFileDownloader.getDownloader().removeFile(uuid, fileName)
                                    .subscribeOn(Schedulers.io())
                                    .flatMap(aLong -> Observable.just(PanFileDownloader.getDownloader().insertFile(file)));
                        }
                    } else {
                        return Observable.just(PanFileDownloader.getDownloader().insertFile(file));
                    }
                    return null;
                });
    }

    public Observable<Long> removeFile(String uuid, String fileName) {
        return PanFileDownloader.getDownloader().removeFile(uuid, fileName);
    }

    /**
     * 维持下载中的文件
     */
    private HashMap<String, DownloadFile> downloadingFileMap = new HashMap<>();

    /**
     * 文件状态
     *
     * @param fileName
     * @param position
     * @return
     */
    public int updateFile(String uuid, String fileName, int position) {
        DownloadFile file = downloadingFileMap.get(fileName);
        if (file != null && file.id != 0) {
            file.offset = position;
            file.uuid = uuid;
            boolean finished = file.offset >= file.fileSize;
            if (finished) {
                downloadingFileMap.remove(fileName);
            }
            file.state = finished ? DownloadState.SUC : DownloadState.DOWNLOADING;
            PanFileDownloader.getDownloader().updateOrSaveFile(file).subscribeOn(Schedulers.io()).subscribe(ret -> {
            }, AppLogger::e);
            return finished ? 0 : 1;
        } else {
            AppLogger.e("this file is not ready for download or new File not cached in db");
            return -1;
        }
    }

    public Observable<Integer> getFileDownloadState(String uuid, String fileName) {
        return PanFileDownloader.getDownloader().getFileDownloadState(uuid, fileName);
    }

    public Observable<DownloadFile> getDownloadFile(String fileName, String uuid) {
        return PanFileDownloader.getDownloader().getFileFrom(fileName, uuid);
    }

    /**
     * 优先提取下载失败的
     *
     * @return
     */
    public Observable<DownloadFile> getNextPreparedDownloadFile(String uuid) {
        return PanFileDownloader.getDownloader().getPreparedDownloadFile(uuid)
                .flatMap(downloadFile -> {
                    if (downloadFile != null) {
                        AppLogger.d("prepared for download: " + downloadFile);
                        downloadingFileMap.put(downloadFile.fileName, downloadFile);
                    }
                    return Observable.just(downloadFile);
                });
    }


    public static class DownloadState {
        public static final int IDLE = 0;//准备好了，需要下载，下载中断，重新加载表的数据的时候，需要更新
        public static final int DOWNLOADING = 1;
        public static final int FAILED = 2;
        public static final int SUC = 3;
    }

}
