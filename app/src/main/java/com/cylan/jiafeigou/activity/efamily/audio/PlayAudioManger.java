package com.cylan.jiafeigou.activity.efamily.audio;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import cylan.log.DswLog;
import com.cylan.jiafeigou.activity.efamily.main.PlayOrStopAudioLIstener;
import com.cylan.jiafeigou.entity.WordsBean;

import java.io.File;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-12-09
 * Time: 16:49
 */

public class PlayAudioManger {


    private static final String TAG = "PlayAudioManger";

    private MediaPlayer mMediaPlayer;

    private WordsBean b;

    private PlayOrStopAudioLIstener mListener;


    public PlayAudioManger(PlayOrStopAudioLIstener listner) {
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.v(TAG, "onCompletion");
                mListener.stop(b);

            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                Log.v(TAG, "onError");
                mListener.stop(b);
                return false;
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.v(TAG, "onPrepared");
                mp.start();
                mListener.play(b);
            }
        });

        this.mListener = listner;
    }

    public void play(File file, WordsBean bean) {
        stop(b);
        b = bean;
        if (!doPrepare(file)) {
            mListener.stop(bean);
        }
    }

    private boolean doPrepare(File soundFile) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(soundFile.getAbsolutePath());
            mMediaPlayer.prepare();
            return true;
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
        return false;
    }

    public void stop(WordsBean mBean) {
        if (mBean != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.seekTo(0);
            mListener.stop(mBean);
            b = null;
        }
    }
}
