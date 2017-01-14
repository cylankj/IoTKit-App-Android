package com.cylan.jiafeigou.support.download.report.listener;

/**
 * Created by Majid Golshadi on 4/20/2014.
 */
public interface DownloadManagerListener {

    void onDownloadStarted(long taskId);

    void onDownloadPaused(long taskId);

    void onDownloadProcess(long taskId, double percent, long downloadedLength);

    void onDownloadFinished(long taskId);

    void onDownloadRebuildStart(long taskId);

    void onDownloadRebuildFinished(long taskId);

    void onDownloadCompleted(long taskId);

//    void connectionLost(long taskId);

    void onFailedReason(long taskId, FailReason reason);

}
