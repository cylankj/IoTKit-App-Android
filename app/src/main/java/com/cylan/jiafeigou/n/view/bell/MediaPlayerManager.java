package com.cylan.jiafeigou.n.view.bell;

import android.media.AudioManager;
import android.media.SoundPool;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ContextUtils;

/**
 * Created by yanzhendong on 2017/8/28.
 */

public class MediaPlayerManager {

    private static MediaPlayerManager instance;

    private SoundPool soundPool;
    private final int doorbellID;

    public static MediaPlayerManager getInstance() {
        if (instance == null) {
            synchronized (MediaPlayerManager.class) {
                if (instance == null) {
                    instance = new MediaPlayerManager();
                }
            }
        }
        return instance;
    }

    private MediaPlayerManager() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        doorbellID = soundPool.load(ContextUtils.getContext(), R.raw.doorbell_called, 1);
    }


    public void play() {
        soundPool.play(doorbellID, 1, 1, 1, -1, 1);
    }

    public void stop() {
        soundPool.stop(doorbellID);
    }
}
