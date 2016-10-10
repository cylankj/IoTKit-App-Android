// IRemoteServiceCallBack.aidl
package com.cylan.jiafeigou;

// Declare any non-default types here with import statements

interface IRemoteServiceCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    void onDownloadStarted(long taskId);

    void onDownloadPaused(long taskId);

    void onDownloadProcess(long taskId, double percent, long downloadedLength);

    void onDownloadFinished(long taskId);

    void onDownloadRebuildStart(long taskId);

    void onDownloadRebuildFinished(long taskId);

    void onDownloadCompleted(long taskId);

    void onFailedReason(long taskId, int reason);
}
