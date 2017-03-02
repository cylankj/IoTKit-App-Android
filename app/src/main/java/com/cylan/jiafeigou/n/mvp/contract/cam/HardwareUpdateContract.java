package com.cylan.jiafeigou.n.mvp.contract.cam;

import android.os.Parcelable;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.UpdateFileBean;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public interface HardwareUpdateContract {

    interface View extends BaseView<Presenter> {

        void handlerResult(int code);

        void onDownloadStart();

        void onDownloadFinish();

        void onDownloading(double percent, long downloadedLength);

        void onDownloadErr(int reason);

        void startUpdate();

        void onUpdateing(int percent);

    }

    interface Presenter extends BasePresenter {

        UpdateFileBean creatDownLoadBean();

        void startDownload(Parcelable parcelable);

        void stopDownload();

        String getFileSize();

        void startUpdate();

        Subscription updateBack();

        /**
         * 开始模拟动画
         */
        void startCounting();

        /**
         * 动画结束:
         */
        void endCounting();

        String FormetSDcardSize(long length);
    }
}
