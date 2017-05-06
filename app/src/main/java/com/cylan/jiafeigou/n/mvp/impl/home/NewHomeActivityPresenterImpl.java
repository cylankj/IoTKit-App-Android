package com.cylan.jiafeigou.n.mvp.impl.home;

import android.text.TextUtils;

import com.cylan.jiafeigou.misc.ClientUpdateManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.home.NewHomeActivityContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import org.json.JSONObject;

import java.io.File;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-23.
 */
public class NewHomeActivityPresenterImpl extends AbstractPresenter<NewHomeActivityContract.View>
        implements NewHomeActivityContract.Presenter,
        ClientUpdateManager.DownloadListener {

    public NewHomeActivityPresenterImpl(NewHomeActivityContract.View view) {
        super(view);
        view.setPresenter(this);
        view.initView();
    }

    private Subscription updateRsp() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.ClientUpdateEvent.class)
                .subscribeOn(Schedulers.newThread())
                .subscribe(ret -> {
                    RxBus.getCacheInstance().removeStickyEvent(RxEvent.ClientUpdateEvent.class);
                    startUpdate(ret.forceUpdate);
                }, throwable -> {
                    AppLogger.e("err:" + MiscUtils.getErr(throwable));
                    addSubscription(updateRsp());
                });
    }

    @Override
    public void start() {
        super.start();
        addSubscription(updateRsp());
    }

    public void startUpdate(int force) {
        try {
            AppLogger.d("开始升级");
            String result = PreferencesUtils.getString(JConstant.KEY_CLIENT_UPDATE_DESC);
            if (TextUtils.isEmpty(result)) return;
            JSONObject jsonObject = new JSONObject(result);
            final String url = jsonObject.getString("url");
            final String versionName = jsonObject.getString("version");
            final String shortVersion = jsonObject.getString("shortversion");
            final String desc = jsonObject.getString("desc");
            ClientUpdateManager.getInstance().enqueue(url, versionName, shortVersion, new ClientUpdateManager.DownloadListener() {
                @Override
                public void start() {
                    AppLogger.d("开始下载");
                }

                @Override
                public void failed(Throwable throwable) {
                    AppLogger.d("下载失败: " + MiscUtils.getErr(throwable));
                    PreferencesUtils.remove(JConstant.KEY_LAST_TIME_CHECK_VERSION);
                    PreferencesUtils.remove(JConstant.KEY_CLIENT_UPDATE_DESC);
                }

                @Override
                public void finished(File file) {
                    AppLogger.d("下载完成");
                    Observable.just(file)
                            .observeOn(AndroidSchedulers.mainThread())
                            .filter(ret -> mView != null)
                            .subscribe(f -> {
                                        long time = System.currentTimeMillis() - PreferencesUtils.getLong(JConstant.KEY_LAST_TIME_CHECK_VERSION, 0);
                                        if (force == 1 || (time == -1 || System.currentTimeMillis() - time > 24 * 1000 * 3600))
                                            mView.needUpdate(desc, f.getAbsolutePath());
                                    },
                                    AppLogger::e);
                }

                @Override
                public void process(long currentByte, long totalByte) {
                }
            });
        } catch (Exception e) {
            AppLogger.e(MiscUtils.getErr(e));
        }
    }

    @Override
    public void failed(Throwable throwable) {

    }

    @Override
    public void finished(File file) {

    }

    @Override
    public void process(long currentByte, long totalByte) {

    }
}
