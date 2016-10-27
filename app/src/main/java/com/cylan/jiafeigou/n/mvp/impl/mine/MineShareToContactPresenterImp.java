package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToContactContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.n.view.adapter.ShareToContactAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToContactPresenterImp extends AbstractPresenter<MineShareToContactContract.View>
        implements MineShareToContactContract.Presenter, ShareToContactAdapter.onShareLisenter {

    private Subscription shareToContactSub;
    private ShareToContactAdapter shareToContactAdapter;
    private ArrayList<SuggestionChatInfoBean> filterDateList;

    public MineShareToContactPresenterImp(MineShareToContactContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        initContactData();
    }

    @Override
    public void stop() {
        if (shareToContactSub != null && shareToContactSub.isUnsubscribed()) {
            shareToContactSub.unsubscribe();
        }
    }

    @Override
    public void initContactData() {

        shareToContactSub = Observable.just(null)
                .map(new Func1<Object, ArrayList<SuggestionChatInfoBean>>() {
                    @Override
                    public ArrayList<SuggestionChatInfoBean> call(Object o) {
                        return getAllContactList();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<SuggestionChatInfoBean>>() {
                    @Override
                    public void call(ArrayList<SuggestionChatInfoBean> list) {
                        handlerContactDataResult(list);
                    }
                });
    }

    @Override
    public void handleSearchResult(String inputContent) {
        filterDateList = new ArrayList<>();
        if (TextUtils.isEmpty(inputContent)) {
            filterDateList = getAllContactList();
        } else {
            filterDateList.clear();
            for (SuggestionChatInfoBean s : getAllContactList()) {
                String phone = s.getContent();
                String name = s.getName();
                if (phone.replace(" ", "").contains(inputContent) || name.contains(inputContent)) {
                    filterDateList.add(s);
                }
            }
        }
        handlerContactDataResult(filterDateList);
    }

    /**
     * desc:处理得到的数据结果
     * @param list
     */
    private void handlerContactDataResult(ArrayList<SuggestionChatInfoBean> list) {

        if (getView() != null && list != null && list.size() != 0){
            shareToContactAdapter = new ShareToContactAdapter(getView().getContext(),list,null);
            getView().initContactReclyView(shareToContactAdapter);
            shareToContactAdapter.setOnShareLisenter(this);
        }else {
            getView().showNoContactNullView();
        }
    }

    @NonNull
    public ArrayList<SuggestionChatInfoBean> getAllContactList() {
        ArrayList<SuggestionChatInfoBean> list = new ArrayList<SuggestionChatInfoBean>();
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
                SuggestionChatInfoBean contact = new SuggestionChatInfoBean("", 1, "");
                String contact_phone = cursor
                        .getString(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String name = cursor.getString(0);
                contact.setContent(contact_phone);
                contact.setName(name);
                if (name != null)
                    list.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }


    /**
     * desc：点击分享按钮
     * @param item
     */
    @Override
    public void isShare(SuggestionChatInfoBean item) {
        Subscription isRegisterSub = Observable.just(item)
                .map(new Func1<SuggestionChatInfoBean, Boolean>() {
                    @Override
                    public Boolean call(SuggestionChatInfoBean bean) {
                        // TODO SDK　检测是否已经注册
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        handlerCheckRegister(aBoolean);
                    }
                });
    }

    /**
     * desc:处理检测注册的结果
     * @param aBoolean
     */
    private void handlerCheckRegister(Boolean aBoolean) {
        if (aBoolean){

        }else {

        }
    }
}
