package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.util.Log;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.module.VersionCheckHelper;
import com.cylan.jiafeigou.n.mvp.contract.cam.FirmwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.File;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class FirmwareUpdatePresenterImpl extends AbstractPresenter<FirmwareUpdateContract.View> implements FirmwareUpdateContract.Presenter,
        SimulatePercent.OnAction {
    private static final String TAG = FirmwareUpdatePresenterImpl.class.getSimpleName();

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

    @Override
    public void start() {
        super.start();
        checkDeviceVersion();
    }

    private void checkDeviceVersion() {
        Subscription subscribe = VersionCheckHelper.checkNewVersion(uuid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.VersionRsp>() {
                    @Override
                    public void call(RxEvent.VersionRsp versionRsp) {
                        Device device = DataSourceManager.getInstance().getDevice(uuid);
                        final String currentVersion = device.$(207, "");
                        if (BindUtils.versionCompare(versionRsp.getVersion().getTagVersion(), currentVersion) > 0) {
                            PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(versionRsp.getVersion()));
                            mView.onNewVersion(versionRsp);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void performEnvironmentCheck(String uuid) {
        Log.d(TAG, "performEnvironmentCheck with uuid:" + uuid);
//
//        Observable.create(new Observable.OnSubscribe<Integer>() {
//            @Override
//            public void call(Subscriber<? super Integer> subscriber) {
//                Device device = getDevice();
//
//
//
//            }
//        })
//
//        Device device = presenter.getDevice();
//        //相同版本
//        if (TextUtils.equals(tvCurrentVersion.getText(), tvNewVersionName.getText())) {
//            //相同版本
//            ToastUtil.showToast(getString(R.string.NEW_VERSION));
//            return false;
////            return true;//mock¬
//        }
//        String deviceMac = device.$(202, "");
//        String routMac = NetUtils.getRouterMacAddress();
//        //1.直连AP
//        if (TextUtils.equals(deviceMac, routMac)) {
//            return true;
//        }
//
//        DpMsgDefine.DPNet dpNet = device.$(201, new DpMsgDefine.DPNet());
//        String localSSid = NetUtils.getNetName(ContextUtils.getContext());
//        //2.不在线
//        if (!JFGRules.isDeviceOnline(dpNet) || TextUtils.isEmpty(dpNet.ssid)) {
//            ToastUtil.showToast(getString(R.string.NOT_ONLINE));
//            return false;
//        }
//        String remoteSSid = dpNet.ssid;
//        AppLogger.d("check ???" + localSSid + "," + remoteSSid);
//        //4.以上条件都不满足的话,就是在线了
//        if (!TextUtils.equals(localSSid, remoteSSid) || dpNet.net != 1) {
//            AlertDialogManager.getInstance().showDialog(this, getString(R.string.setwifi_check, remoteSSid),
//                    getString(R.string.setwifi_check, remoteSSid), getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
//                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
//                    }, getString(R.string.CANCEL), null);
//            return false;
//        }
//        //简单地认为是同一个局域网
//        return true;
    }
}
