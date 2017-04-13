package com.cylan.jiafeigou.base.module;

import com.cylan.jiafeigou.base.view.IBasePresenterInjector;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;

/**
 * Created by yanzhendong on 2017/4/13.
 */

public class BasePresenterInjector implements IBasePresenterInjector {
    protected JFGSourceManager sourceManager;
    protected IDPTaskDispatcher taskDispatcher;

    public BasePresenterInjector(JFGSourceManager manager, IDPTaskDispatcher dispatcher) {
        this.sourceManager = manager;
        this.taskDispatcher = dispatcher;
    }

    @Override
    public <T extends BasePresenter> T inject(T presenter) {
        presenter.setSourceManager(sourceManager);
        presenter.setTaskDispatcher(taskDispatcher);
        return presenter;
    }
}
