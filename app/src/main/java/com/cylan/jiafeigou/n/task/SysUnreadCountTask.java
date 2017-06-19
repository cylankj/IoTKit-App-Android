package com.cylan.jiafeigou.n.task;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.home.SystemMessageFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.CacheObject;
import com.cylan.jiafeigou.support.badge.TreeHelper;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 系统消息读数
 * Created by hds on 17-6-7.
 */

public class SysUnreadCountTask implements Action1<Object> {
    @Override
    public void call(Object o) {
        getSystemUnreadCount()
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class).first(rsp->rsp.seq==ret)
                        .filter(result -> result.map != null)
                        .observeOn(Schedulers.newThread())
                        .flatMap(rsp -> {
                            int count = -1;
                            if (rsp != null && rsp.map != null && rsp.map.size() != 0) {
                                for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : rsp.map.entrySet()) {
                                    try {
                                        if (entry.getKey() == 1101 || entry.getKey() == 1103 || entry.getKey() == 1104) {
                                            count = 0;
                                            ArrayList<JFGDPMsg> value = entry.getValue();
                                            if (value.size() != 0) {
                                                JFGDPMsg jfgdpMsg = value.get(0);
                                                Integer unReadCount = DpUtils.unpackData(jfgdpMsg.packValue, Integer.class);
                                                if (unReadCount == null) unReadCount = 0;
                                                AppLogger.d("unReadCount:" + unReadCount);
                                                count += unReadCount;
                                            }
                                        }
                                    } catch (Exception e) {
                                        AppLogger.e("getUnreadBack:" + e.getLocalizedMessage());
                                    }
                                }
                            }
                            return Observable.just(count);
                        }))
                .subscribeOn(Schedulers.newThread())
                .subscribe(integer -> {
                    TreeHelper helper = BaseApplication.getAppComponent().getTreeHelper();
                    TreeNode node = helper.findTreeNodeByName(SystemMessageFragment.class.getSimpleName());
                    node.setCacheData(new CacheObject().setCount(integer).setObject(integer));
                    RxBus.getCacheInstance().postSticky(new RxEvent.InfoUpdate());
                }, AppLogger::e);
    }

    private Observable<Long> getSystemUnreadCount() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> list = new ArrayList<>();
                JFGDPMsg msg1 = new JFGDPMsg(1101L, System.currentTimeMillis());
                JFGDPMsg msg2 = new JFGDPMsg(1103L, System.currentTimeMillis());
                JFGDPMsg msg3 = new JFGDPMsg(1104L, System.currentTimeMillis());
                list.add(msg1);
                list.add(msg2);
                list.add(msg3);
                long seq = BaseApplication.getAppComponent().getCmd().robotGetData("", list, 10, false, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
                AppLogger.d("fetchNewInfo:");
            } catch (JfgException e) {
                AppLogger.e("fetchNewInfo" + e.getLocalizedMessage());
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

}
