package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.n.mvp.contract.cam.FirmwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;

import java.io.File;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class FirmwareUpdatePresenterImpl extends AbstractPresenter<FirmwareUpdateContract.View> implements FirmwareUpdateContract.Presenter,
        SimulatePercent.OnAction {


    public FirmwareUpdatePresenterImpl(FirmwareUpdateContract.View view) {
        super(view);
    }

    @Override
    public void actionDone() {

    }

    @Override
    public void actionPercent(int percent) {

    }

    @Override
    public void cleanFile() {
        Observable.just("cleanFile")
                .subscribeOn(Schedulers.io())
                .subscribe(ret -> FileUtils.deleteFile(ContextUtils.getContext().getFilesDir().getAbsolutePath()
                        + File.separator + "." + uuid), AppLogger::e);
    }
}
