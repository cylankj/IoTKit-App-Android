package com.cylan.jiafeigou.n.view.cam;

/**
 * 内容非常多的一个layout
 * A|返回|------------|speaker|mic|capture|  |
 * B|---------------------------- | 流量 |   |
 *  |                                       |
 *  |                 _________             |
 * C|                |loading  |            |
 *  |                 ---------             |
 * D||安全防护|          |直播|时间|     |全屏| |
 *  |                                        |
 * E|按钮|--------历史录像时间轴-----|直播|安全防护|
 *  |                                        |
 *  |----------------------------------------|
 *  |
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

    void showLayoutTopBar(int visibility);

    void showLayoutFlow(int visibility);

    void showLayoutLoading(int visibility);

    void showLayoutTime(int visibility);

    void showLayoutWheel(int visibility);

    void showLayoutBottom(int visibility);

}
