package com.cylan.jiafeigou.support.headset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by hds on 17-5-3.
 */

public class HeadsetObserver {
    private static HeadsetObserver headsetObserver;

    public static HeadsetObserver getHeadsetObserver() {
        if (headsetObserver == null)
            synchronized (HeadsetObserver.class) {
                if (headsetObserver == null)
                    headsetObserver = new HeadsetObserver();
            }
        return headsetObserver;
    }

    private HeadSetReceiver headSetReceiver;
    private Map<String, HeadsetListener> map = new HashMap<>();

    private void init() {
        if (headSetReceiver == null)
            headSetReceiver = new HeadSetReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        try {
            ContextUtils.getContext().registerReceiver(headSetReceiver, intentFilter);
        } catch (Exception e) {
            AppLogger.e("err:" + MiscUtils.getErr(e));
        }
    }

    public boolean isHeadsetOn() {
        AudioManager am = (AudioManager) ContextUtils.getContext().getSystemService(Context.AUDIO_SERVICE);
        AppLogger.d("isMicrophoneMute: " + am.isMicrophoneMute());
        AppLogger.d("isMusicActive: " + am.isMusicActive());
        AppLogger.d("isBluetoothA2dpOn: " + am.isBluetoothA2dpOn());
        AppLogger.d("isSpeakerphoneOn: " + am.isSpeakerphoneOn());
        AppLogger.d("isWiredHeadsetOn: " + am.isWiredHeadsetOn());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            AppLogger.d("isVolumeFixed: " + am.isVolumeFixed());
        return am.isWiredHeadsetOn();
    }

    private void clean() {
        try {
            if (headSetReceiver == null) return;
            ContextUtils.getContext().unregisterReceiver(headSetReceiver);
        } catch (Exception e) {
            AppLogger.e("err:" + MiscUtils.getErr(e));
        }
    }

    public void addObserver(HeadsetListener listener) {
        if (listener == null) return;
        if (headSetReceiver == null) init();
        map.put(listener.getClass().getSimpleName(), listener);
    }

    public HeadsetListener removeObserver(HeadsetListener listener) {
        if (listener == null) return null;
        if (map == null || map.size() == 0) {
            clean();
        }
        return map.remove(listener.getClass().getSimpleName());
    }

    public interface HeadsetListener {
        void onHeadSetPlugIn(boolean plugIn);
    }

    private class HeadSetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                boolean isOn = intent.getIntExtra("state", 0) == 1;
                Iterator<String> keySet = map.keySet().iterator();
                while (keySet.hasNext()) {
                    String next = keySet.next();
                    map.get(next).onHeadSetPlugIn(isOn);
                }
//                if (intent.getIntExtra("state", 0) == 0) {
//
//                } else if (intent.getIntExtra("state", 0) == 1) {
//
//                }
            }
        }
    }
}
