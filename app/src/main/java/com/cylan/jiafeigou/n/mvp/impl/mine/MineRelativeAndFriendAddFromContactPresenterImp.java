package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativeAndFriendAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
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
public class MineRelativeAndFriendAddFromContactPresenterImp implements MineRelativeAndFriendAddFromContactContract.Presenter {

    static Uri contactsUri = Uri
            .parse("content://com.android.contacts/raw_contacts");
    static Uri dataUri = Uri.parse("content://com.android.contacts/data");


    private MineRelativeAndFriendAddFromContactContract.View view;
    private Subscription contactSubscriber;
    private ArrayList<SuggestionChatInfoBean> list;

    public MineRelativeAndFriendAddFromContactPresenterImp(MineRelativeAndFriendAddFromContactContract.View view) {
        this.view = view;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void initContactData() {

        list = new ArrayList<SuggestionChatInfoBean>();

        contactSubscriber = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<Object,ArrayList<SuggestionChatInfoBean>>() {
                    @Override
                    public ArrayList call(Object o) {
                        return getAllContactList();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<SuggestionChatInfoBean>>() {
                    @Override
                    public void call(ArrayList<SuggestionChatInfoBean> arrayList) {
                        view.setRcyAdapter(arrayList);
                        view.InitItemClickListener();
                    }
                });
    }

    @NonNull
    public ArrayList<SuggestionChatInfoBean> getAllContactList() {
        ArrayList<SuggestionChatInfoBean> list = new ArrayList<SuggestionChatInfoBean>();

        ContentResolver resolver = view.getContext().getContentResolver();
        Cursor contact_cursor = resolver.query(contactsUri,
                new String[] { "contact_id" }, null, null, null);
        while (contact_cursor.moveToNext()) {
            // 获取到raw_contacts表中的contact_id
            String contact_id = contact_cursor.getString(0);
            if (contact_id != null) {
                SuggestionChatInfoBean contact = new SuggestionChatInfoBean("",1,"");

                Cursor data_cursor = resolver.query(dataUri, new String[] {
                                "data1", "mimetype" }, "raw_contact_id=?",
                        new String[] { contact_id }, null);
                while (data_cursor.moveToNext()) {
                    // 获取到data表中的"data1",mimetypes表中的"mimetype"
                    String data1 = data_cursor.getString(0);
                    String mimetypes = data_cursor.getString(1);

                    // 根据mimetypes判断data1所属的数据类型
                    if ("vnd.android.cursor.item/phone_v2".equals(mimetypes)) {
                        contact.setContent(data1);
                    }
                    if ("vnd.android.cursor.item/name".equals(mimetypes)) {
                        contact.setName(data1);
                    }
                }
                data_cursor.close();
                list.add(contact);
            }
        }
        contact_cursor.close();
        return list;
    }

    @Override
    public void addContactItem(SuggestionChatInfoBean bean) {

    }
}
