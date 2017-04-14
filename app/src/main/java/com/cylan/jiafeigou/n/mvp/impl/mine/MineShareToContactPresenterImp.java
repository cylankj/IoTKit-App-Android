package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToContactContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

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
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToContactPresenterImp extends AbstractPresenter<MineShareToContactContract.View>
        implements MineShareToContactContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private ArrayList<RelAndFriendBean> filterDateList;
    private ArrayList<RelAndFriendBean> allCoverData = new ArrayList<>();
    private ArrayList<RelAndFriendBean> hasShareFriend;

    public MineShareToContactPresenterImp(MineShareToContactContract.View view, ArrayList<RelAndFriendBean> hasShareFiend) {
        super(view);
        view.setPresenter(this);
        this.hasShareFriend = hasShareFiend;
    }

    @Override
    public void start() {
        if (hasShareFriend != null && hasShareFriend.size() != 0) {
            ArrayList<RelAndFriendBean> list = converData2(hasShareFriend);
            allCoverData.addAll(list);
            handlerContactDataResult(list);
        } else {
            ArrayList<RelAndFriendBean> list = getAllContactList();
            allCoverData.addAll(list);
            handlerContactDataResult(list);
        }

        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
//            compositeSubscription.add(getHasShareContractCallBack());
            compositeSubscription.add(shareDeviceCallBack());
        }
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }

    @Override
    public void handlerSearchResult(String inputContent) {
        filterDateList = new ArrayList<>();
        if (TextUtils.isEmpty(inputContent)) {
            filterDateList.clear();
            filterDateList.addAll(allCoverData);
        } else {
            filterDateList.clear();
            for (RelAndFriendBean s : allCoverData) {
                String phone = s.account;
                String name = s.alias;
                if (phone.replace(" ", "").contains(inputContent) || name.contains(inputContent)) {
                    filterDateList.add(s);
                }
            }
        }
        handlerContactDataResult(filterDateList);
    }

    @Override
    public void handlerShareClick(final String cid, String item) {
        rx.Observable.just(item)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String account) {
                        try {
                           BaseApplication.getAppComponent().getCmd().shareDevice(cid, account);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("handlerShareClick" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到已经分享的亲友数
     *
     * @param cid
     * @return
     */
    @Override
    public Subscription getHasShareContract(final String cid) {
        return rx.Observable.just(cid)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            BaseApplication.getAppComponent().getCmd().getUnShareListByCid(cid);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getHasShareContract" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到已经分享好友的回调
     *
     * @return
     */
    @Override
    public Subscription getHasShareContractCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetHasShareFriendCallBack.class)
                .flatMap(new Func1<RxEvent.GetHasShareFriendCallBack, Observable<ArrayList<RelAndFriendBean>>>() {
                    @Override
                    public Observable<ArrayList<RelAndFriendBean>> call(RxEvent.GetHasShareFriendCallBack getHasShareFriendCallBack) {
                        if (getHasShareFriendCallBack != null) {

                            if (getHasShareFriendCallBack.arrayList.size() != 0) {
                                return Observable.just(converData(getHasShareFriendCallBack.arrayList));
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
                        allCoverData.addAll(list);
                        handlerContactDataResult(list);
                    }
                }, AppLogger::e);
    }

    /**
     * 分享设备的回调
     *
     * @return
     */
    @Override
    public Subscription shareDeviceCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ShareDeviceCallBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ShareDeviceCallBack>() {
                    @Override
                    public void call(RxEvent.ShareDeviceCallBack shareDeviceCallBack) {
                        if (shareDeviceCallBack != null) {
                            if (getView() != null) {
                                getView().hideShareingProHint();
                                getView().handlerCheckRegister(shareDeviceCallBack.requestId, shareDeviceCallBack.account);
                            }
                        }
                    }
                }, AppLogger::e);
    }

    /**
     * 检测发送短信权限
     *
     * @return
     */
    @Override
    public boolean checkSendSmsPermission() {
        if (ContextCompat.checkSelfPermission(getView().getContext(),
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 数据的转换 标记已分享和未分享
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
        return list;
    }


    /**
     * 数据的转换 标记已分享和未分享
     *
     * @param arrayList
     * @return
     */
    private ArrayList<RelAndFriendBean> converData2(ArrayList<RelAndFriendBean> arrayList) {
        ArrayList<RelAndFriendBean> list = new ArrayList<>();
        for (RelAndFriendBean contract : getAllContactList()) {
            for (RelAndFriendBean friend : arrayList) {
                if (friend.account.equals(contract.account)) {
                    contract.isCheckFlag = 1;
                } else {
                    contract.isCheckFlag = 0;
                }
            }
            list.add(contract);
        }
        return list;
    }

    /**
     * desc:处理得到的数据结果
     *
     * @param list
     */
    private void handlerContactDataResult(ArrayList<RelAndFriendBean> list) {
        if (getView() != null && list != null && list.size() != 0) {
            Collections.sort(list, new Comparent());
            getView().hideNoContactNullView();
            getView().initContactReclyView(list);
        } else {
            getView().showNoContactNullView();
        }
    }

    @NonNull
    public ArrayList<RelAndFriendBean> getAllContactList() {
        ArrayList<RelAndFriendBean> list = new ArrayList<RelAndFriendBean>();
        //得到ContentResolver对象
        ContentResolver cr = getView().getContext().getContentResolver();
        //取得电话本中开始一项的光标
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor == null) {
            return list;
        }
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

            //****获取邮箱
            Cursor emails = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + ContactId, null, null);
            int emailIndex = 0;
            if (emails.getCount() > 0) {
                emailIndex = emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
            }
            while (emails.moveToNext()) {
                String email = emails.getString(emailIndex);
                RelAndFriendBean friendBean = new RelAndFriendBean();
                friendBean.alias = contact;
                friendBean.account = email;
                if (JConstant.EMAIL_REG.matcher(friendBean.account).find()) {
                    list.add(friendBean);
                }
            }
            emails.close();
        }
        cursor.close();
        return list;
    }

    public class Comparent implements Comparator<RelAndFriendBean> {
        @SuppressWarnings("unchecked")
        @Override
        public int compare(RelAndFriendBean lhs, RelAndFriendBean rhs) {
            Collator ca = Collator.getInstance(Locale.CHINA);
            int flags = 0;
            if (ca.compare(lhs.alias.toLowerCase(), rhs.alias.toLowerCase()) < 0) {
                flags = -1;
            } else if (ca.compare(lhs.alias.toLowerCase(), rhs.alias.toLowerCase()) > 0) {
                flags = 1;
            } else {
                flags = 0;
            }
            return flags;
        }
    }
}
