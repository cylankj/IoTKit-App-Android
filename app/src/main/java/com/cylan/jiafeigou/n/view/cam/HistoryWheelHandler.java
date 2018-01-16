package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.HistoryManager;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.DatePickerDialogFragment;
import com.cylan.jiafeigou.widget.wheel.HistoryWheelView;

import java.util.TreeSet;

import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_DRAGGING;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_FINISH;

/**
 * Created by hds on 17-4-26.
 */

public class HistoryWheelHandler implements HistoryWheelView.HistoryListener {
    private HistoryWheelView superWheelExt;
    private Context context;
    private String uuid;

    public HistoryWheelHandler(HistoryWheelView superWheel, String uuid) {
        this.superWheelExt = superWheel;
        superWheelExt.setHistoryListener(this);
        context = superWheel.getContext();
        this.uuid = uuid;
    }

    public void showDatePicker(boolean isLand) {
        showPortDatePicker();
    }

    private int getMinute(long time) {
        return (int) Math.ceil((((double) time) / 60));
    }

    private boolean isSameMinute(long time1, long time2) {
        return getMinute(time1) == getMinute(time2);
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
                        TreeSet<JFGVideo> historyFiles = HistoryManager.getInstance().getHistory(uuid);
                        JFGVideo historyFile = new JFGVideo("", (Long) value / 1000L, 0);
                        JFGVideo floor = historyFiles.floor(historyFile);
                        JFGVideo ceiling = historyFiles.ceiling(historyFile);

                        /*如果在一分钟内有视频,也需要认为有视频,举个例子,当前选择的时间是8:00:00 这个时间点没有视频,
                        * 但是8:00:05这个时间有视频,也认为是有效选择了自动播放8:00:05这里的视频
                        * */
                        boolean inFloor = floor != null && (floor.beginTime + floor.duration >= historyFile.beginTime ||
                                isSameMinute(floor.beginTime + floor.duration, historyFile.beginTime));
                        boolean inCeiling = ceiling != null && isSameMinute(ceiling.beginTime, historyFile.beginTime);
                        if (BuildConfig.DEBUG) {
                            Log.d("HistoryWheelHandler", "select time is:" + historyFile.beginTime +
                                    ",floor is:" + floor +
                                    ",ceiling is" + ceiling +
                                    ",in floor:" + inFloor +
                                    ",in ceiling:" + inCeiling
                            );
                        }
                        if (!inCeiling && !inFloor) {
                            //没有这段视频
                            ToastUtil.showToast(ContextUtils.getContext().getString(R.string.Historical_No));
                        } else {
                            //如果是同一分钟则不更新
                            if (datePickerListener != null && !isSameMinute(select / 1000, historyFile.beginTime)) {
                                datePickerListener.onPickDate(historyFile.beginTime, STATE_FINISH);
                            }
                        }
                    }
                }
            });
            fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "DatePickerDialogFragment");
        }
    }

    public void setDatePickerListener(DatePickerListener datePickerListener) {
        this.datePickerListener = datePickerListener;
    }

    private DatePickerListener datePickerListener;

    @Override
    public void onHistoryTimeChanged(long time) {
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
