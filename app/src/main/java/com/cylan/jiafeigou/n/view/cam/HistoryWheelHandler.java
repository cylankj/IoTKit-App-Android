package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.misc.JFGRules;
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
import com.cylan.jiafeigou.widget.wheel.ex.IData;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
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
        if (presenter.getHistoryDataProvider() == null) {
            AppLogger.d("历史录像没准备好");
            return;
        }
        superWheelExt.setHistoryFiles(presenter.getHistoryDataProvider().getRawHistoryFiles());
    }

    public void showDatePicker(boolean isLand) {
        showPortDatePicker();
    }


    private void showPortDatePicker() {
        TreeSet<HistoryFile> historyFiles = new TreeSet<>(presenter.getHistoryDataProvider().getRawHistoryFiles());
        if (historyFiles.size() > 0) {
            long start = historyFiles.first().time * 1000L;
            long end = historyFiles.last().time * 1000L;
            long select = superWheelExt.getCurrentTime();
            int timezoneOffset = JFGRules.getDeviceTimezone(DataSourceManager.getInstance().getDevice(uuid)).getRawOffset();
            String title = context.getString(R.string.TIME);
            DatePickerDialogFragment fragment = DatePickerDialogFragment.newInstance(start, end, select, timezoneOffset, title, null, null);
            fragment.setAction(new BaseDialog.BaseDialogAction() {
                @Override
                public void onDialogAction(int id, Object value) {
                    if (value != null && value instanceof Long) {
                        HistoryFile historyFile = new HistoryFile();
                        historyFile.time = (Long) value / 1000L;
                        HistoryFile floor = historyFiles.floor(historyFile);
                        HistoryFile ceiling = historyFiles.ceiling(historyFile);
                        boolean inFloor = floor != null && floor.time + floor.duration >= historyFile.time;
                        boolean inCeiling = ceiling != null && ceiling.time == historyFile.time;
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
                        presenter.startPlayHistory(TimeUtils.wrapToLong(timeStart));
                        setNav2Time(TimeUtils.wrapToLong(timeStart), true);
                        AppLogger.d("找到历史录像?" + historyFile);
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    public boolean isHistoryLocked() {
        return superWheelExt.isLocked();
    }

    public void setNav2Time(long time, boolean lock) {
        superWheelExt.scrollToPosition(TimeUtils.wrapToLong(time), lock);
    }

    public void setupHistoryData(IData dataProvider) {
        final long time = System.currentTimeMillis();
        superWheelExt.setHistoryFiles(dataProvider.getRawHistoryFiles());
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
