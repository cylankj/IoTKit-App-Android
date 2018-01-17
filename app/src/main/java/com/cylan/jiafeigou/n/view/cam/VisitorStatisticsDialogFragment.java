package com.cylan.jiafeigou.n.view.cam;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.module.SubscriptionSupervisor;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2018/1/16.
 */

public class VisitorStatisticsDialogFragment extends DialogFragment {
    private String uuid;

    @BindView(R.id.visitor_today_count)
    TextView tvTodayCount;
    @BindView(R.id.visitor_yesterday_count)
    TextView tvYesterdayCount;
    @BindView(R.id.visitor_newly_increased_count)
    TextView tvNewlyIncreasedCount;

    private int todayCount = 0;
    private int allCount = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.l_style_dialog);
        Bundle arguments = getArguments();
        if (arguments != null) {
            uuid = arguments.getString(JConstant.KEY_DEVICE_ITEM_UUID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_visitor_statistics_pop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        performRefreshContent(todayCount, allCount);
        performLoadVisitorStatistics();
    }

    @OnClick(R.id.close)
    public void close() {
        dismissAllowingStateLoss();
    }

    @Override
    public void onStop() {
        super.onStop();
        SubscriptionSupervisor.unsubscribe(this, SubscriptionSupervisor.CATEGORY_DEFAULT, null);
    }

    public static VisitorStatisticsDialogFragment newInstance(String uuid) {
        VisitorStatisticsDialogFragment fragment = new VisitorStatisticsDialogFragment();
        Bundle argument = new Bundle();
        argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(argument);
        return fragment;
    }


    private void performLoadVisitorStatistics() {
        Subscription subscribe1 = Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                DpMsgDefine.VisitorCountStatisticsReq statisticsReq = new DpMsgDefine.VisitorCountStatisticsReq();
                long timeNow = System.currentTimeMillis() / 1000;
                statisticsReq.cid = uuid;
                statisticsReq.type = 0;
                statisticsReq.end = timeNow;
                statisticsReq.begin = TimeUtils.getSpecificDayStartTime(TimeUtils.getTodayStartTime() - 12 * 60 * 60 * 1000) / 1000;
                long dataSeq = Command.getInstance().sendUniservalDataSeq(13, DpUtils.pack(statisticsReq));
                subscriber.onNext(dataSeq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp.class).first(rsp -> rsp.seq == seq))
                .map(universalDataRsp -> {
                    DpMsgDefine.VisitorCountStatisticsRsp statisticsRsp = DpUtils.unpackDataWithoutThrow(universalDataRsp.data, DpMsgDefine.VisitorCountStatisticsRsp.class, null);
                    Log.d("VisitorStatistics", "rsp is:" + statisticsRsp);
                    return statisticsRsp;
                })
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(visitorCountStatisticsRsp -> {
                    if (visitorCountStatisticsRsp != null) {
                        List<DpMsgDefine.VisitorCountSub> countSubs = visitorCountStatisticsRsp.visitorCountSubs;
                        DpMsgDefine.VisitorCountSub countSub = countSubs.get(countSubs.size() - 1);
                        int todayCount = countSub.count;
                        performRefreshContent(todayCount, visitorCountStatisticsRsp.count);
                    }
                }, throwable -> {

                });
        SubscriptionSupervisor.subscribe(this, SubscriptionSupervisor.CATEGORY_DEFAULT, "performLoadVisitorStatistics", subscribe1);
    }

    private void performRefreshContent(int todayCount, int allCount) {
        this.todayCount = todayCount;
        this.allCount = allCount;
        tvTodayCount.setText(String.valueOf(todayCount));
        tvYesterdayCount.setText(String.valueOf(allCount - todayCount));
        int increaseCount = todayCount - (allCount - todayCount);
        tvNewlyIncreasedCount.setText((increaseCount >= 0 ? "+" : "") + increaseCount);
        tvNewlyIncreasedCount.setTextColor(increaseCount >= 0 ? Color.parseColor("#F43531") : Color.parseColor("#459C17"));
    }
}
