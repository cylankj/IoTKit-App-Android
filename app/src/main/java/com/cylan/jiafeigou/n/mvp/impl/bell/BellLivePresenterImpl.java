package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.cylan.jfgapp.interfases.CallBack;
import com.cylan.jiafeigou.base.wrapper.BaseCallablePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;

import java.io.File;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-8-10.
 */

public class BellLivePresenterImpl extends BaseCallablePresenter<BellLiveContract.View> implements
        BellLiveContract.Presenter {
    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
    }

    @Override
    public void capture() {
        JfgCmdInsurance.getCmd().screenshot(false, new CallBack<Bitmap>() {
            @Override
            public void onSucceed(Bitmap bitmap) {
                String filePath = JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + ".png";
                mView.onTakeSnapShotSuccess(bitmap);
                Observable.create((Observable.OnSubscribe<IDPEntity>) subscriber -> {
                    BitmapUtils.saveBitmap2file(bitmap, filePath);
                    DpMsgDefine.DPWonderItem item = new DpMsgDefine.DPWonderItem();
                    item.msgType = DpMsgDefine.DPWonderItem.TYPE_PIC;
                    item.cid = mUUID;
                    Device device = sourceManager.getJFGDevice(mUUID);
                    item.place = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
                    long time = System.currentTimeMillis();
                    item.fileName = time / 1000 + ".jpg";
                    item.time = (int) (time / 1000);
                    IDPEntity entity = new DPEntity()
                            .setUuid(mUUID)
                            .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG)
                            .setVersion(System.currentTimeMillis())
                            .setAccount(sourceManager.getAJFGAccount().getAccount())
                            .setAction(DBAction.SHARED)
                            .setOption(new DBOption.SingleSharedOption(1, 1, filePath))
                            .setBytes(item.toBytes());
                    subscriber.onNext(entity);
                    subscriber.onCompleted();
                })
                        .subscribeOn(Schedulers.io())
                        .flatMap(entity -> taskDispatcher.perform(entity))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                        }, e -> AppLogger.d(e.getMessage()));
            }

            @Override
            public void onFailure(String s) {
                mView.onTakeSnapShotFailed();
            }
        });
    }
}
