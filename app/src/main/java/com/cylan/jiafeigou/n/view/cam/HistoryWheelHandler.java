package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.view.adapter.CamLandHistoryDateAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.DatePickerDialogFragment;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

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
    private RecyclerView recyclerView;
    private WeakReference<DatePickerDialogFragment> datePickerRef;
    private static final String TAG = "HistoryWheelHandler";

    public HistoryWheelHandler(ViewGroup landDateListContainer, SuperWheelExt superWheel, CamLiveContract.Presenter presenter) {
        this.landDateListContainer = landDateListContainer;
        this.superWheelExt = superWheel;
        superWheelExt.setWheelRollListener(this);
        this.presenter = presenter;
        context = superWheel.getContext();
        landDateListContainer.post(() -> {
            recyclerView = (RecyclerView) landDateListContainer.findViewById(R.id.rv_land_date_list);
        });
    }

    public void dateUpdate() {
        if (presenter.getHistoryDataProvider() == null) {
            AppLogger.d("历史录像没准备好");
            return;
        }
        superWheelExt.setDataProvider(presenter.getHistoryDataProvider());
    }

    public void showDatePicker(boolean isLand) {
        if (isLand) {
            if (AnimatorUtils.getSlideOutXDistance(landDateListContainer) == 0)
                showLandDatePicker();
            else {
                if (landDateListContainer.getTranslationX() == landDateListContainer.getMeasuredWidth()) {
                    //花出去了
                }
            }
        } else {
            showPortDatePicker();
        }
    }

    public void onBackPress() {
        if (landDateListContainer.getTranslationX() == 0)
            AnimatorUtils.slideOutRight(landDateListContainer);
    }

    private void showLandDatePicker() {
        AnimatorUtils.slideInRight(landDateListContainer);
        if (recyclerView.getAdapter() == null || recyclerView.getAdapter().getItemCount() == 0) {
            final ArrayList<Long> dateStartList = presenter.getFlattenDateList();
            Collections.sort(dateStartList, Collections.reverseOrder());//来一个降序
            Log.d(TAG, "sort: " + dateStartList);
            recyclerView.setAdapter(new CamLandHistoryDateAdapter(context, null, R.layout.layout_cam_history_land_list));
            ((CamLandHistoryDateAdapter) recyclerView.getAdapter()).addAll(dateStartList);
            ((CamLandHistoryDateAdapter) recyclerView.getAdapter()).setOnItemClickListener((View itemView, int viewType, int position) -> {
                Log.d(TAG, ".........click:" + position);
                long time = dateStartList.get(position);
                AppLogger.d("date pick: " + TimeUtils.getSpecifiedDate(time));
                loadSelectedDay(TimeUtils.getSpecificDayStartTime(time));
            });
            ((CamLandHistoryDateAdapter) recyclerView.getAdapter()).setCurrentFocusTime(getWheelCurrentFocusTime());
        }
    }

    //


    private void showPortDatePicker() {
        if (datePickerRef == null || datePickerRef.get() == null) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, context.getString(R.string.TIME));
            DatePickerDialogFragment.newInstance(bundle);
            datePickerRef = new WeakReference<>(DatePickerDialogFragment.newInstance(bundle));
            datePickerRef.get().setAction((int id, Object value) -> {
                if (value != null && value instanceof Long) {
                    AppLogger.d("date pick: " + TimeUtils.getSpecifiedDate((Long) value));
                    loadSelectedDay(TimeUtils.getSpecificDayStartTime((Long) value));
                }
            });
        }
        datePickerRef.get().setTimeFocus(getWheelCurrentFocusTime());
        datePickerRef.get().setDateList(presenter.getFlattenDateList());
        datePickerRef.get().show(((FragmentActivity) context).getSupportFragmentManager(),
                "DatePickerDialogFragment");
    }

    private long getWheelCurrentFocusTime() {
        return superWheelExt.getCurrentFocusTime();
    }

    private void loadSelectedDay(long timeStart) {
        presenter.assembleTheDay(timeStart / 1000L)
                .subscribeOn(Schedulers.io())
                .filter(iData -> iData != null)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> {
                    AppLogger.d("reLoad hisData: good");
                })
                .subscribe(iData -> {
                    setupHistoryData(iData);
                    HistoryFile historyFile = iData.getMaxHistoryFile();
                    if (historyFile != null) {
                        setNav2Time(historyFile.time * 1000L);
                        presenter.startPlayHistory(historyFile.time * 1000L);
                        AppLogger.d("找到历史录像?" + historyFile);
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    public void setNav2Time(long time) {
        superWheelExt.post(() -> superWheelExt.setPositionByTime(time));
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
                Log.d("onTimeUpdate", "STATE_DRAGGING :" + TimeUtils.getTestTime(time));
                break;
            case STATE_ADSORB:
                Log.d("onTimeUpdate", "STATE_ADSORB :" + TimeUtils.getTestTime(time));
                break;
            case STATE_FINISH:
                Log.d("onTimeUpdate", "STATE_FINISH :" + TimeUtils.getTestTime(time));
                presenter.startPlayHistory(time);
                break;
        }
    }
}
