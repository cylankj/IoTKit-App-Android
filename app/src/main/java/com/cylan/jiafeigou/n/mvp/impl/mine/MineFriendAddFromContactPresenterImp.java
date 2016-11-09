package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendAddFromContactPresenterImp extends AbstractPresenter<MineFriendAddFromContactContract.View> implements MineFriendAddFromContactContract.Presenter {

    private Subscription contactSubscriber;
    private ArrayList<RelAndFriendBean> filterDateList;

    public MineFriendAddFromContactPresenterImp(MineFriendAddFromContactContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (contactSubscriber != null && !contactSubscriber.isUnsubscribed()){
            contactSubscriber.unsubscribe();
        }
        initContactData();
    }

    @Override
    public void stop() {
        if (contactSubscriber != null) {
            contactSubscriber.unsubscribe();
        }
    }

    @Override
    public void initContactData() {

        contactSubscriber = Observable.just(null)
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
        if (arrayList != null && arrayList.size() != 0 && getView() != null){
            getView().initContactRecycleView(arrayList);
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
        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = getAllContactList();
        } else {
            filterDateList.clear();
            for (RelAndFriendBean s : getAllContactList()) {
                String phone = s.account;
                String name = s.alias;
                if (phone.replace(" ", "").contains(filterStr) || name.contains(filterStr)) {
                    filterDateList.add(s);
                }
            }
        }
        handlerDataResult(filterDateList);
    }

}
