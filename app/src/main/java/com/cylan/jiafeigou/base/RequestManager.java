package com.cylan.jiafeigou.base;


import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.n.base.BaseApplication;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/6/10.
 */

public class RequestManager {

    public static <T> Observable<List<T>> get(int msgId, Class<T> elementType) {
        IDPTaskDispatcher dispatcher = BaseApplication.getAppComponent().getTaskDispatcher();
        DPEntity entity = new DPEntity("", msgId, 0, DBAction.QUERY, DBOption.SingleQueryOption.DESC_20_LIMIT);
        return dispatcher.perform(entity).map(result -> {
            List<T> ret;
            try {
                ret = result.getResultResponse();
            } catch (Exception e) {
                ret = new ArrayList<>();
            }
            if (ret == null) {
                ret = new ArrayList<>();
            }
            return ret;
        });
    }

    public static void upload(String fileName, String filePath) {
//        Observable.just(getRemoteFilePath(fileName))
//                .observeOn(Schedulers.io())
//                .map(remote -> {
//                    int result = -1;
//                    try {
//                        result = BaseApplication.getAppComponent().getCmd().putFileToCloud(remote, filePath);
//                    } catch (JfgException e) {
//                        e.printStackTrace();
//                    }
//                    AppLogger.d("上传返回码为:" + result);
//                    return result;
//                })
//                .flatMap(seq -> RxBus.getCacheInstance().toObservable(JFGMsgHttpResult.class)
//                        .filter(ret -> ret.requestId == seq)
//                        .first())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(result -> {
//                    AppLogger.e("上传的结果为:" + new Gson().toJson(result));
////                    mView.onUploadResult(result.ret);
//                }, e -> {
//                    AppLogger.e(e.getMessage());
//                });

    }

    public static String getRemoteFilePath(String uuid, String fileName) {
        return null;
    }


}
