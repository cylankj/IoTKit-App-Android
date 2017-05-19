package com.cylan.jiafeigou.n.view.cam;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.widget.live.ILiveControl;

/**
 * 内容非常多的一个layout
 * A|返回|------------|speaker|mic|capture|  |
 * B|---------------------------- | 流量 |   |
 * |                                       |
 * |                 _________             |
 * C|                |loading  |            |
 * |                 ---------             |
 * D||安全防护|          |直播|时间|     |全屏| |
 * |                                        |
 * E|按钮|--------历史录像时间轴-----|直播|安全防护|
 * |                                        |
 * |----------------------------------------|
 * |
 * F|  speaker |     |mic|         |capture||
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Created by hds on 17-4-19.
 */

public interface ICamLiveLayer {

    /**
     * 屏幕比例
     *
     * @param ratio
     */
    void initLiveViewRect(float ratio, Rect rect);

    void initView(CamLiveContract.Presenter presenter, String uuid);

    /**
     * speaker*2,mic*2,capture*2
     */
    void initHotRect();

    void onLivePrepared(int type);

    void onLiveStart(CamLiveContract.Presenter presenter, Device device);

    void onLiveStop(CamLiveContract.Presenter presenter, Device device, int errCode);

    void orientationChanged(CamLiveContract.Presenter presenter, Device device, int orientation);

    void onRtcpCallback(int type, JFGMsgVideoRtcp rtcp);

    void onResolutionRsp(JFGMsgVideoResolution resolution);

    void onHistoryDataRsp(CamLiveContract.Presenter presenter);

    void onLiveDestroy();

    void onDeviceStandByChanged(Device device, View.OnClickListener clickListener);

    void onLoadPreviewBitmap(Bitmap bitmap);

    void onCaptureRsp(FragmentActivity activity, Bitmap bitmap);

    void setLoadingRectAction(ILiveControl.Action action);

    void onNetworkChanged(CamLiveContract.Presenter presenter, boolean connected);

    void onActivityStart(CamLiveContract.Presenter presenter, Device device);

//    void setCaptureListener(View.OnClickListener captureListener);

    void updateLiveViewMode(String mode);

    /***

     */
    void setHotSeatState(int liveType, boolean speaker,
                         boolean speakerEnable,
                         boolean mic,
                         boolean micEnable,
                         boolean capture, boolean captureEnable);

    void setHotSeatListener(View.OnClickListener micListener,
                            View.OnClickListener speakerListener,
                            View.OnClickListener captureListener);

    int getMicState();

    int getSpeakerState();


    void resumeGoodFrame();

    void startBadFrame();
}
