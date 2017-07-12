package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToContactContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.n.view.adapter.item.ShareContactItem;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToContactPresenterImp extends AbstractPresenter<MineShareToContactContract.View>
        implements MineShareToContactContract.Presenter {


    public MineShareToContactPresenterImp(MineShareToContactContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void checkAndInitContactList(int contactType) {
        Subscription subscribe = Observable.just("checkAndInitContactList")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    ArrayList<ShareContactItem> result = new ArrayList<>();
                    ContentResolver contentResolver = getView().getContext().getContentResolver();
                    Cursor phoneQuery = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
                    Account account = DataSourceManager.getInstance().getAccount();
                    //向下移动光标
                    ShareContactItem shareContactItem;
                    while (phoneQuery != null && phoneQuery.moveToNext()) {
                        String displayName = phoneQuery.getString(phoneQuery.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY));
                        String phoneNumber = phoneQuery.getString(phoneQuery.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        shareContactItem = new ShareContactItem();
                        shareContactItem.name = displayName;
                        shareContactItem.contactType = contactType;
                        result.add(shareContactItem);
                        if (phoneNumber != null) {
                            phoneNumber = phoneNumber.replace("-", "").replace(" ", "");
                            if (phoneNumber.startsWith("+86")) {
                                phoneNumber = phoneNumber.substring(3);
                            } else if (phoneNumber.startsWith("86")) {
                                phoneNumber = phoneNumber.substring(2);
                            }
                        }
                        shareContactItem.phone = phoneNumber;
                        if (TextUtils.equals(phoneNumber, account.getAccount())) {
                            //说明是自己
                            shareContactItem.shared = true;
                        }
                        AppLogger.d("添加电话联系人:" + new Gson().toJson(shareContactItem));
                    }
                    if (phoneQuery != null) phoneQuery.close();
                    Cursor emailQuery = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Email.SORT_KEY_PRIMARY);
                    while (emailQuery != null && emailQuery.moveToNext()) {
                        String displayName = emailQuery.getString(emailQuery.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME_PRIMARY));
                        String emailAddress = emailQuery.getString(emailQuery.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                        shareContactItem = new ShareContactItem();
                        shareContactItem.name = displayName;
                        shareContactItem.contactType = contactType;
                        shareContactItem.email = emailAddress;
                        result.add(shareContactItem);
                        if (TextUtils.equals(emailAddress, account.getAccount())) {
                            //说明是自己
                            shareContactItem.shared = true;
                        }
                        AppLogger.d("添加邮箱联系人:" + new Gson().toJson(shareContactItem));
                    }
                    if (emailQuery != null) emailQuery.close();

                    JFGShareListInfo jfgShareListInfo = DataSourceManager.getInstance().getShareListByCid(getUuid());
                    if (jfgShareListInfo != null && jfgShareListInfo.friends != null) {
                        for (JFGFriendAccount friend : jfgShareListInfo.friends) {
                            for (ShareContactItem contactItem : result) {
                                if (TextUtils.equals(friend.account, contactItem.phone)
                                        || TextUtils.equals(friend.account, contactItem.email)) {
                                    contactItem.shared = true;
                                }
                            }
                        }
                    }
                    return result;
                })
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading(R.string.LOADING))
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(result -> {
                    getView().onInitContactFriends(result);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

    @Override
    public void shareDeviceToContact(ShareContactItem shareContactItem) {
        Subscription subscribe = Observable.just("shareDeviceToContact")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().shareDevice(getUuid(), shareContactItem.getAccount());
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(e.getMessage());
                    }
                    return cmd;
                })
                .flatMap(cmd -> RxBus.getCacheInstance().toObservable(RxEvent.ShareDeviceCallBack.class).first())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .doOnSubscribe(() -> getView().showLoading(R.string.LOADING))
                .doOnTerminate(() -> getView().hideLoading())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    getView().onShareDeviceResult(shareContactItem, result);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

    @Override
    public void checkFriendAccount(ShareContactItem item) {
        Subscription subscribe = Observable.just("addFriendByContact")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().checkFriendAccount(item.getAccount());
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class).first())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .doOnSubscribe(() -> getView().showLoading(R.string.getting))
                .doOnTerminate(() -> getView().hideLoading())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    FriendContextItem friendContextItem = null;
                    boolean accountExist = true;
                    if (result != null) {
                        JFGSourceManager manager = BaseApplication.getAppComponent().getSourceManager();
                        if (result.isFriend) {
                            ArrayList<JFGFriendAccount> friendsList = manager.getFriendsList();
                            JFGFriendAccount friendAccount = null;
                            if (friendsList != null) {
                                for (JFGFriendAccount friend : friendsList) {
                                    if (TextUtils.equals(friend.account, result.account)) {
                                        friendAccount = friend;
                                        break;
                                    }
                                }
                            }
                            if (friendAccount == null) {
                                friendAccount = new JFGFriendAccount(result.account, null, result.alias);
                            }
                            friendContextItem = new FriendContextItem(friendAccount);
                        } else if (result.code == 240) {//未注册
                            accountExist = false;
                            friendContextItem = new FriendContextItem(new JFGFriendAccount(item.getAccount(), null, null));
                        } else {//添加
                            ArrayList<JFGFriendRequest> friendsRequestList = manager.getFriendsReqList();
                            JFGFriendRequest friendRequest = null;
                            if (friendsRequestList != null) {
                                for (JFGFriendRequest request : friendsRequestList) {
                                    if (TextUtils.equals(request.account, request.account)) {
                                        friendRequest = request;
                                        break;
                                    }
                                }
                            }
                            if (friendRequest == null) {
                                friendRequest = new JFGFriendRequest();
                                friendRequest.time = System.currentTimeMillis();
                                friendRequest.alias = result.alias;
                                friendRequest.account = result.account;
                                friendRequest.sayHi = null;
                            }
                            friendContextItem = new FriendContextItem(friendRequest);
                        }
                    }
                    getView().onCheckFriendAccountResult(friendContextItem, item, accountExist);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

}
