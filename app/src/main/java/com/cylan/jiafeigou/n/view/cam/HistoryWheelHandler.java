package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.DatePickerDialogFragment;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;

import java.lang.ref.WeakReference;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_ADSORB;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_DRAGGING;
import static com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt.STATE_FINISH;


/**
 * Created by hds on 17-4-26.
 */

public class HistoryWheelHandler implements SuperWheelExt.WheelRollListener {
    private SuperWheelExt superWheelExt;
    private CamLiveContract.Presenter presenter;
    private Context context;
    private ViewGroup landDateListContainer;
    private WeakReference<DatePickerDialogFragment> datePickerRef;
    private static final String TAG = "HistoryWheelHandler";
    private String uuid;

    public HistoryWheelHandler(ViewGroup landDateListContainer, SuperWheelExt superWheel, CamLiveContract.Presenter presenter) {
        this.landDateListContainer = landDateListContainer;
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
//        if (isLand) {
//            if (landDateListContainer.getVisibility() == View.GONE
//                    || landDateListContainer.getTranslationX() == landDateListContainer.getWidth()) {
//                AnimatorUtils.slideInRight(landDateListContainer);
//                landDateListContainer.removeCallbacks(containerHide);
//                landDateListContainer.postDelayed(containerHide, 3000);//自动隐藏
//            } else if (AnimatorUtils.getSlideOutXDistance(landDateListContainer) == 0)
//                AnimatorUtils.slideOutRight(landDateListContainer);
//            showLandDatePicker();
//        } else {
        showPortDatePicker();
//        }
    }

    public void onBackPress() {
        if (landDateListContainer.getTranslationX() == 0)
            AnimatorUtils.slideOutRight(landDateListContainer);
    }

//    private void showLandDatePicker() {
//        if (recyclerView.getAdapter() == null || recyclerView.getAdapter().getItemCount() == 0) {
//            final ArrayList<Long> dateStartList = presenter.getFlattenDateList();
//            Collections.sort(dateStartList, Collections.reverseOrder());//来一个降序
//            AppLogger.d("sort: " + dateStartList);
//            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false));
//            recyclerView.setAdapter(new CamLandHistoryDateAdapter(context, null, R.layout.layout_cam_history_land_list));
//            ((CamLandHistoryDateAdapter) recyclerView.getAdapter()).addAll(dateStartList);
//            ((CamLandHistoryDateAdapter) recyclerView.getAdapter()).setOnItemClickListener((View itemView, int viewType, int position) -> {
//                long time = dateStartList.get(position);
//                AppLogger.d("msgTime pick: " + TimeUtils.getSpecifiedDate(time));
//                loadSelectedDay(TimeUtils.getSpecificDayStartTime(time));
//                landDateListContainer.removeCallbacks(containerHide);
//                landDateListContainer.post(containerHide);//选中立马隐藏
//                ((CamLandHistoryDateAdapter) recyclerView.getAdapter()).setCurrentFocusPos(position);
//            });
//            ((CamLandHistoryDateAdapter) recyclerView.getAdapter()).setCurrentFocusTime(getWheelCurrentFocusTime());
//            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                    landDateListContainer.removeCallbacks(containerHide);
//                    landDateListContainer.postDelayed(containerHide, 3000);//自动隐藏
//                }
//            });
//        }
//    }

    private void showPortDatePicker() {
        if (datePickerRef == null || datePickerRef.get() == null) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, context.getString(R.string.TIME));
            DatePickerDialogFragment.newInstance(bundle);
            datePickerRef = new WeakReference<>(DatePickerDialogFragment.newInstance(bundle));
            datePickerRef.get().setAction((int id, Object value) -> {
                if (value != null && value instanceof Long) {
                    IData data = presenter.getHistoryDataProvider();
                    HistoryFile historyFile = data == null ? null : data.getMaxHistoryFile();
                    if (historyFile == null || historyFile.getTime() + historyFile.getDuration() < (long) value / 1000) {
                        AppLogger.d("没有这段视频: " + historyFile + "," + value);
                        ToastUtil.showToast(ContextUtils.getContext().getString(R.string.Historical_No));
                        return;
                    }
                    AppLogger.d("msgTime pick: " + TimeUtils.getTimeSpecial((Long) value) + "," + value);
                    if (datePickerListener != null)
                        datePickerListener.onPickDate((Long) value, STATE_FINISH);
                    loadSelectedDay((Long) value);
                }
            });
        }
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        datePickerRef.get().setTimeZone(JFGRules.getDeviceTimezone(device));
        datePickerRef.get().setTimeFocus(getWheelCurrentFocusTime());
        datePickerRef.get().setDateList(presenter.getFlattenDateList());
        datePickerRef.get().show(((FragmentActivity) context).getSupportFragmentManager(),
                "DatePickerDialogFragment");
    }

    private long getWheelCurrentFocusTime() {
        return superWheelExt.getCurrentFocusTime();
    }

    /**
     * 选择一天,load所有的数据,但是需要移动的这一天的开始位置.
     */
    private void loadSelectedDay(long timeStart) {
//        final long start = TimeUtils.getSpecificDayStartTime(timeStart);
        presenter.assembleTheDay()
                .subscribeOn(Schedulers.io())
                .filter(iData -> iData != null)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> AppLogger.d("reLoad hisData: good"))
                .subscribe(iData -> {
                    setupHistoryData(iData);
                    HistoryFile historyFile = iData.getMinHistoryFileByStartTime(timeStart);//最小时间.
                    if (historyFile != null) {
                        setNav2Time(historyFile.time * 1000L);
                        presenter.startPlayHistory(historyFile.time * 1000L);
                        AppLogger.d("找到历史录像?" + historyFile);
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    public void setNav2Time(long time) {
        superWheelExt.post(() -> superWheelExt.setPositionByTime(TimeUtils.wrapToLong(time)));
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
        tmpTime = time;
        switch (state) {
            case STATE_DRAGGING:
                Log.d("onTimeUpdate", "STATE_DRAGGING :" + TimeUtils.getTestTime(time));
                if (datePickerListener != null)
                    datePickerListener.onPickDate(time / 1000, STATE_DRAGGING);
                superWheelExt.removeCallbacks(dragRunnable);
//                superWheelExt.postDelayed(dragRunnable, 400);
                break;
            case STATE_ADSORB:
                Log.d("onTimeUpdate", "STATE_ADSORB :" + TimeUtils.getTestTime(time));
                break;
            case STATE_FINISH:
                Log.d("onTimeUpdate", "STATE_FINISH :" + TimeUtils.getTestTime(time));
                if (datePickerListener != null)
                    datePickerListener.onPickDate(time / 1000, STATE_FINISH);
                superWheelExt.removeCallbacks(dragRunnable);
                superWheelExt.postDelayed(dragRunnable, 400);
                break;
        }
    }

    private long tmpTime;
    private Runnable dragRunnable = new Runnable() {
        @Override
        public void run() {
            presenter.startPlayHistory(tmpTime);
            AppLogger.d("拖动停止了:" + tmpTime);
        }
    };

    public void setDatePickerListener(DatePickerListener datePickerListener) {
        this.datePickerListener = datePickerListener;
    }

    private Runnable containerHide = new Runnable() {
        @Override
        public void run() {
            AnimatorUtils.slideOutRight(landDateListContainer);
        }
    };

    private DatePickerListener datePickerListener;

    public boolean isDragging() {
        return false;
    }

    /**
     * 选择日期,滚动条变化.都要通知.
     */
    public interface DatePickerListener {
        void onPickDate(long time, int state);
    }

}
