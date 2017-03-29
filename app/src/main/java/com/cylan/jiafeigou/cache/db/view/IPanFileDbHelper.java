package com.cylan.jiafeigou.cache.db.view;

import com.cylan.jiafeigou.cache.db.module.DownloadFile;

import java.util.List;

import rx.Observable;

/**
 * Created by holy on 2017/3/19.
 */

public interface IPanFileDbHelper {


    /**
     * 从表中读取
     *
     * @param time
     * @param lt    less than
     * @param count
     * @return
     */
    Observable<List<DownloadFile>> getFileFrom(String uuid, int time, boolean lt, int count);

    Observable<DownloadFile> getFileFrom(String uuid, int time);

    Observable<DownloadFile> getFileFrom(String uuid, String fileName);

    /**
     * 下一个准备好下载的文件
     *
     * @return
     */
    Observable<DownloadFile> getPreparedDownloadFile(String uuid);

    Observable<Long> updateOrSaveFile(DownloadFile downloadFile);

    Observable<Integer> getFileDownloadState(String uuid, String fileName);

    Observable<List<Long>> updateOrSaveFile(List<DownloadFile> downloadFileList);

    Observable<Long> removeFile(String uuid, String fileName);

    long insertFile(DownloadFile downloadFile);
}
