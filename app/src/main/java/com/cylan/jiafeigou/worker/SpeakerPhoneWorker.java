package com.cylan.jiafeigou.worker;

import android.media.AudioManager;
import android.os.SystemClock;

/**
 * Created by hebin on 2015/10/19.
 */
public class SpeakerPhoneWorker implements Runnable {

    private AudioManager audioManager;
    private boolean isConnect;

    public SpeakerPhoneWorker(boolean isConnectHeadsetPlug, AudioManager am) {
        this.isConnect = isConnectHeadsetPlug;
        this.audioManager = am;
    }

    @Override
    public void run() {
        SystemClock.sleep(1000);
        if (isConnect) {
            audioManager.setSpeakerphoneOn(false);
        } else {
            audioManager.setSpeakerphoneOn(true);
        }
    }
}
