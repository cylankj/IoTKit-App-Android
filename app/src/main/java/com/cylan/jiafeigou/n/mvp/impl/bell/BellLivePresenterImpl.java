package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.text.TextUtils;

import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.base.wrapper.BaseCallablePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.utils.JfgUtils;

import java.io.File;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-8-10.
 */

public class BellLivePresenterImpl extends BaseCallablePresenter<BellLiveContract.View> implements
        BellLiveContract.Presenter {
    @Inject
    JFGSourceManager sourceManager;

    @Inject
    public BellLivePresenterImpl(BellLiveContract.View view) {
        super(view);
    }

    @Override
    public void capture() {

        Observable.just("capture")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    byte[] screenshot = appCmd.screenshot(false);
                    if (screenshot != null) {
                        int w = ((JfgAppCmd) BaseApplication.getAppComponent().getCmd()).videoWidth;
                        int h = ((JfgAppCmd) BaseApplication.getAppComponent().getCmd()).videoHeight;
                        Bitmap bitmap = JfgUtils.byte2bitmap(w, h, screenshot);
                        AndroidSchedulers.mainThread().createWorker().schedule(() -> mView.onTakeSnapShotSuccess(bitmap));
                        String filePath = JConstant.MEDIA_PATH + File.separator + "." + uuid + System.currentTimeMillis();
                        String fileName = System.currentTimeMillis() + ".png";
                        MiscUtils.insertImage(JConstant.MEDIA_PATH, fileName);
                        BitmapUtils.saveBitmap2file(bitmap, filePath);
                        MediaScannerConnection.scanFile(mView.activity(), new String[]{filePath}, null, null);
                        AppLogger.e("截图文件地址:" + filePath);
                        DpMsgDefine.DPWonderItem item = new DpMsgDefine.DPWonderItem();
                        item.msgType = DpMsgDefine.DPWonderItem.TYPE_PIC;
                        item.cid = uuid;
                        Device device = sourceManager.getDevice(uuid);
                        item.place = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
                        long time = System.currentTimeMillis();
                        item.fileName = time / 1000 + ".jpg";
                        item.time = (int) (time / 1000);
                        IDPEntity entity = new DPEntity()
                                .setUuid(uuid)
                                .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG)
                                .setVersion(System.currentTimeMillis())
                                .setAccount(sourceManager.getAccount().getAccount())
                                .setAction(DBAction.SHARED)
                                .setOption(new DBOption.SingleSharedOption(1, 1, filePath))
                                .setBytes(item.toBytes());
                        return entity;
                    } else {
                        AndroidSchedulers.mainThread().createWorker().schedule(() -> mView.onTakeSnapShotFailed());
                        return null;
                    }

                })
                .filter(ret -> ret != null)
                .flatMap(entity -> mTaskDispatcher.perform(entity))
                .subscribe(result -> {
                }, e -> AppLogger.d(e.getMessage()));
    }
}
