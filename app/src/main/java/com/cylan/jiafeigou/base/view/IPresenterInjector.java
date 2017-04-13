package com.cylan.jiafeigou.base.view;

import com.cylan.jiafeigou.base.wrapper.BasePresenter;

/**
 * Created by yanzhendong on 2017/4/13.
 */

public interface IPresenterInjector {

    <T extends BasePresenter> T inject(T presenter);
}
