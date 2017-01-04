package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.efamily.MsgBindCidReq;
import com.cylan.jiafeigou.misc.efamily.RspMsgHeader;
import com.cylan.jiafeigou.n.mvp.contract.bind.ScanContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.TimeZone;

import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-7.
 */
public class ScanPresenterImpl extends AbstractPresenter<ScanContract.View> implements ScanContract.Presenter {


    public ScanPresenterImpl(ScanContract.View v) {
        super(v);
        v.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{scanResult()};
    }

    private Subscription scanResult() {
        return RxBus.getCacheInstance().toObservable(RxEvent.EFamilyMsgpack.class)
                .subscribeOn(Schedulers.newThread())
                .map((RxEvent.EFamilyMsgpack eFamilyMsgpack) -> {
                    try {
                        switch (eFamilyMsgpack.msgId) {
                            case 16219:
                                RspMsgHeader rspHeader = DpUtils.unpackData(eFamilyMsgpack.data, RspMsgHeader.class);
                                if (rspHeader != null && TextUtils.equals(getView().getUuid(), rspHeader.caller))
                                    getView().onScanRsp(rspHeader.ret);
                                Log.d(TAG, "rspHeader: " + rspHeader);
                                break;
                        }
                    } catch (Exception e) {
                        AppLogger.e("" + e.getLocalizedMessage());
                    }
                    return null;
                })
                .retry(new RxHelper.RxException<>("scanResult"))
                .subscribe();
    }

    @Override
    public void submit(Bundle bundle) {
        String cid = bundle.getString("cid");
        String mac = bundle.getString("mac");
        String alias = bundle.getString("alias");
        int way = bundle.getInt("bindWay");
        MsgBindCidReq mMsgBindCidReq = new MsgBindCidReq(cid);
        mMsgBindCidReq.cid = cid;
        mMsgBindCidReq.is_rebind = way;
        mMsgBindCidReq.timezone = TimeZone.getDefault().getID();
        mMsgBindCidReq.alias = alias;
        mMsgBindCidReq.mac = mac;
        mMsgBindCidReq.seq = 20011L;
        byte[] data = DpUtils.pack(mMsgBindCidReq);
        JfgCmdInsurance.getCmd().sendEfamilyMsg(data);
        Log.d(TAG, String.format("unpack: %s", MiscUtils.bytesToHex(data)));
    }
}
