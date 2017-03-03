package com.cylan.jiafeigou.cache.db.view;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public interface IDPTask<T extends IDPTaskResult> {

    Observable<T> performLocal();

    Observable<T> performServer();
}
