package com.cylan.jiafeigou.n.mvp.impl.cloud;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveLeaveMesBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveMesgBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveVideoTalkBean;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CloudLiveVoiceTalkView;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.sina.weibo.sdk.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLivePresenterImp extends AbstractPresenter<CloudLiveContract.View> implements CloudLiveContract.Presenter {

    private int val1;
    private int val2;
    private Subscription talkSub;
    private int BASE = 600;

    private MediaRecorder mMediaRecorder;
    public static final int MAX_LENGTH = 1000 * 60 * 10;// 最大录音时长;
    private File filePath;
    private long startTime;
    private long endTime;

    private String output_Path= Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator+"luyin.3gp";


    private static final String BASE_DB ="base_db";
    private static final String LEAVE_MESG_DB ="leave_mesg_db";
    private static final String VIDEO_TALK_DB ="video_talk_db";


    private DbUtils base_db;
    private DbUtils leave_mesg_db;
    private DbUtils video_talk_db;

    public CloudLivePresenterImp(CloudLiveContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (talkSub != null){
            talkSub.unsubscribe();
        }
    }

    @Override
    public void startTalk() {
        talkSub = Observable.interval(500,200,TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<Long, Double>() {
                    @Override
                    public Double call(Long aLong) {
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Double>() {
                    @Override
                    public void call(Double value) {
                        int val = updateMicStatus();
                        val1=val+12;
                        val2=val;
                        if(getView() != null)
                        getView().refreshView(val1,val2);
                    }
                });
    }

    @Override
    public String startRecord() {

        if (mMediaRecorder != null){
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder=null;
        }

        try {
            filePath = new File(output_Path);
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);

            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mMediaRecorder.setOutputFile(filePath.getAbsolutePath());
            mMediaRecorder.setMaxDuration(MAX_LENGTH);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            startTime = System.currentTimeMillis();

        } catch (IllegalStateException e) {

        } catch (IOException e) {

        }

        return filePath.getAbsolutePath();
    }

    private int updateMicStatus() {
        int voiceVal = 0;
        if (mMediaRecorder != null) {
            double ratio = (double)mMediaRecorder.getMaxAmplitude()/BASE;
            double db = 0;// 分贝
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
             voiceVal = (int) db;
        }
        return voiceVal;
    }

    @Override
    public void stopRecord() {
        if (mMediaRecorder == null)
            return;
        endTime = System.currentTimeMillis();
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    @Override
    public boolean checkSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    /**
     * desc:添加一个数据
     */
    @Override
    public void addMesgItem(CloudLiveBaseBean bean) {
        if(getView() != null)
        getView().refreshRecycleView(bean);
    }

    @Override
    public CloudLiveBaseBean creatMesgBean() {
        return new CloudLiveBaseBean();
    }

    @Override
    public String getLeaveMesgLength() {
        String timeLength = "";
        long time = (endTime - startTime)/1000;
        if(time / 60 == 0){
            timeLength = time+"''";
        }else {
            timeLength = time/60 + "'"+time%60+"''";
        }
        return timeLength;
    }

    @Override
    public String parseTime(String times) {
        long timem = Long.parseLong(times);
        Date time = new Date(timem);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm");
        String dateString = formatter.format(time);
        return dateString;
    }

    /**
     * desc:创建数据库
     */
    @Override
    public DbUtils createBaseDB() {
        DbUtils.DaoConfig config = new DbUtils.DaoConfig(getView().getContext());
        config.setDbName(BASE_DB); //db名
        config.setDbVersion(1);  //db版本
        config.setDbUpgradeListener(new MyDbLisenter());
        base_db = DbUtils.create(config);
        base_db.configAllowTransaction(true);    //开启事物

        DbUtils.DaoConfig leave_db_config = new DbUtils.DaoConfig(getView().getContext());
        leave_db_config.setDbName(LEAVE_MESG_DB);
        leave_db_config.setDbVersion(1);
        leave_db_config.setDbUpgradeListener(new MyDbLisenter());
        leave_mesg_db = DbUtils.create(leave_db_config);
        leave_mesg_db.configAllowTransaction(true);

        DbUtils.DaoConfig video_talk_db_config = new DbUtils.DaoConfig(getView().getContext());
        video_talk_db_config.setDbName(LEAVE_MESG_DB);
        video_talk_db_config.setDbVersion(1);
        video_talk_db_config.setDbUpgradeListener(new MyDbLisenter());
        video_talk_db = DbUtils.create(video_talk_db_config);
        video_talk_db.configAllowTransaction(true);

        try {
            base_db.createTableIfNotExist(CloudLiveBaseBean.class); //创建一个表
            leave_mesg_db.createTableIfNotExist(CloudLiveLeaveMesBean.class);
            video_talk_db.createTableIfNotExist(CloudLiveVideoTalkBean.class);
        } catch (DbException e) {
            e.printStackTrace();
        }

        return base_db;
    }

    public class MyDbLisenter implements DbUtils.DbUpgradeListener{

        @Override
        public void onUpgrade(DbUtils dbUtils, int oldVersion, int newVersion) {
            try {
                if (oldVersion == 1 && newVersion == 2) {
                    String sql = "ALTER TABLE " + dbUtils.getDaoConfig().getDbName()
                            + " ADD COLUMN TEMP TEXT";
                    dbUtils.execNonQuery(sql);
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

}
