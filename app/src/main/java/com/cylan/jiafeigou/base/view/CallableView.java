package com.cylan.jiafeigou.base.view;

/**
 * Created by yzd on 16-12-30.
 */

public interface CallableView extends ViewableView {

    void onPickup();//接听呼叫

    void onCallAnswerInOther();//其他设备已接听

    void onNewCallWhenInLive(String person);//正在接听中又来了新的呼叫

    void onPreviewPicture(String picture);

    void onListen();

    void onNewCallTimeOut();
}
