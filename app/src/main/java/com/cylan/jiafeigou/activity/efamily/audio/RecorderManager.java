package com.cylan.jiafeigou.activity.efamily.audio;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;

import com.cylan.jiafeigou.base.MyApp;

import java.io.IOException;

public class RecorderManager {
    static final private double EMA_FILTER = 0.6;
    private static final String TAG = "RecorderManager";

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;

    public RecorderManager() {
        mRecorder = new MediaRecorder();
        //指定音频来源（麦克风）
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //指定音频输出格式
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //指定音频编码方式
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        mRecorder.setAudioSamplingRate(44100);

    }


    public void start(String pathname) {
        try {
            //指定录制音频输出信息的文件
            mRecorder.setOutputFile(pathname);
            mRecorder.prepare();
            mRecorder.start();
            mEMA = 0.0;
        } catch (Exception e) {
        }

    }


    public void stop() {
        try {
            if (mRecorder != null) {
                mRecorder.setOnErrorListener(null);
                mRecorder.setOnInfoListener(null);
                mRecorder.setPreviewDisplay(null);
                mRecorder.stop();     // stop recording
                mRecorder.release();
                mRecorder = null;
            }
        } catch (Exception e) {
        }
    }

    private void pause() {
        if (mRecorder != null) {
            mRecorder.stop();
        }
    }

    private void start() {
        if (mRecorder != null) {
            mRecorder.start();
        }
    }

    public double getAmplitude() {
        try {
            if (mRecorder != null)
                return (mRecorder.getMaxAmplitude() / 2700.0);
            else
                return 0;
        } catch (IllegalStateException e) {
            return 0;
        }

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }


    public static int getWordsDuration(String path) {
        if (path == null || path.equals(""))
            return 0;
        MediaPlayer mediaPlayer = MediaPlayer.create(MyApp.getContext(), Uri.parse(path));
        int duration = 0;
        try {
            if (mediaPlayer == null)
                return duration;
            mediaPlayer.prepare();
        } catch (IllegalStateException e) {
        } catch (IOException e) {
        }
        duration = mediaPlayer.getDuration() / 1000;
        mediaPlayer.stop();
        return duration > 60 ? 60 : duration;
    }

}
