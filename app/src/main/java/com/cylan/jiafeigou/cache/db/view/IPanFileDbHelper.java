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
     * @param asc
     * @param count
     * @return
     */
    Observable<List<DownloadFile>> getFileFrom(int time, boolean asc, int count);

    Observable<Long> updateOrSaveFile(DownloadFile downloadFile);

    Observable<List<Long>> updateOrSaveFile(List<DownloadFile> downloadFileList);

}
