package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendAddFromContactPresenterImp extends AbstractPresenter<MineFriendAddFromContactContract.View> implements MineFriendAddFromContactContract.Presenter {

    private ArrayList<RelAndFriendBean> filterDateList;
    private CompositeSubscription compositeSubscription;
    private ArrayList<RelAndFriendBean> allContactBean = new ArrayList<RelAndFriendBean>();
    private Network network;
    private boolean isCheckAcc;

    public MineFriendAddFromContactPresenterImp(MineFriendAddFromContactContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getFriendListDataCallBack());
            compositeSubscription.add(checkFriendAccountCallBack());
        }
        registerNetworkMonitor();
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
        unregisterNetworkMonitor();
    }


    /**
     * desc：处理获取到的联系人数据
     *
     * @param arrayList
     */
    private void handlerDataResult(ArrayList<RelAndFriendBean> arrayList) {
        if (arrayList != null) {
            if (arrayList.size() != 0 && getView() != null) {
                getView().initContactRecycleView(arrayList);
                getView().hideNoContactView();
            } else {
                getView().showNoContactView();
            }
        } else {
            getView().showNoContactView();
        }
    }

    /**
     * 获取到过滤后的所有的联系人
     *
     * @return
     */
    @NonNull
    public ArrayList<RelAndFriendBean> getAllContactList() {
        ArrayList<RelAndFriendBean> list = new ArrayList<RelAndFriendBean>();
        //得到ContentResolver对象
        ContentResolver cr = getView().getContext().getContentResolver();
        //取得电话本中开始一项的光标
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor == null) return list;
        //向下移动光标
        while (cursor.moveToNext()) {
            //取得联系人名字
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            String contact = cursor.getString(nameFieldColumnIndex);
            //取得电话号码
            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
            while (phone.moveToNext()) {
                RelAndFriendBean friendBean = new RelAndFriendBean();
                friendBean.alias = contact;
                String PhoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                PhoneNumber = PhoneNumber.replace("-", "");
                PhoneNumber = PhoneNumber.replace(" ", "");
                friendBean.account = PhoneNumber;
                if (friendBean.account.startsWith("+86")) {
                    friendBean.account = friendBean.account.substring(3);
                } else if (friendBean.account.startsWith("86")) {
                    friendBean.account = friendBean.account.substring(2);
                }
                if (JConstant.PHONE_REG.matcher(friendBean.account).find()) {
                    list.add(friendBean);
                }
            }
            phone.close();

            Cursor emails = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + ContactId, null, null);
            int emailIndex = 0;
            if (emails.getCount() > 0) {
                emailIndex = emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
            }
            while (emails.moveToNext()) {
                RelAndFriendBean friendBean = new RelAndFriendBean();
                String email = emails.getString(emailIndex);
                friendBean.alias = contact;
                friendBean.account = email;
                if (JConstant.EMAIL_REG.matcher(email).find()) {
                    list.add(friendBean);
                }
            }
            emails.close();
        }
        cursor.close();
        return list;
    }

    @Override
    public void filterPhoneData(String filterStr) {
        filterDateList = new ArrayList<>();
        if (allContactBean.size() != 0) {
            if (TextUtils.isEmpty(filterStr)) {
                filterDateList.clear();
                filterDateList.addAll(allContactBean);
            } else {
                filterDateList.clear();
                for (RelAndFriendBean s : allContactBean) {
                    String phone = s.account;
                    String name = s.alias;
                    if (phone.replace(" ", "").contains(filterStr) || name.contains(filterStr)) {
                        filterDateList.add(s);
                    }
                }
            }
        }
        handlerDataResult(filterDateList);
    }

    /**
     * 获取好友列表的数据
     *
     * @return
     */
    @Override
    public void getFriendListData() {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        BaseApplication.getAppComponent().getCmd().getFriendList();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getFriendListData" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取好友列表的回调
     *
     * @return
     */
    @Override
    public Subscription getFriendListDataCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetFriendList.class)
                .flatMap(new Func1<RxEvent.GetFriendList, Observable<ArrayList<RelAndFriendBean>>>() {
                    @Override
                    public Observable<ArrayList<RelAndFriendBean>> call(RxEvent.GetFriendList getFriendList) {
                        if (getFriendList != null) {
                            if (getFriendList.arrayList.size() != 0) {
                                return Observable.just(converData(getFriendList.arrayList));
                            } else {
                                return Observable.just(getAllContactList());
                            }
                        } else {
                            return Observable.just(getAllContactList());
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<RelAndFriendBean>>() {
                    @Override
                    public void call(ArrayList<RelAndFriendBean> list) {
                        allContactBean.addAll(list);
                        handlerDataResult(list);
                    }
                }, AppLogger::e);
    }

    /**
     * 检测好友的账号是否已经注册
     *
     * @param account
     */
    @Override
    public void checkFriendAccount(final String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            BaseApplication.getAppComponent().getCmd().checkFriendAccount(account);
                            isCheckAcc = true;
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("checkFriendAccount" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 检测好友的账号是否已经注册回调
     *
     * @return
     */
    @Override
    public Subscription checkFriendAccountCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckAccountCallback>() {
                    @Override
                    public void call(RxEvent.CheckAccountCallback checkAccountCallback) {
                        if (checkAccountCallback != null && isCheckAcc) {
                            getView().hideLoadingPro();
                            handlerCheckAccountResult(checkAccountCallback);
                            isCheckAcc = false;
                        }
                    }
                }, AppLogger::e);
    }

    /**
     * 检测短信权限
     *
     * @return
     */
    @Override
    public boolean checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(getView().getContext(),
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 处理检测账号的结果
     *
     * @param checkAccountCallback
     */
    private void handlerCheckAccountResult(RxEvent.CheckAccountCallback checkAccountCallback) {
        if (getView() != null) {
            if (checkAccountCallback.i == 240) {
                //未注册发送短信
                getView().openSendSms();
            } else if (checkAccountCallback.i == 0) {
                //已注册
                getView().jump2SendAddMesgFragment();
            }
        }
    }

    /**
     * 数据的转换 标记已添加和未添加
     *
     * @param arrayList
     * @return
     */
    private ArrayList<RelAndFriendBean> converData(ArrayList<JFGFriendAccount> arrayList) {
        ArrayList<RelAndFriendBean> list = new ArrayList<>();
        for (RelAndFriendBean contract : getAllContactList()) {
            for (JFGFriendAccount friend : arrayList) {
                if (friend.account.equals(contract.account)) {
                    contract.isCheckFlag = 1;
                } else {
                    contract.isCheckFlag = 0;
                }
            }
            list.add(contract);
        }
        Collections.sort(list, new Comparent());
        return list;
    }

    public class Comparent implements Comparator<RelAndFriendBean> {
        @SuppressWarnings("unchecked")
        @Override
        public int compare(RelAndFriendBean lhs, RelAndFriendBean rhs) {
            Collator ca = Collator.getInstance(Locale.CHINA);
            int flags = 0;
            if (ca.compare(lhs.alias, rhs.alias) < 0) {
                flags = -1;
            } else if (ca.compare(lhs.alias, rhs.alias) > 0) {
                flags = 1;
            } else {
                flags = 0;
            }
            return flags;
        }
    }

    @Override
    public void registerNetworkMonitor() {
        try {
            if (network == null) {
                network = new Network();
                final IntentFilter filter = new IntentFilter();
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                ContextUtils.getContext().registerReceiver(network, filter);
            }
        } catch (Exception e) {
            AppLogger.e("registerNetworkMonitor" + e.getLocalizedMessage());
        }
    }

    @Override
    public void unregisterNetworkMonitor() {
        if (network != null) {
            ContextUtils.getContext().unregisterReceiver(network);
            network = null;
        }
    }

    /**
     * 监听网络状态
     */
    private class Network extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
                updateConnectivityStatus(status.state);
            }
        }
    }

    /**
     * 连接状态变化
     */
    private void updateConnectivityStatus(int network) {
        Observable.just(network)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return getView() != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        getView().onNetStateChanged(integer);
                    }
                }, AppLogger::e);
    }
}
