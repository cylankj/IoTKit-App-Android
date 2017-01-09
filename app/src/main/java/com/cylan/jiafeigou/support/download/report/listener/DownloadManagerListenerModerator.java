package com.cylan.jiafeigou.support.download.report.listener;

/**
 * Created by Majid Golshadi on 4/21/2014.
 */
public class DownloadManagerListenerModerator {

    private DownloadManagerListener downloadManagerListener;

    public DownloadManagerListenerModerator(DownloadManagerListener listener) {
        downloadManagerListener = listener;
    }

    public void OnDownloadStarted(long taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.onDownloadStarted(taskId);
        }
    }

    public void OnDownloadPaused(long taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.onDownloadPaused(taskId);
        }
    }

    public void onDownloadProcess(long taskId, double percent, long downloadedLength) {
        if (downloadManagerListener != null) {
            downloadManagerListener.onDownloadProcess(taskId, percent, downloadedLength);
        }
    }

    public void OnDownloadFinished(long taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.onDownloadFinished(taskId);
        }
    }

    public void OnDownloadRebuildStart(long taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.onDownloadRebuildStart(taskId);
        }
    }


    public void OnDownloadRebuildFinished(long taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.onDownloadRebuildFinished(taskId);
        }
    }

    public void OnDownloadCompleted(long taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.onDownloadCompleted(taskId);
        }
    }

    public void onFailed(long taskId, FailReason failReason) {
        if (downloadManagerListener != null) {
            downloadManagerListener.onFailedReason(taskId, failReason);
        }
    }
}
