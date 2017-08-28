package com.cylan.jiafeigou.n.view.bell;

import android.media.MediaPlayer;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ContextUtils;

/**
 * Created by yanzhendong on 2017/8/28.
 */

public class MediaPlayerManager {

    private static MediaPlayerManager instance;

    private MediaPlayer mediaPlayer;
    private int doorbellID;

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
        mediaPlayer = MediaPlayer.create(ContextUtils.getContext(), R.raw.doorbell_called);

    }


    public void play() {
//        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundPool.play(sampleId, 1, 1, 1, -1, 1));
//        soundPool.load(ContextUtils.getContext(), R.raw.doorbell_called, 1);

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(0, 0);
            mediaPlayer.stop();
        }
        mediaPlayer.setVolume(1, 1);
        mediaPlayer.start();

    }

    public void stop() {
//        soundPool.stop(doorbellID);
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(0, 0);
            mediaPlayer.stop();
        }
    }
}
