package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.module.FriendBean;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.AddFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.NetUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class AddFriendsContactImp extends AbstractPresenter<AddFriendContract.View> implements AddFriendContract.Presenter {

    private ArrayList<FriendBean> filterDateList;
    private ArrayList<FriendBean> allContactBean = new ArrayList<FriendBean>();
    private boolean isCheckAcc;
    private BaseDBHelper helper;

    public AddFriendsContactImp(AddFriendContract.View view) {
        super(view);
        view.setPresenter(this);
        helper = (BaseDBHelper) BaseApplication.getAppComponent().getDBHelper();
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                getFriendListDataCallBack(),
                checkFriendAccountCallBack()};
    }


    /**
     * desc：处理获取到的联系人数据
     *
     * @param arrayList
     */
    private void handlerDataResult(ArrayList<FriendBean> arrayList) {
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
    public ArrayList<FriendBean> getAllContactList() {
        ArrayList<FriendBean> list = new ArrayList<FriendBean>();
        //得到ContentResolver对象
        ContentResolver cr = getView().getContext().getContentResolver();
        //取得电话本中开始一项的光标
        String sort = "sort_key";
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            sort = "phonebook_label";
        }
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, sort);
        if (cursor == null) return list;
        //向下移动光标
        while (cursor.moveToNext()) {
            //取得联系人名字
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            String contact = cursor.getString(nameFieldColumnIndex);
            //取得电话号码
            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
            String sort_key = cursor.getString(cursor.getColumnIndex(sort));
            while (phone.moveToNext()) {
                FriendBean friendBean = new FriendBean();
                friendBean.alias = contact;
                String PhoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                PhoneNumber = PhoneNumber.replace("-", "");
                PhoneNumber = PhoneNumber.replace(" ", "");
                friendBean.account = PhoneNumber;
                friendBean.sortkey = sort_key;
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
                FriendBean friendBean = new FriendBean();
                String email = emails.getString(emailIndex);
                friendBean.alias = contact;
                friendBean.account = email;
                friendBean.sortkey = sort_key;
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
                for (FriendBean s : allContactBean) {
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
                .subscribe(o -> BaseApplication.getAppComponent().getCmd().getFriendList(),
                        throwable -> AppLogger.e("getFriendListData" + throwable.getLocalizedMessage()));
    }

    /**
     * 获取好友列表的回调
     *
     * @return
     */
    @Override
    public Subscription getFriendListDataCallBack() {
//        return RxBus.getCacheInstance().toObservable(RxEvent.GetFriendList.class)
//                .flatMap(getFriendList -> {
//                    ArrayList<FriendBean> list = BaseApplication.getAppComponent().getSourceManager().getFriendsList();
//                    if (ListUtils.getSize(list) != 0) {
//                        return Observable.just(list);
//                    } else {
//                        return Observable.just(getAllContactList());
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(list -> {
//                    if (allContactBean.size() > 0) {
//                        allContactBean.clear();
//                    }
//                    allContactBean.addAll(list);
//                    handlerDataResult(list);
//                }, AppLogger::e);
        return null;
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
                .subscribe(s -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().checkFriendAccount(account);
                        isCheckAcc = true;
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
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
                .subscribe(checkAccountCallback -> {
                    if (checkAccountCallback != null && isCheckAcc) {
                        getView().hideLoadingPro();
                        handlerCheckAccountResult(checkAccountCallback);
                        isCheckAcc = false;
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
    private ArrayList<FriendBean> converData(ArrayList<JFGFriendAccount> arrayList) {
        ArrayList<FriendBean> list = new ArrayList<>();
        for (FriendBean contract : getAllContactList()) {
            for (JFGFriendAccount friend : arrayList) {
                if (friend.account.equals(contract.account)) {
                    contract.isCheckFlag = 1;
                } else {
                    contract.isCheckFlag = 0;
                }
            }
            list.add(contract);
        }
        Collections.sort(list, new AComparator());
        return list;
    }

    public class AComparator implements Comparator<FriendBean> {
        @SuppressWarnings("unchecked")
        @Override
        public int compare(FriendBean lhs, FriendBean rhs) {
            Collator ca = Collator.getInstance(Locale.getDefault());
            int flags = 0;
            if (ca.compare(lhs.sortkey, rhs.sortkey) < 0) {
                flags = -1;
            } else if (ca.compare(lhs.sortkey, rhs.sortkey) > 0) {
                flags = 1;
            } else {
                flags = 0;
            }
            return flags;
        }
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{
                ConnectivityManager.CONNECTIVITY_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        if (mView == null || !mView.isAdded()) return;
        Observable.just(NetUtils.getJfgNetType())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> getView().onNetStateChanged(integer),
                        AppLogger::e);
    }
}
