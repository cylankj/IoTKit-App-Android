package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToContactContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
        implements MineShareToContactContract.Presenter {

    private Subscription shareToContactSub;
    private ArrayList<SuggestionChatInfoBean> filterDateList;
    private Subscription shareToThisContact;
    private Subscription isRegisterSub;

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

        if (shareToThisContact != null && shareToThisContact.isUnsubscribed()){
            shareToThisContact.unsubscribe();
        }

        if(isRegisterSub != null && isRegisterSub.isUnsubscribed()){
            isRegisterSub.unsubscribe();
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
     * desc:分享设备给该联系人
     * @param bean
     */
    @Override
    public void shareToContact(SuggestionChatInfoBean bean) {
        if (getView() != null){
            getView().showShareingProHint();
        }
        shareToThisContact = Observable.just(bean)
                .map(new Func1<SuggestionChatInfoBean, Boolean>() {
                    @Override
                    public Boolean call(SuggestionChatInfoBean bean) {
                        //TODO 调用SDK分享该设备给该联系人
                        return true;
                    }
                })
                .delay(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        getView().hideShareingProHint();
                        if (aBoolean){
                            ToastUtil.showToast("分享成功");
                        }else {
                            ToastUtil.showToast("分享失败");
                        }
                    }
                });
    }

    @Override
    public void handlerShareClick(final SuggestionChatInfoBean item) {
        // TODO SDK　检测是否已经注册及设置已经分享的人数是否超过5人
        if (getView() != null){
            getView().showShareingProHint();
            getView().changeShareingProHint("loading");
                    }
        isRegisterSub = Observable.just(item)
                .map(new Func1<SuggestionChatInfoBean, Integer>() {
                    @Override
                    public Integer call(SuggestionChatInfoBean bean) {
                        // TODO SDK　检测是否已经注册 1.已注册未分享  2.已注册未分享人数到达5人  3.已注册已分享  4.未注册
                        return 4;
                    }
                })
                .delay(2000,TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer state) {
                        getView().hideShareingProHint();
                        handlerCheckRegister(state,item);
                    }
                });
    }

    /**
     * desc:处理得到的数据结果
     * @param list
     */
    private void handlerContactDataResult(ArrayList<SuggestionChatInfoBean> list) {

        if (getView() != null && list != null && list.size() != 0){
            getView().initContactReclyView(list);
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
     * desc:处理检测注册的结果
     * @param state
     */
    private void handlerCheckRegister(int state,SuggestionChatInfoBean item) {
        switch (state){
            case 1:                                     //已注册 未分享人数未达到5人
                if (getView() != null){
                    getView().showShareDeviceDialog(item);
                    }
                break;

            case 2:                                     //已注册 未分享但人数达到5人
                if (getView() != null){
                    getView().showPersonOverDialog("只能分享给5位用户");
                    }
                break;

            case 3:                                    //已注册 已分享
                if (getView() != null){
                    getView().showPersonOverDialog("已经分享给此账号啦");
    }
                break;
            case 4:                                    //未注册
                jump2SendMesg(item);
                break;
        }
    }

    /**
     * 发送邀请短信
     * @param item
     */
    private void jump2SendMesg(SuggestionChatInfoBean item) {
        if (getView() != null){
            getView().startSendMesgActivity(item);
        }
    }
}
