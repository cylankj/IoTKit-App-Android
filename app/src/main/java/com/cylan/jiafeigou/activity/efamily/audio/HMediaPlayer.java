package com.cylan.jiafeigou.activity.efamily.audio;

import android.media.AudioManager;
import android.media.MediaPlayer;

import cylan.log.DswLog;

import java.io.IOException;


/**
 * Created by hunt on 15-2-15. hunt
 */
public class HMediaPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private Object tag = null;
    private String mPath;
    private MediaPlayer mMediaPlayer;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean isStopped = true;

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public HMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    public void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    public boolean isStopped() {
        return isStopped;
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        try {
            release();
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
        setRunning(false);
        if (mediaStateListener != null)
            mediaStateListener.onComplete(mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        setRunning(false);
        if (mediaStateListener != null)
            mediaStateListener.onFileFailed(mp, what, extra);
        return true;
    }


    /**
     * @param path: media path
     * @return
     */
    public void setPath(String path) {
        this.mPath = path;
    }


    public int getProgress() {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && mMediaPlayer.getDuration() != 0)
                return mMediaPlayer.getCurrentPosition() * 100 / mMediaPlayer.getDuration();
        } catch (Exception e) {
        }
        return 0;
    }

    public void pause() {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                setRunning(true);
                setPaused(true);
                if (mediaStateListener != null)
                    mediaStateListener.onPause();
            }
        } catch (Exception e) {

        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getPath() {
        return mPath;
    }

    private boolean InitMediaPlayer() throws Exception {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(this);
        }
        try {
            loadResource();
        } catch (IOException e) {
            release();
            return false;
        } catch (IllegalArgumentException e) {
            release();
            return false;
        } catch (IllegalStateException e) {
            release();
            return false;
        }
        return true;
    }

    /**
     * @return
     */

    private void loadResource() throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mMediaPlayer.setDataSource(mPath);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    /**
     * if media is pause ,so resume else start a new media
     */
    public boolean start() {
        setStopped(false);
        setPaused(false);
        if (isRunning()) {
            mMediaPlayer.start();
            if (mediaStateListener != null)
                mediaStateListener.onPlay();
        } else {
            try {
                if (!InitMediaPlayer())
                    return false;
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                setRunning(true);
                if (mediaStateListener != null)
                    mediaStateListener.onPlay();
            } catch (Exception e) {
                DswLog.ex(e.toString());
                DswLog.d("file is wrong!!!!");
                setStopped(true);
                if (mediaStateListener != null)
                    mediaStateListener.onFileFailed(this.mMediaPlayer, MediaPlayer.MEDIA_ERROR_IO, 0);
                return false;
            }
        }
//        mPath = null;
        return true;
    }

    public void stop() {
        try {
            if (mMediaPlayer != null) {
                setStopped(true);
                mMediaPlayer.stop();
            }
        } catch (IllegalStateException i) {
            DswLog.d("hMediaPlayer,stop failed:" + i);
        }
    }

    public void release() throws Exception {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }
        setStopped(true);
        mPath = null;
    }

    public MediaPlayer getInstance() {
        return mMediaPlayer;
    }

    private OnMediaStateListener mediaStateListener;

    public void setMediaStateListener(OnMediaStateListener listener) {
        mediaStateListener = listener;
    }

    public interface OnMediaStateListener {
        void onFileFailed(MediaPlayer mp, int what, int extra);

        void onComplete(MediaPlayer mp);

        void onPlay();

        void onPause();
    }

}
