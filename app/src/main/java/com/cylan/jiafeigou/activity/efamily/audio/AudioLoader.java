package com.cylan.jiafeigou.activity.efamily.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.cylan.jiafeigou.R;
import cylan.log.DswLog;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.entity.msg.EfamlMsg;
import com.cylan.jiafeigou.utils.ThreadPoolUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-13
 * Time: 09:42
 */

public class AudioLoader {

    private static final String TAG = "AudioLoader";


    private static final int STATE_DOWNCOMPLETE = 0x00;
    private static final int STATE_START = 0x01;
    private static final int STATE_STOP = 0x02;
    private static final int STATE_NO_FILE = 0x03;


    private AudioLoadCallback mCallback;
    private String mCid;
    private Handler mMainThreadHandler = null;
    private MediaPlayer mMediaPlayer = null;
    private Context mContext ;
    private AudioManager audioManager ;


    public AudioLoader(Context context, String cid, AudioLoadCallback callback) {
        this.mCid = cid;
        this.mCallback = callback;
        this.mContext = context;
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public void loadAudio(final EfamlMsg mEfamlMsg) {
        if (mEfamlMsg == null)
            return;
        stopPlaying();
        String path = mEfamlMsg.filePath(mCid);
        File file = new File(path);
        if (mMainThreadHandler == null) {
            mMainThreadHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what) {
                        case STATE_DOWNCOMPLETE:
                            stopPlaying();
                            startPlaying((EfamlMsg)msg.obj);
                            break;
                        case STATE_STOP:
                            mCallback.stopOther();
                            stopPlaying();
                            audioManager.abandonAudioFocus(afChangeListener);
                            break;
                        case STATE_START:
                            stopPlaying();
                            startPlaying((EfamlMsg)msg.obj);
                            break;
                        case STATE_NO_FILE:
                            mCallback.stopNoFile();
                            break;
                    }
                    return true;
                }
            });
        }
        if (file.exists()) {
            DswLog.v(TAG + "--TOPLAY");
            mMainThreadHandler.obtainMessage(STATE_DOWNCOMPLETE, mEfamlMsg).sendToTarget();
        } else {
            DswLog.v(TAG + "--TODOWNLOAD");
            ThreadPoolUtils.execute(getTask(mEfamlMsg));
        }
    }


    private synchronized Runnable getTask(final EfamlMsg bean) {
        return new Runnable() {
            @Override
            public void run() {
                if (!Environment.getExternalStorageDirectory().exists()) {
                    ToastUtil.showFailToast(MyApp.getContext(), MyApp.getContext().getString(R.string.has_not_sdcard));
                    mMainThreadHandler.sendEmptyMessage(STATE_STOP);
                    return;
                }
                try {
                    System.setProperty("http.keepAlive", "false");// 解决经常报此异常问题，at
                    // java.util.zip.GZIPInputStream.readFully(GZIPInputStream.java:214)
                    URL Url = new URL(bean.downUrl(mCid));
                    URLConnection conn = Url.openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    if (is == null) { // 没有下载流
                        mMainThreadHandler.sendEmptyMessage(STATE_STOP);
                        DswLog.e(TAG + "--没有下载流");
                    }
                    FileOutputStream FOS = new FileOutputStream(bean.filePath(mCid)); // 创建写入文件内存流，通过此流向目标写文件

                    byte buf[] = new byte[1024];
                    int numread;
                    while ((numread = is.read(buf)) != -1) {
                        FOS.write(buf, 0, numread);
                    }
                    is.close();
                    DswLog.v(TAG + "--download finish");
                    mMainThreadHandler.obtainMessage(STATE_DOWNCOMPLETE, bean).sendToTarget();
                    FOS.flush();
                    if (FOS != null) {
                        FOS.close();
                    }
                } catch (Exception e) {
                    DswLog.ex(e.toString());
                    DswLog.e(TAG + "--Exception");
                    mMainThreadHandler.sendEmptyMessage(STATE_STOP);
                    mMainThreadHandler.sendEmptyMessage(STATE_NO_FILE);
                }

            }

        };

    }


    private void startPlaying(final EfamlMsg msg) {
        if (requestFocus()){
            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(msg.filePath(mCid));
                mMediaPlayer.prepare();

                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        DswLog.e(TAG + "prepare() success");
                        mMediaPlayer.start();
                    }
                });
            } catch (IOException e) {
                mMainThreadHandler.sendEmptyMessage(STATE_STOP);
                DswLog.e(TAG + "prepare() failed");
            }

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    DswLog.e(TAG + "onCompletion");
                    mMainThreadHandler.sendEmptyMessage(STATE_STOP);
                }
            });
        }

    }

    public void stopPlaying() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            DswLog.e(TAG + "stopPlaying");
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void resume(){
        if (mMediaPlayer != null)
            mMediaPlayer.start();
    }

    private void pause(){
        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
            mMediaPlayer.pause();
    }

    private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange){
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    audioManager.abandonAudioFocus(afChangeListener);
                    stopPlaying();
                    break;
                default:
                    break;
            }
        }
    };

    private boolean requestFocus(){
        int result = audioManager.requestAudioFocus(afChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }
}