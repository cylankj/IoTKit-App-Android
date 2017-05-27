package com.cylan.jiafeigou.n.mvp.impl.mine;

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

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/5/26.
 */

public class MineShareContentPresenterImpl extends BasePresenter<MineShareContentContract.View> implements MineShareContentContract.Presenter {


    @Override
    public void unShareContent(ShareContentItem item, int position) {
        Subscription subscribe = Observable.just(new DPEntity(null, 606, item.shareItem.version, DBAction.DELETED, null))
                .observeOn(Schedulers.io())
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    AppLogger.d("取消分享返回结果为:" + new Gson().toJson(result));
                    mView.onUnShareContentResponse(result.getResultCode(), position);
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        registerSubscription(subscribe);
    }

    @Override
    public void loadFromServer(long version, boolean refresh) {
        Subscription subscribe = Observable.just(new DPEntity(null, 606, 0, DBAction.QUERY, DBOption.SingleQueryOption.DESC_20_LIMIT))
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    mView.onShareContentResponse(result, refresh);
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        registerSubscription(subscribe);
    }
}
