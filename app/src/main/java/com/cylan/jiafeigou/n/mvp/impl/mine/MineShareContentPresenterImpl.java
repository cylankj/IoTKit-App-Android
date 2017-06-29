package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareContentContract;
import com.cylan.jiafeigou.n.view.adapter.item.ShareContentItem;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/5/26.
 */

public class MineShareContentPresenterImpl extends BasePresenter<MineShareContentContract.View> implements MineShareContentContract.Presenter {


    @Override
    public void unShareContent(Iterable<DpMsgDefine.DPShareItem> item, Iterable<Integer> selection) {
        Subscription subscribe = Observable.just(item)
                .map(items -> {
                    List<DPEntity> result = new ArrayList<>();
                    DPEntity entity;
                    for (DpMsgDefine.DPShareItem contentItem : items) {
                        entity = new DPEntity(contentItem.cid, 606, contentItem.version, DBAction.DELETED, null);
                        result.add(entity);
                    }
                    return result;
                })
                .observeOn(Schedulers.io())
                .flatMap(this::perform)
                .map(ret -> {
                    List<DPEntity> result = new ArrayList<>();
                    DPEntity entity;
                    for (DpMsgDefine.DPShareItem shareItem : item) {
                        entity = new DPEntity(shareItem.cid, 511, shareItem.time, DBAction.DELETED, null);
                        result.add(entity);
                    }
                    return result;
                })
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    AppLogger.d("取消分享返回结果为:" + new Gson().toJson(result));
                    mView.onUnShareContentResponse(result.getResultCode(), selection);
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        registerSubscription(subscribe);
    }

    @Override
    public void loadFromServer(long version, boolean refresh) {
        Subscription subscribe = Observable.just(new DPEntity(null, 606, 0, DBAction.QUERY, DBOption.SingleQueryOption.DESC_20_LIMIT))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(this::perform)
                .map(result -> {
                    List<ShareContentItem> contentItems = new ArrayList<>();
                    if (result != null && result.getResultCode() == 0) {
                        List<DpMsgDefine.DPShareItem> shareItems = result.getResultResponse();
                        if (shareItems != null) {
                            ShareContentItem shareContentItem;
                            for (DpMsgDefine.DPShareItem item : shareItems) {
                                shareContentItem = new ShareContentItem(item);
                                contentItems.add(shareContentItem);
                            }
                        }
                    }
                    return contentItems;
                })
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> mView.showLoading(R.string.LOADING))
                .doOnTerminate(() -> mView.hideLoading())
                .subscribe(result -> {
                    mView.onShareContentResponse(result, refresh);
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        registerSubscription(subscribe);
    }
}
