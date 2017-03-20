package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.cache.db.impl.PanFileDownloader;
import com.cylan.jiafeigou.cache.db.module.DownloadFile;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import rx.Observable;

/**
 * Created by holy on 2017/3/18.
 */

public class PanAlbumDataManager {

    private boolean downloading = false;
    private static PanAlbumDataManager instance;


    private PanAlbumDataManager() {
    }

    public static PanAlbumDataManager getInstance() {
        if (instance == null) {
            synchronized (PanAlbumDataManager.class) {
                if (instance == null) instance = new PanAlbumDataManager();
            }
        }
        return instance;
    }

    private HashMap<String, DownloadFile> downloadFileHashMap = new HashMap<>();

    public ArrayList<DownloadFile> getAllFileList() {
        ArrayList<DownloadFile> list = new ArrayList<>(downloadFileHashMap.size());
        Iterator<String> set = downloadFileHashMap.keySet().iterator();
        while (set.hasNext()) {
            String fileName = set.next();
            list.add(downloadFileHashMap.get(fileName));
        }
        return list;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public boolean isDownloading() {
        return downloading;
    }

    /**
     * 存放列表
     *
     * @param fileName
     * @param md5
     * @return
     */
    public boolean putFile(String fileName, byte[] md5, int fileSize) {
        boolean ret = false;
        DownloadFile downloadFile = downloadFileHashMap.get(fileName);
        if (downloadFile == null) {
            downloadFile = new DownloadFile();
            downloadFile.fileName = fileName;
            downloadFile.md5 = md5;
            downloadFile.fileSize = fileSize;
            downloadFileHashMap.put(fileName, downloadFile);
            ret = true;
        } else {
            if (Arrays.equals(md5, downloadFile.md5) || fileSize != downloadFile.fileSize) {
                AppLogger.e("文件更新了？:" + fileName);
                downloadFile.fileSize = fileSize;
                downloadFile.md5 = md5;
                ret = false;
            }
        }
//        PanFileDownloader.getDownloader().updateOrSaveFile(downloadFile)
//                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
//                .subscribe();
        return ret;
    }

    public boolean removeFile(String fileName) {
        return downloadFileHashMap.remove(fileName) != null;
    }


    public byte[] getFileMd5(String fileName) {
        DownloadFile file = downloadFileHashMap.get(fileName);
        if (file != null && file.md5 != null)
            return file.md5;
        return null;
    }

    /**
     * 文件状态
     *
     * @param fileName
     * @param offset
     * @return
     */
    public int updateDownloadFile(String fileName, int offset) {
        DownloadFile file = downloadFileHashMap.get(fileName);
        if (file == null) {
            return -1;
        }
        this.downloading = true;
        file.state = DownloadState.DOWNLOADING;
        file.fileName = fileName;
        file.offset = offset;
        if (file.offset > file.fileSize) {
            AppLogger.e("should't happened:" + file);
            return 2;
        }
        if (file.offset == file.fileSize) {
            AppLogger.e("下载成功" + file);
            file.state = DownloadState.SUC;
            return 1;
        }
        return 0;
    }

    public Observable<Integer> getFileDownloadState(String fileName) {
        return PanFileDownloader.getDownloader().getFileDownloadState(fileName);
    }

    public Observable<DownloadFile> getDownloadFile(String fileName) {
        return PanFileDownloader.getDownloader().getFileFrom(fileName);
    }

    /**
     * 优先提取下载失败的
     *
     * @return
     */
    public DownloadFile getNextPreparedDownloadFile() {
        if (downloadFileHashMap.size() == 0) return null;
        DownloadFile file = null;
        Iterator<String> set = downloadFileHashMap.keySet().iterator();
        while (set.hasNext()) {
            String fileName = set.next();
            DownloadFile file1 = downloadFileHashMap.get(fileName);
            if (file1 != null && file1.state == DownloadState.FAILED) {
                return file1;
            }
            file = file1;
        }
        return file;
    }


    public static class DownloadState {
        public static final int IDLE = 0;//准备好了，需要下载，下载中断，重新加载表的数据的时候，需要更新
        public static final int DOWNLOADING = 1;
        public static final int SUC = 2;
        public static final int FAILED = 3;
    }

}
