package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.JFGDevBaseValue;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.SdCardInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class SdCardInfoPresenterImpl extends AbstractPresenter<SdCardInfoContract.View> implements SdCardInfoContract.Presenter {

    private boolean isClearSucc;
    private String uuid;

    public SdCardInfoPresenterImpl(SdCardInfoContract.View view,String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                onClearSdBack()
        };
    }

    /**
     * 是否有SD卡
     * @return
     */
    @Override
    public boolean getSdcardState() {
        DpMsgDefine.DPSdStatus sdStatus = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE, null);
        //sd卡状态
        if (sdStatus != null) {
            if (!sdStatus.hasSdcard && sdStatus.err != 0) {
                //sd初始化失败
                return false;
            }
        }
        if (sdStatus != null && !sdStatus.hasSdcard) {
            return false;
        }
        return true;
    }

    @Override
    public void clearSDcard(int id) {
        // TODO 格式化Sd卡
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
//                    BaseValue baseValue = new BaseValue();
//                    baseValue.setId(id);
//                    baseValue.setValue(o);
//                    boolean update = GlobalDataProxy.getInstance().update(uuid, baseValue, false);
//                    AppLogger.d("clearSDcard_req:"+update);

                    ArrayList<JFGDPMsg> ipList = new ArrayList<JFGDPMsg>();
                    JFGDPMsg mesg = new JFGDPMsg(DpMsgMap.ID_218_DEVICE_FORMAT_SDCARD,0);
                    ipList.add(mesg);
                    try {
                        long req = JfgCmdInsurance.getCmd().robotSetData(uuid, ipList);
                        AppLogger.d("clearSDcard_req:"+req);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e("clearSDcard"+throwable.getLocalizedMessage());
                });
    }

    @Override
    public void clearCountTime() {
        Observable.just(null)
                .delay(2, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Object o)->{
                    if (getView() != null && !isClearSucc)getView().clearSdResult(2);
                });
    }

    @Override
    public Subscription onClearSdBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SdcardClearRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.SdcardClearRsp sdcardClearRsp)->{
                    if (sdcardClearRsp != null){
                        isClearSucc = true;
                        JFGDPMsgRet jfgdpMsgRet = sdcardClearRsp.arrayList.get(0);
                        if (jfgdpMsgRet.id == 218 && jfgdpMsgRet.ret == 0){
                            getView().clearSdResult(0);
                        }else {
                            getView().clearSdResult(1);
                        }
                    }
                } );
    }

}
