package com.cylan.jiafeigou.n.mvp.contract;

import android.os.Parcelable;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by cylan-hunt on 16-9-29.
 */

public interface DownloadContract {

    interface View extends BaseView<Presenter> {
        /**
         * 闪屏结束
         */
        void onDownloadStart();

        void onDownloadFinish();

        void onDownloading(double percent, long downloadedLength);

        void onDownloadErr(int reason);
    }


    interface Presenter extends BasePresenter {

        void startDownload(Parcelable parcelable);

        void stopDownload();
    }
}
