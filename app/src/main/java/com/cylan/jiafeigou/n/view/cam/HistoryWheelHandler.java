package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.DatePickerDialogFragment;
import com.cylan.jiafeigou.widget.wheel.ex.DataExt;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_ADSORB;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_DRAGGING;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_FINISH;

//import com.cylan.jiafeigou.utils.TimeUtils;


/**
 * Created by hds on 17-4-26.
 */

public class HistoryWheelHandler implements SuperWheelExt.WheelRollListener {
    private SuperWheelExt superWheelExt;
    private CamLiveContract.Presenter presenter;
    private Context context;
    private WeakReference<DatePickerDialogFragment> datePickerRef;
    private String uuid;

    public long getLastUpdateTime() {
        return superWheelExt.getLastUpdateTime();
    }

    public long getNextTimeDistance() {
        return superWheelExt.getNextTimeDistance();
    }

    public HistoryWheelHandler(SuperWheelExt superWheel, CamLiveContract.Presenter presenter) {
        this.superWheelExt = superWheel;
        superWheelExt.setWheelRollListener(this);
        this.presenter = presenter;
        context = superWheel.getContext();
        uuid = presenter.getUuid();
    }

    public void dateUpdate() {
        if (presenter.getHistoryDataProvider() == null) {
            AppLogger.d("历史录像没准备好");
            return;
        }
        superWheelExt.setDataProvider(presenter.getHistoryDataProvider());
    }

    public void showDatePicker(boolean isLand) {
        showPortDatePicker();
    }


    private void showPortDatePicker() {
        Bundle bundle = new Bundle();
        bundle.putString(BaseDialog.KEY_TITLE, context.getString(R.string.TIME));
        DatePickerDialogFragment fragment = DatePickerDialogFragment.newInstance(bundle);
        fragment.setAction((int id, Object value) -> {
            if (value != null && value instanceof Long) {
                IData data = presenter.getHistoryDataProvider();
                HistoryFile historyFile = data == null ? null : data.getMaxHistoryFile();
                //时间轴上没有
                if (historyFile == null || historyFile.getTime() + historyFile.getDuration() < (long) value / 1000) {
                    historyFile = History.getHistory().getHistoryFile((Long) value);
                    if (historyFile == null) {
                        AppLogger.d("没有这段视频: " + historyFile + "," + value);
                        ToastUtil.showToast(ContextUtils.getContext().getString(R.string.Historical_No));
                        return;
                    } else {
                        //需要重新回执时间轴

                    }
                }

                AppLogger.d("msgTime pick: " + History.date2String((Long) value) + "," + value);
                if (datePickerListener != null) {
                    datePickerListener.onPickDate((Long) value, STATE_FINISH);
                }
                playPreciseByTime((Long) value);
            }
        });
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        fragment.setTimeZone(JFGRules.getDeviceTimezone(device));
        fragment.setTimeFocus(getWheelCurrentFocusTime());
        fragment.setDateList(presenter.getFlattenDateList());
        fragment.show(((FragmentActivity) context).getSupportFragmentManager(),
                "DatePickerDialogFragment");
    }

    private long getWheelCurrentFocusTime() {
        return superWheelExt.getCurrentFocusTime();
    }

    /**
     * 选择一天,load所有的数据,但是需要移动的这一天的开始位置.
     */
    private void playPreciseByTime(long timeStart) {
        final String date = History.parseTime2Date(TimeUtils.wrapToLong(timeStart));
        ArrayList<HistoryFile> hList = History.getHistory().getHistoryFile(date);
        if (ListUtils.isEmpty(hList)) {
            AppLogger.d("没有这天的数据啊，查");
            Subscription subscription = RxBus.getCacheInstance().toObservable(RxEvent.JFGHistoryVideoParseRsp.class)
                    .filter(rsp -> TextUtils.equals(rsp.uuid, uuid))
                    .timeout(30, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.computation())
                    .subscribe(rsp -> {
                        ArrayList<HistoryFile> files = History.getHistory().getHistoryFile(date);
                        DataExt.getInstance().flattenData(files, JFGRules.getDeviceTimezone(presenter.getDevice()));
                        playByTime(timeStart);
                    }, throwable -> AppLogger.e("失败了?" + MiscUtils.getErr(throwable)));
            presenter.addSubscription("playPreciseByTime", subscription);
            presenter.fetchHistoryDataList(timeStart);
            return;
        }
        playByTime(timeStart);
    }

    private void playByTime(long timeStart) {
        presenter.assembleTheDay(timeStart)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> AppLogger.d("reLoad hisData: good"))
                .subscribe(iData -> {
                    setupHistoryData(iData);
                    HistoryFile historyFile = iData.getMinHistoryFileByStartTime(timeStart);//最小时间.
                    if (historyFile != null) {
                        //需要使用post，因为上面的setupHistoryData是post
                        setNav2Time(TimeUtils.wrapToLong(timeStart), true);
                        presenter.startPlayHistory(TimeUtils.wrapToLong(timeStart));
                        AppLogger.d("找到历史录像?" + historyFile);
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    public void setNav2Time(long time, boolean post) {
        if (!post) {
            setNav2Time(time);
        } else {
            superWheelExt.setPositionByTimePost(time, true);
        }
    }

    public void setNav2Time(long time) {
        superWheelExt.setPositionByTime(TimeUtils.wrapToLong(time), false);
    }

    public boolean isBusy() {
        return superWheelExt.isBusy();
    }

    public void setupHistoryData(IData dataProvider) {
        final long time = System.currentTimeMillis();
        superWheelExt.setDataProvider(dataProvider);
        superWheelExt.setWheelRollListener(this);
        Log.d("performance", "CamLivePortWheel performance: " + (System.currentTimeMillis() - time));
    }

    @Override
    public void onWheelTimeUpdate(long time, int state) {

        switch (state) {
            case STATE_DRAGGING:
                Log.d("onTimeUpdate", "STATE_DRAGGING :" + History.date2String(time) + ",time:" + time);
//                if (datePickerListener != null)
//                    datePickerListener.onPickDate(time / 1000, STATE_DRAGGING);
//                superWheelExt.removeCallbacks(dragRunnable);
                break;
            case STATE_ADSORB:
                Log.d("onTimeUpdate", "STATE_ADSORB :" + History.date2String(time) + ",time:" + time);
                break;
            case STATE_FINISH:
                Log.d("onTimeUpdate", "STATE_FINISH :" + History.date2String(time) + ",time:" + time);
//                tmpTime = time;
//                superWheelExt.removeCallbacks(dragRunnable);
//                superWheelExt.postDelayed(dragRunnable, 700);
                presenter.startPlayHistory(time);
                if (datePickerListener != null) {
                    datePickerListener.onPickDate(time / 1000, STATE_FINISH);
                }
                AppLogger.d("拖动停止了:" + time + "," + History.date2String(time));
                break;
            default:
                break;
        }
    }

    //    private long tmpTime;
    private Runnable dragRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };

    public void setDatePickerListener(DatePickerListener datePickerListener) {
        this.datePickerListener = datePickerListener;
    }

    private DatePickerListener datePickerListener;

    public boolean isDragging() {
        return false;
    }

    public long getNextFocusTime(long time) {
        return superWheelExt.getNextFocusTime(time);
    }

    /**
     * 选择日期,滚动条变化.都要通知.
     */
    public interface DatePickerListener {
        void onPickDate(long time, int state);
    }

}
