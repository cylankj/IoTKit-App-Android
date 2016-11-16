package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import java.util.ArrayList;

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
    private ArrayList<RelAndFriendBean> allContactBean;
    public MineFriendAddFromContactPresenterImp(MineFriendAddFromContactContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getFriendListData());
            compositeSubscription.add(getFriendListDataCallBack());
            compositeSubscription.add(checkFriendAccountCallBack());
        }
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }
    }

    @Override
    public Subscription initContactData() {
        return Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<Object, ArrayList<RelAndFriendBean>>() {
                    @Override
                    public ArrayList call(Object o) {
                        return getAllContactList();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<RelAndFriendBean>>() {
                    @Override
                    public void call(ArrayList<RelAndFriendBean> arrayList) {
                        handlerDataResult(arrayList);
                    }
                });
    }

    /**
     * desc：处理获取到的联系人数据
     * @param arrayList
     */
    private void handlerDataResult(ArrayList<RelAndFriendBean> arrayList) {
        if (arrayList != null){
            if (arrayList.size() != 0 && getView() != null){
                getView().initContactRecycleView(arrayList);
                getView().hideNoContactView();
            }else {
                getView().showNoContactView();
            }
        }else {
            getView().showNoContactView();
        }
    }

    @NonNull
    public ArrayList<RelAndFriendBean> getAllContactList() {
        ArrayList<RelAndFriendBean> list = new ArrayList<RelAndFriendBean>();
        Cursor cursor = null;
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        // 这里是获取联系人表的电话里的信息  包括：名字，名字拼音，联系人id,电话号码；
        // 然后在根据"sort-key"排序
        cursor = getView().getContext().getContentResolver().query(
                uri,
                new String[]{"display_name", "sort_key", "contact_id",
                        "data1"}, null, null, "sort_key");

        if (cursor.moveToFirst()) {
            do {
                RelAndFriendBean contact = new RelAndFriendBean();
                String contact_phone = cursor
                        .getString(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String name = cursor.getString(0);
                contact.account = contact_phone;
                contact.alias = name;
                if (name != null)
                    list.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @Override
    public void addContactItem(RelAndFriendBean bean) {

    }

    @Override
    public void filterPhoneData(String filterStr) {
        filterDateList = new ArrayList<>();
        if (allContactBean.size() != 0){
            if (TextUtils.isEmpty(filterStr)) {
                filterDateList = allContactBean;
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
     * @return
     */
    @Override
    public Subscription getFriendListData() {
        return rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        JfgCmdInsurance.getCmd().getFriendList();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getFriendListData"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取好友列表的回调
     * @return
     */
    @Override
    public Subscription getFriendListDataCallBack() {
        return RxBus.getDefault().toObservable(RxEvent.GetFriendList.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetFriendList>() {
                    @Override
                    public void call(RxEvent.GetFriendList getFriendList) {
                        if (getFriendList != null && getFriendList instanceof RxEvent.GetFriendList){
                            allContactBean = new ArrayList<RelAndFriendBean>();
                            allContactBean.addAll(handlerData(getFriendList.arrayList));
                            handlerDataResult(allContactBean);
                        }
                    }
                });
    }

    /**
     * 检测好友的账号是否已经注册
     * @param account
     */
    @Override
    public void checkFriendAccount(final String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        JfgCmdInsurance.getCmd().checkFriendAccount(account);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("checkFriendAccount"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 检测好友的账号是否已经注册回调
     * @return
     */
    @Override
    public Subscription checkFriendAccountCallBack() {
        return RxBus.getDefault().toObservable(RxEvent.CheckAccountCallback.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckAccountCallback>() {
                    @Override
                    public void call(RxEvent.CheckAccountCallback checkAccountCallback) {
                        if (checkAccountCallback != null && checkAccountCallback instanceof RxEvent.CheckAccountCallback){
                            getView().hideLoadingPro();
                            handlerCheckAccountResult(checkAccountCallback);
                        }
                    }
                });
    }

    /**
     * 处理检测账号的结果
     * @param checkAccountCallback
     */
    private void handlerCheckAccountResult(RxEvent.CheckAccountCallback checkAccountCallback) {
        if (getView() != null){
            if (checkAccountCallback.i == 240){
                //未注册发送短信
                getView().sendSms();
            }else if (checkAccountCallback.i == 0){
                //已注册
                getView().jump2SendAddMesgFragment();
            }
        }
    }

    /**
     * //数据处理标记已添加与否
     */
    private ArrayList<RelAndFriendBean> handlerData(ArrayList<JFGFriendAccount> getFriendList) {
        ArrayList<RelAndFriendBean> tempList = new ArrayList<>();
        ArrayList<RelAndFriendBean> resultList = new ArrayList<>();

        for (RelAndFriendBean friendBean:getAllContactList()){
            if (friendBean.account.startsWith("+86")){
                friendBean.account = friendBean.account.substring(3);
            }else if (friendBean.account.startsWith("86")){
                friendBean.account = friendBean.account.substring(2);
            }

            if (JConstant.PHONE_REG.matcher(friendBean.account).find()){
                tempList.add(friendBean);
            }
        }

        for (RelAndFriendBean friendBean:tempList){
            if(tempList.size() != 0){
                for (JFGFriendAccount account:getFriendList){
                    if (friendBean.account.equals(account.account)){
                        friendBean.isCheckFlag = 1;
                    }else {
                        friendBean.isCheckFlag = 0;
                    }
                }
            }
            resultList.add(friendBean);
        }
        return resultList;
    }
}
