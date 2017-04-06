package com.cylan.jiafeigou.n.mvp.impl.cloud;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.db.DataBaseUtil;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseDbBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;
import com.cylan.jiafeigou.utils.CloseUtils;
import com.cylan.jiafeigou.utils.NetUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLivePresenterImp extends AbstractPresenter<CloudLiveContract.View> implements CloudLiveContract.Presenter {

    private int val1;
    private int val2;
    private int BASE = 600;

    private MediaRecorder mMediaRecorder;
    private MediaPlayer mPlayer;
    public static final int MAX_LENGTH = 1000 * 60 * 10;// 最大录音时长;
    private File filePath;
    private long startTime;
    private long endTime;
    private String uuid;

    private String output_Path = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + System.currentTimeMillis() + "luyin.3gp";

    private DbManager base_db;

    private Subscription checkDeviceOnLineSub;
    private Subscription leaveMesgSub;
    private CompositeSubscription subscription;
    private String userIcon;

    public CloudLivePresenterImp(CloudLiveContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
    }

    @Override
    public void start() {
        super.start();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        } else {
            subscription = new CompositeSubscription();
            subscription.add(getAccount());
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (checkDeviceOnLineSub != null) {
            checkDeviceOnLineSub.unsubscribe();
        }
        if (leaveMesgSub != null) {
            leaveMesgSub.unsubscribe();
        }
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        stopPlayRecord();
    }

    /**
     * 录音动画
     */
    @Override
    public void startTalkAnimation() {
        rx.Observable.interval(500, 200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object value) {
                        int val = updateMicStatus();
                        val1 = val + 12;
                        val2 = val;
                        if (getView() != null)
                            getView().refreshView(val1, val2);
                    }
                });
    }

    /**
     * 开始录音
     *
     * @return
     */
    @Override
    public String startRecord() {

        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
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
            double ratio = (double) mMediaRecorder.getMaxAmplitude() / BASE;
            double db = 0;// 分贝
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
            voiceVal = (int) db;
        }
        return voiceVal;
    }

    /**
     * 停止录音
     */
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

    /**
     * 播放录音
     *
     * @param mFileName
     */
    @Override
    public void playRecord(String mFileName) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    getView().stopPlayVoiceAnim();
                }
            });
        } catch (IOException e) {

        }
    }

    /**
     * 停止播放录音
     */
    @Override
    public void stopPlayRecord() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.stop();
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
    }

    /**
     * 检测SD卡能用否
     *
     * @return
     */
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
        if (getView() != null)
            getView().refreshRecycleView(bean);
        getView().scrollToLast();
    }

    @Override
    public CloudLiveBaseBean creatMesgBean() {
        return new CloudLiveBaseBean();
    }

    /**
     * 获取录音的时长
     *
     * @return
     */
    @Override
    public String getLeaveMesgLength() {
        String timeLength = "";
        long time = (endTime - startTime) / 1000;
        if (time / 60 == 0) {
            timeLength = time + "''";
        } else {
            timeLength = time / 60 + "'" + time % 60 + "''";
        }
        return timeLength;
    }

    @Override
    public String parseTime(long times) {
        Date time = new Date(times);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm");
        String dateString = formatter.format(time);
        return dateString;
    }

    /**
     * desc:创建数据库
     */
    @Override
    public void getDBManger(String dbName) {
        base_db = DataBaseUtil.getInstance(dbName).dbManager;
    }

    /**
     * 类转换成字节
     *
     * @param s
     * @return
     */
    @Override
    public byte[] getSerializedObject(Serializable s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(s);
        } catch (IOException e) {
            return null;
        } finally {
            CloseUtils.close(oos);
        }
        byte[] result = baos.toByteArray();
        return result;
    }

    /**
     * 字节转换成类
     *
     * @param in
     * @return
     */
    @Override
    public Object readSerializedObject(byte[] in) {
        Object result = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(in);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bais);
            result = ois.readObject();
        } catch (Exception e) {
            result = null;
        } finally {
            CloseUtils.close(ois);
        }
        return result;
    }

    @Override
    public void saveIntoDb(CloudLiveBaseDbBean bean) {
        try {
            if (base_db == null) return;
            base_db.save(bean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<CloudLiveBaseDbBean> findAllFromDb() {
        List<CloudLiveBaseDbBean> allData = new ArrayList<>();
        try {
            List<CloudLiveBaseDbBean> tempAll = base_db.findAll(CloudLiveBaseDbBean.class);
            if (tempAll != null && tempAll.size() > 0) {
                allData.addAll(tempAll);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        return allData;
    }


    /**
     * 点击视频通话处理
     */
    @Override
    public void handlerVideoTalk() {
        checkDeviceOnLineSub = Observable.just(null)
                .map(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        //TODO 检测设备是否离线；true离线、false在线
                        return true;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        getView().handlerVideoTalkResult(aBoolean);
                    }
                });
    }

    /**
     * 点击语音留言处理
     */
    @Override
    public void handlerLeveaMesg() {
        leaveMesgSub = Observable.just(null)
                .map(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        //TODO 检查设备是否在线0
                        return true;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        getView().showVoiceTalkDialog(aBoolean);
                    }
                });
    }

    /**
     * 获取账号信息用于创建数据库
     */
    @Override
    public Subscription getAccount() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null && getUserInfo instanceof RxEvent.GetUserInfo) {
                            if (getView() != null) {
                                getDBManger(getUserInfo.jfgAccount.getAccount());
                                initData(getUserInfo.jfgAccount.getPhotoUrl());
                                userIcon = getUserInfo.jfgAccount.getPhotoUrl();
                            }
                        }
                    }
                });
    }

    /**
     * 初始化消息列表的数据
     */
    @Override
    public void initData(String userIcon) {
        List<CloudLiveBaseBean> list = new ArrayList<>();
        List<CloudLiveBaseDbBean> fromAllDb = findAllFromDb();
        if (fromAllDb != null && fromAllDb.size() > 0) {
            for (CloudLiveBaseDbBean dBbean : fromAllDb) {
                if (!TextUtils.isEmpty(dBbean.uuid) && uuid.equals(dBbean.uuid)) {
                    CloudLiveBaseBean newBean = new CloudLiveBaseBean();
                    newBean.setType(dBbean.getType());
                    newBean.setUserIcon(userIcon);
                    newBean.setData(readSerializedObject(dBbean.getData()));
                    list.add(newBean);
                }
            }
        }
        if (getView() != null) {
            handlerDataResult(list);
        }
    }

    /**
     * 检测录音的权限
     */
    @Override
    public boolean checkRecordPermission() {
        if (ContextCompat.checkSelfPermission(getView().getContext(),
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getUserIcon() {
        return userIcon;
    }

    /**
     * 设备是否在线
     *
     * @return
     */
    @Override
    public boolean isDeviceOnline() {
        DpMsgDefine.DPNet net = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_201_NET,null);
        return net != null && JFGRules.isDeviceOnline(net)
                && NetUtils.getJfgNetType(getView().getContext()) != 0;
    }

    /**
     * 处理数据的结果
     *
     * @param list
     */
    private void handlerDataResult(List<CloudLiveBaseBean> list) {
        if (list.size() == 0) {
            getView().showNoMesg();
            getView().initRecycleView(list);
        } else {
            getView().hideNoMesg();
            getView().initRecycleView(list);
        }
    }
}
