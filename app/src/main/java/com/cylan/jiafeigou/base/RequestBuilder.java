package com.cylan.jiafeigou.base;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by yanzhendong on 2017/6/10.
 */

public interface RequestBuilder<T> {

    Observable<T> process();

    RequestAdapter adapter();

    RequestAdapter adapter(Func1 function);

    void setAdapter(RequestAdapter adapter);
}
