package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.Converter;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMessageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-7-13.
 */
public class CamMessageListPresenterImpl extends AbstractPresenter<CamMessageListContract.View>
        implements CamMessageListContract.Presenter {


    private BeanCamInfo info;
    private CompositeSubscription compositeSubscription;

    public CamMessageListPresenterImpl(CamMessageListContract.View view, BeanCamInfo info) {
        super(view);
        view.setPresenter(this);
        this.info = info;
    }

    @Override
    public void start() {
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(messageListSub());
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }

    private Subscription messageListSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.JfgAlarmMsg.class)
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<RxEvent.JfgAlarmMsg, Observable<ArrayList<CamMessageBean>>>() {
                    @Override
                    public Observable<ArrayList<CamMessageBean>> call(RxEvent.JfgAlarmMsg jfgAlarmMsg) {
                        ArrayList<CamMessageBean> beanList = Converter.convert(jfgAlarmMsg.uuid, jfgAlarmMsg.jfgdpMsgs);
                        return Observable.just(beanList);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new RxHelper.Filter<>("messageListSub()=null?", getView() != null))
                .map((ArrayList<CamMessageBean> jfgdpMsgs) -> {
                    getView().onMessageListRsp(jfgdpMsgs);
                    return null;
                })
                .retry(new RxHelper.RxException<>("messageListSub"))
                .subscribe();
    }

    @Override
    public void fetchMessageList() {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<Object, ArrayList<CamMessageBean>>() {
                    @Override
                    public ArrayList<CamMessageBean> call(Object o) {
                        ArrayList<JFGDPMsg> dps = new ArrayList<>();
                        dps.add(new JFGDPMsg(DpMsgMap.ID_505_CAMERA_ALARM_MSG, 0));
                        dps.add(new JFGDPMsg(DpMsgMap.ID_204_SDCARD_STORAGE, 0));
                        try {
                            JfgCmdInsurance.getCmd().robotGetData(info.deviceBase.uuid,
                                    dps, 20, false, 0);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .subscribe();
    }
}
