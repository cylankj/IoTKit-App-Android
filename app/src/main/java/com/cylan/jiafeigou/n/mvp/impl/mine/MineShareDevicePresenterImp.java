package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.NetUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class MineShareDevicePresenterImp extends AbstractPresenter<MineShareDeviceContract.View> implements MineShareDeviceContract.Presenter {

    private ArrayList<JFGShareListInfo> hasShareFriendList = new ArrayList<>();
    private CompositeSubscription subscription;
    private ArrayList<DeviceBean> allDevice = new ArrayList<>();
    private ArrayList<RelAndFriendBean> hasShareFriendData = new ArrayList<>();
    private ArrayList<RelAndFriendBean> shareSucceedData = new ArrayList<>();

    public MineShareDevicePresenterImp(MineShareDeviceContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        } else {
            subscription = new CompositeSubscription();
            subscription.add(initData());
            subscription.add(getDeviceInfoCallBack());
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @Override
    public Subscription initData() {
        return Observable.just(getShareDeviceList())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<DeviceBean>>() {
                    @Override
                    public void call(ArrayList<DeviceBean> deviceList) {

                        if (getView() != null && deviceList != null && deviceList.size() > 0) {
                            AppLogger.d("share_device:" + deviceList.size());
                            allDevice.clear();
                            ArrayList<String> cidList = new ArrayList<String>();
                            for (DeviceBean bean : deviceList) {
                                if (TextUtils.isEmpty(bean.shareAccount)) {
                                    cidList.add(bean.uuid);
                                    allDevice.add(bean);
                                }
                            }

                            if (NetUtils.getNetType(getView().getContext()) == -1) {
                                ArrayList<JFGShareListInfo> shareList = DataSourceManager.getInstance().getShareList();
                                if (shareList == null || shareList.size() == 0) {
                                    handlerShareDeviceListData(allDevice);
                                    return;
                                }
                                hasShareFriendList.clear();
                                hasShareFriendList.addAll(shareList);
                                for (int i = 0; i < allDevice.size(); i++) {
                                    if (allDevice.get(i).uuid.equals(shareList.get(i).cid)) {
                                        allDevice.get(i).hasShareCount = shareList.get(i).friends.size();
                                    }
                                }
                                handlerShareDeviceListData(allDevice);
                            } else {
                                getDeviceInfo(cidList);
                            }
                        } else {
                            getView().hideLoadingDialog();
                            getView().showNoDeviceView();
                        }
                    }
                });
    }

    @Override
    public ArrayList<RelAndFriendBean> getJFGInfo(int position) {
        hasShareFriendData.clear();
        hasShareFriendData.addAll(shareSucceedData);
        if (this.hasShareFriendList != null && this.hasShareFriendList.size() != 0) {
            for (JFGFriendAccount info : this.hasShareFriendList.get(position).friends) {
                RelAndFriendBean relAndFriendBean = new RelAndFriendBean();
                relAndFriendBean.account = info.account;
                relAndFriendBean.alias = info.alias;
                relAndFriendBean.markName = info.markName;
                try {
                    relAndFriendBean.iconUrl = JfgCmdInsurance.getCmd().getSignedCloudUrl(0, String.format(Locale.getDefault(), "/image/%s.jpg", info.account));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                hasShareFriendData.add(relAndFriendBean);
            }
        }
        return hasShareFriendData;
    }

    /**
     * desc:处理设备分享的数据
     */
    private void handlerShareDeviceListData(ArrayList<DeviceBean> shareDeviceList) {
        if (shareDeviceList != null && shareDeviceList.size() != 0) {
            if (getView() != null) {
                getView().initRecycleView(shareDeviceList);
            }
        } else {
            if (getView() != null) {
                getView().hideLoadingDialog();
                getView().showNoDeviceView();
            }
        }
    }

    /**
     * desc:获取到分享设备的list集合数据
     */
    private ArrayList<DeviceBean> getShareDeviceList() {
        ArrayList<DeviceBean> list = new ArrayList<>();
        List<Device> devices = DataSourceManager.getInstance().getAllJFGDevice();
        for (Device info : devices) {
            DeviceBean bean = new DeviceBean();
            bean.alias = info.alias;
            bean.pid = info.pid;
            bean.uuid = info.uuid;
            bean.shareAccount = info.shareAccount;
            bean.sn = info.sn;
            list.add(bean);
        }
        return list;
    }


    @Override
    public ArrayList<RelAndFriendBean> getHasShareRelAndFriendList(JFGShareListInfo info) {

        ArrayList<RelAndFriendBean> list = new ArrayList<>();

        for (JFGFriendAccount account : info.friends) {
            RelAndFriendBean bean = new RelAndFriendBean();
            bean.account = account.account;
            bean.alias = account.alias;
            //TODO 具体赋值
            list.add(bean);
        }
        return list;
    }

    /**
     * 获取到设备已分享的亲友数
     *
     * @param cid
     */
    @Override
    public void getDeviceInfo(ArrayList<String> cid) {
        rx.Observable.just(cid)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<ArrayList<String>>() {
                    @Override
                    public void call(ArrayList<String> cid) {
                        if (cid != null && cid.size() != 0) {
                            JfgCmdInsurance.getCmd().getShareList(cid);
                        }
                    }
                }, throwable -> {
                    AppLogger.e("getDeviceInfo" + throwable.getLocalizedMessage());
                });
    }

    /**
     * 获取到已经分享的亲友数的回调
     *
     * @return
     */
    @Override
    public Subscription getDeviceInfoCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetShareListCallBack.class)
                .flatMap(new Func1<RxEvent.GetShareListCallBack, Observable<ArrayList<DeviceBean>>>() {
                    @Override
                    public Observable<ArrayList<DeviceBean>> call(RxEvent.GetShareListCallBack getShareListCallBack) {
                        if (getShareListCallBack != null) {
                            if (getShareListCallBack.i == 0 && getShareListCallBack.arrayList.size() != 0) {
                                //每个设备已分享的亲友集合
                                hasShareFriendList.clear();
                                hasShareFriendList.addAll(getShareListCallBack.arrayList);
                                //该设备以分享的亲友数赋值
                                for (int i = 0; i < allDevice.size(); i++) {
                                    if (allDevice.get(i).uuid.equals(getShareListCallBack.arrayList.get(i).cid)) {
                                        allDevice.get(i).hasShareCount = getShareListCallBack.arrayList.get(i).friends.size();
                                    }
                                }
                                return Observable.just(allDevice);
                            } else {
                                return Observable.just(allDevice);
                            }
                        } else {
                            return Observable.just(allDevice);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<DeviceBean>>() {
                    @Override
                    public void call(ArrayList<DeviceBean> deviceBeen) {
                        handlerShareDeviceListData(deviceBeen);
                    }
                });
    }

    /**
     * 检测是否拥有联系人的权限
     */
    @Override
    public boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(getView().getContext(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void unShareSucceedDel(int position, ArrayList<String> arrayList) {
        Iterator iterator = hasShareFriendList.get(position).friends.iterator();
        Iterator iterator2 = shareSucceedData.iterator();
        while (iterator.hasNext()) {
            JFGFriendAccount friend = (JFGFriendAccount) iterator.next();
            for (String str : arrayList) {
                if (friend.account.equals(str)) {
                    iterator.remove();
                }
            }
        }
        while (iterator2.hasNext()) {
            RelAndFriendBean friend = (RelAndFriendBean) iterator2.next();
            for (String str : arrayList) {
                if (friend.account.equals(str)) {
                    iterator2.remove();
                }
            }
        }
    }

    @Override
    public void shareSucceedAdd(ArrayList<RelAndFriendBean> list) {
//        shareSucceedData.clear();
        shareSucceedData.addAll(list);
    }

    @Override
    public void clearData() {
        shareSucceedData.clear();
    }

}
