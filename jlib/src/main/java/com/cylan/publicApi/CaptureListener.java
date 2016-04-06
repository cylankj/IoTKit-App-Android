package com.cylan.publicApi;

public interface CaptureListener {
//    /**
//     * this method should be called in the CameraManager
//     *
//     * @param data
//     * @param length
//     * @param captureObject
//     */
//    public void startRecord(byte[] data, Camera c, PreviewCallback call);
//
//    public void startLive(byte[] data, Camera c, long captureObject, PreviewCallback call);

    /**
     * 从vidiocapture传出去图像数据。
     *
     * @param data
     */
    public void onFrame(byte[] data);
}
