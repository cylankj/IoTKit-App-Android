package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGResult;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.module.FriendsReqBean;
import com.cylan.jiafeigou.cache.db.module.FriendsReqBeanDao;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.task.FetchFriendsTask;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.NetUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendsPresenterImp extends AbstractPresenter<MineFriendsContract.View> implements MineFriendsContract.Presenter {

    public MineFriendsPresenterImp(MineFriendsContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        fetchFriends();
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                fetchFriendsRspSub()};
    }

    private Subscription fetchFriendsRspSub() {
        return Observable.just("go")
                .subscribeOn(Schedulers.newThread())
                .flatMap(s -> RxBus.getCacheInstance().toObservableSticky(RxEvent.AllFriendsRsp.class))
                .flatMap(ret -> Observable.just(BaseApplication.getAppComponent().getSourceManager().getFriendsList()))
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> ret != null && mView != null && mView.isAdded())
                .subscribe(ret -> {
                    mView.initAddReqReqList(sortAddReqList(BaseApplication.getAppComponent().getSourceManager().getFriendsReqList()));
                    mView.initFriendList(BaseApplication.getAppComponent().getSourceManager().getFriendsList());
                }, AppLogger::e);
    }

    private void fetchFriends() {
        Observable.just(new FetchFriendsTask())
                .subscribeOn(Schedulers.newThread())
                .subscribe(objectAction1 -> objectAction1.call(""), AppLogger::e);
    }

    @Override
    public void stop() {
        super.stop();
    }


    @Override
    public boolean checkAddRequestOutTime(FriendsReqBean bean) {
        long oneMonth = 30 * 24 * 60 * 60 * 1000L;
        long current = System.currentTimeMillis();
        //怀疑 bean.time是秒
        boolean isLongTime = String.valueOf(bean.time).length() == String.valueOf(current).length();
        return (current - (isLongTime ? bean.time : bean.time * 1000L)) > oneMonth;
    }

    /**
     * desc：添加请求集合的排序
     *
     * @param list
     * @return
     */
    public ArrayList<FriendsReqBean> sortAddReqList(ArrayList<FriendsReqBean> list) {
        Comparator<FriendsReqBean> comparator = (lhs, rhs) -> {
            long oldTime = Long.parseLong(rhs.time + "");
            long newTime = Long.parseLong(lhs.time + "");
            return (int) (newTime - oldTime);
        };
        Collections.sort(list, comparator);
        return list;
    }

    /**
     * 发送添加请求
     */
    @Override
    public void sendAddReq(final String account) {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().addFriend(account, "");
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, throwable -> AppLogger.e("sendAddReq: " + throwable.getLocalizedMessage()));
    }

    /**
     * 同意添加后SDK的调用
     */
    @Override
    public void acceptAddSDK(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .delay(1, TimeUnit.SECONDS)
                .subscribe(new ConsentAccountTask(MineFriendsPresenterImp.this, mView), AppLogger::e);
    }

    /**
     * 删除选项
     */
    private static class DeleteReqTask implements Action1<String> {

        private WeakReference<MineFriendsContract.Presenter> weakReference;
        private WeakReference<MineFriendsContract.View> viewWeakReference;

        public DeleteReqTask(MineFriendsContract.Presenter contract, MineFriendsContract.View view) {
            this.weakReference = new WeakReference<>(contract);
            this.viewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void call(String account) {
            Observable.just(account)
                    .subscribeOn(Schedulers.newThread())
                    .flatMap(o -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteAddReqBack.class))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {
                        JFGResult result = ret.jfgResult;
                        if (result.code == JError.ErrorOK) {
                            updateReqList(account);
                        }
                        AppLogger.d("需要更新缓存");
                        if (viewWeakReference.get() != null)
                            viewWeakReference.get().deleteItemRsp(account, result.code);
                        BaseDBHelper dbHelper = (BaseDBHelper) BaseApplication.getAppComponent().getDBHelper();
                        FriendsReqBeanDao dao = dbHelper.getDaoSession().getFriendsReqBeanDao();
                        List<FriendsReqBean> list1 = dao.queryBuilder().where(FriendsReqBeanDao.Properties.Account.eq(account)).list();
                        dao.deleteInTx(list1);
                        throw new RxEvent.HelperBreaker("结束了");
                    }, AppLogger::e);
            try {
                BaseApplication.getAppComponent().getCmd().delAddFriendMsg(account);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }

    private static void updateReqList(final String account) {
        ArrayList<FriendsReqBean> list = BaseApplication.getAppComponent().getSourceManager().getFriendsReqList();
        if (list != null) {
            for (FriendsReqBean bean : list) {
                if (bean != null && TextUtils.equals(bean.account, account)) {
                    list.remove(bean);
                    break;
                }
            }
        }
        AppLogger.d("重新刷新列表,走一遍流程,就不需要特殊处理");
        Observable.just(new FetchFriendsTask())
                .subscribeOn(Schedulers.newThread())
                .subscribe(objectAction1 -> objectAction1.call(""), AppLogger::e);
    }

    /**
     * 发送同意
     */
    private static class ConsentAccountTask implements Action1<String> {
        private WeakReference<MineFriendsContract.Presenter> weakReference;
        private WeakReference<MineFriendsContract.View> viewWeakReference;

        public ConsentAccountTask(MineFriendsContract.Presenter contract, MineFriendsContract.View view) {
            this.weakReference = new WeakReference<>(contract);
            this.viewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void call(String s) {
            Observable.just(s)
                    .subscribeOn(Schedulers.newThread())
                    .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.ConsentAddFriendBack.class)
                            .timeout(30, TimeUnit.SECONDS))
                    .subscribe(ret -> {
                        JFGResult result = ret.jfgResult;
                        if (result.code == JError.ErrorOK) {
                            updateReqList(s);
                            AppLogger.d("刷新列表");
                        }
                        if (viewWeakReference.get() != null && viewWeakReference.get().isAdded()) {
                            viewWeakReference.get().consentRsp(s, result.code);
                        }
                        throw new RxEvent.HelperBreaker("结束了");
                    }, throwable -> {
                        if (throwable instanceof TimeoutException && viewWeakReference.get() != null && viewWeakReference.get().isAdded()) {
                            viewWeakReference.get().consentRsp(s, -1);
                        }
                    });
            try {
                BaseApplication.getAppComponent().getCmd().consentAddFriend(s);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{
                ConnectivityManager.CONNECTIVITY_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION
        };
    }

    /**
     * 删除好友添加请求
     *
     * @param account
     */
    @Override
    public void deleteAddReq(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new DeleteReqTask(MineFriendsPresenterImp.this, mView), AppLogger::e);
    }

    @Override
    public void removeCache(String account) {
//        Pair<ArrayList<JFGFriendAccount>, ArrayList<JFGFriendRequest>> pair = BaseApplication.getAppComponent().getSourceManager().getFriendsList();
//        if (pair != null && pair.second != null) {
//            for (JFGFriendRequest request : pair.second) {
//                if (request != null && TextUtils.equals(account, request.account)) {
//                    pair.second.remove(request);
//                    break;
//                }
//            }
//        }
//        TreeHelper helper = BaseApplication.getAppComponent().getTreeHelper();
//        TreeNode node = helper.findTreeNodeByName(MineFriendsFragment.class.getSimpleName());
//        node.setCacheData(new CacheObject().setCount(pair == null || pair.second == null ? 0 : ListUtils.getSize(pair.second))
//                .setObject(pair == null || pair.second == null ? 0 : pair.second));
    }


    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        if (mView == null || !mView.isAdded()) return;
        Observable.just(NetUtils.getJfgNetType())
                .filter(integer -> getView() != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> getView().onNetStateChanged(integer), AppLogger::e);
    }


}
