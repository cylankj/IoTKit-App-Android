package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToContactContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.rx.RxBus;

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
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToContactPresenterImp extends AbstractPresenter<MineShareToContactContract.View>
        implements MineShareToContactContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private ArrayList<RelAndFriendBean> filterDateList;
    private ArrayList<RelAndFriendBean> allCoverData = new ArrayList<>();
    private ArrayList<RelAndFriendBean> hasShareFriend;

    public MineShareToContactPresenterImp(MineShareToContactContract.View view,ArrayList<RelAndFriendBean> hasShareFiend) {
        super(view);
        view.setPresenter(this);
        this.hasShareFriend = hasShareFiend;
    }

    @Override
    public void start() {
        if (hasShareFriend != null && hasShareFriend.size() != 0){
            ArrayList<RelAndFriendBean> list = converData2(hasShareFriend);
            allCoverData.addAll(list);
            handlerContactDataResult(list);
        }else {
            ArrayList<RelAndFriendBean> list = getAllContactList();
            allCoverData.addAll(list);
            handlerContactDataResult(list);
        }

        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }else {
            compositeSubscription = new CompositeSubscription();
//            compositeSubscription.add(getHasShareContractCallBack());
            compositeSubscription.add(shareDeviceCallBack());
        }
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
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
                        JfgCmdInsurance.getCmd().shareDevice(cid,account);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("handlerShareClick"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到已经分享的亲友数
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
                        JfgCmdInsurance.getCmd().getUnShareListByCid(cid);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getHasShareContract"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到已经分享好友的回调
     * @return
     */
    @Override
    public Subscription getHasShareContractCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetHasShareFriendCallBack.class)
                .flatMap(new Func1<RxEvent.GetHasShareFriendCallBack, Observable<ArrayList<RelAndFriendBean>>>() {
                    @Override
                    public Observable<ArrayList<RelAndFriendBean>> call(RxEvent.GetHasShareFriendCallBack getHasShareFriendCallBack) {
                        if (getHasShareFriendCallBack != null && getHasShareFriendCallBack instanceof RxEvent.GetHasShareFriendCallBack){

                            if (getHasShareFriendCallBack.arrayList.size() != 0){
                                return Observable.just(converData(getHasShareFriendCallBack.arrayList));
                            }else {
                                return Observable.just(getAllContactList());
                            }
                        }else {
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
                });
    }

    /**
     * 分享设备的回调
     * @return
     */
    @Override
    public Subscription shareDeviceCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ShareDeviceCallBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ShareDeviceCallBack>() {
                    @Override
                    public void call(RxEvent.ShareDeviceCallBack shareDeviceCallBack) {
                        if (shareDeviceCallBack != null && shareDeviceCallBack instanceof RxEvent.ShareDeviceCallBack){
                            if (getView() != null){
                                getView().hideShareingProHint();
                                getView().handlerCheckRegister(shareDeviceCallBack.requestId,shareDeviceCallBack.account);
                            }
                        }
                    }
                });
    }

    /**
     * 数据的转换 标记已分享和未分享
     * @param arrayList
     * @return
     */
    private ArrayList<RelAndFriendBean> converData(ArrayList<JFGFriendAccount> arrayList) {
        ArrayList<RelAndFriendBean> list = new ArrayList<>();
        for (RelAndFriendBean contract:getAllContactList()){
            for (JFGFriendAccount friend:arrayList){
                if (friend.account.equals(contract.account)){
                    contract.isCheckFlag = 1;
                }else {
                    contract.isCheckFlag = 0;
                }
            }
            list.add(contract);
        }
        return list;
    }


    /**
     * 数据的转换 标记已分享和未分享
     * @param arrayList
     * @return
     */
    private ArrayList<RelAndFriendBean> converData2(ArrayList<RelAndFriendBean> arrayList) {
        ArrayList<RelAndFriendBean> list = new ArrayList<>();
        for (RelAndFriendBean contract:getAllContactList()){
            for (RelAndFriendBean friend:arrayList){
                if (friend.account.equals(contract.account)){
                    contract.isCheckFlag = 1;
                }else {
                    contract.isCheckFlag = 0;
                }
            }
            list.add(contract);
        }
        return list;
    }


    /**
     * desc:处理得到的数据结果
     * @param list
     */
    private void handlerContactDataResult(ArrayList<RelAndFriendBean> list) {
        if (getView() != null && list != null && list.size() != 0){
            getView().initContactReclyView(list);
        }else {
            getView().showNoContactNullView();
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
                RelAndFriendBean friendBean = new RelAndFriendBean();
                String contact_phone = cursor
                        .getString(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String name = cursor.getString(0);
                friendBean.account = contact_phone;
                friendBean.alias = name;
                if (name != null){
                    if (friendBean.account.startsWith("+86")){
                        friendBean.account = friendBean.account.substring(3);
                    }else if (friendBean.account.startsWith("86")){
                        friendBean.account = friendBean.account.substring(2);
                    }

                    if (JConstant.PHONE_REG.matcher(friendBean.account).find()){
                        list.add(friendBean);
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

}
