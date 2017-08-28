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

    }


    public synchronized void play() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(ContextUtils.getContext(), R.raw.doorbell_called);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public synchronized void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(0, 0);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public synchronized void setVoice(int i) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(i, i);
        }
    }
}
