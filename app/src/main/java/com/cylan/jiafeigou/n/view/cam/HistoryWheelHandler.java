package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.HistoryManager;
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
import com.cylan.jiafeigou.widget.wheel.HistoryWheelView;
import com.cylan.jiafeigou.widget.wheel.ex.DataExt;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_DRAGGING;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_FINISH;

/**
 * Created by hds on 17-4-26.
 */

public class HistoryWheelHandler implements HistoryWheelView.HistoryListener {
    private HistoryWheelView superWheelExt;
    private CamLiveContract.Presenter presenter;
    private Context context;
    private String uuid;

    public HistoryWheelHandler(HistoryWheelView superWheel, CamLiveContract.Presenter presenter) {
        this.superWheelExt = superWheel;
        superWheelExt.setHistoryListener(this);
        this.presenter = presenter;
        context = superWheel.getContext();
        uuid = presenter.getUuid();
    }

    public void dateUpdate() {
        TreeSet<JFGVideo> history = HistoryManager.getInstance().getHistory(uuid);
        if (history == null) {
            AppLogger.d("历史录像没准备好");
            return;
        }
        superWheelExt.setHistoryFiles(HistoryManager.getInstance().getHistory(uuid));
    }

    public void showDatePicker(boolean isLand) {
        showPortDatePicker();
    }


    private void showPortDatePicker() {
        TreeSet<JFGVideo> historyFiles = HistoryManager.getInstance().getHistory(uuid);
        if (historyFiles.size() > 0) {
            long start = historyFiles.first().beginTime * 1000L;
            long end = historyFiles.last().beginTime * 1000L;
            long select = superWheelExt.getCurrentTime();
            int timezoneOffset = JFGRules.getDeviceTimezone(DataSourceManager.getInstance().getDevice(uuid)).getRawOffset();
            String title = context.getString(R.string.TIME);
            DatePickerDialogFragment fragment = DatePickerDialogFragment.newInstance(start, end, select, timezoneOffset, title, null, null);
            fragment.setAction(new BaseDialog.BaseDialogAction() {
                @Override
                public void onDialogAction(int id, Object value) {
                    if (value != null && value instanceof Long) {
                        JFGVideo historyFile = new JFGVideo("", (Long) value / 1000L, 0);
                        JFGVideo floor = historyFiles.floor(historyFile);
                        JFGVideo ceiling = historyFiles.ceiling(historyFile);
                        boolean inFloor = floor != null && floor.beginTime + floor.duration >= historyFile.beginTime;
                        boolean inCeiling = ceiling != null && ceiling.beginTime == historyFile.beginTime;
                        if (!inCeiling && !inFloor) {
                            //没有这段视频
                            ToastUtil.showToast(ContextUtils.getContext().getString(R.string.Historical_No));
                        } else {
                            if (datePickerListener != null) {
                                datePickerListener.onPickDate((Long) value, STATE_FINISH);
                            }
                            playPreciseByTime((Long) value);
                        }
                    }
                }
            });
            fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "DatePickerDialogFragment");
        }
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
            presenter.fetchHistoryDataListV2(uuid, (int) timeStart, 0, 1);
            return;
        }
        playByTime(timeStart);
    }

    private void playByTime(long timeStart) {
        presenter.startPlayHistory(TimeUtils.wrapToLong(timeStart));
        setNav2Time(TimeUtils.wrapToLong(timeStart), true);
        setupHistoryData();
    }

    public boolean isHistoryLocked() {
        return superWheelExt.isLocked();
    }

    public void setNav2Time(long time, boolean lock) {
        superWheelExt.scrollToPosition(TimeUtils.wrapToLong(time), lock);
    }

    public void setupHistoryData() {
        final long time = System.currentTimeMillis();
        superWheelExt.setHistoryFiles(HistoryManager.getInstance().getHistory(uuid));
        superWheelExt.setHistoryListener(this);
        Log.d("performance", "CamLivePortWheel performance: " + (System.currentTimeMillis() - time));
    }

    public void setDatePickerListener(DatePickerListener datePickerListener) {
        this.datePickerListener = datePickerListener;
    }

    private DatePickerListener datePickerListener;

    @Override
    public void onHistoryTimeChanged(long time) {
        presenter.startPlayHistory(time);
        if (datePickerListener != null) {
            datePickerListener.onPickDate(time / 1000, STATE_FINISH);
        }
        AppLogger.d("拖动停止了:" + time + "," + History.date2String(time));
    }

    @Override
    public void onScrolling(long time) {
        if (datePickerListener != null) {
            datePickerListener.onPickDate(time / 1000, STATE_DRAGGING);
        }
    }

    /**
     * 选择日期,滚动条变化.都要通知.
     */
    public interface DatePickerListener {
        void onPickDate(long time, int state);
    }

}
