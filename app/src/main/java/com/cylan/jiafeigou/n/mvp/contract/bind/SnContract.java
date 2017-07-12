package com.cylan.jiafeigou.n.mvp.contract.bind;

import com.cylan.jiafeigou.n.mvp.BaseFragmentView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;

/**
 * Created by hds on 17-7-12.
 */

public interface SnContract {

    interface View extends BaseFragmentView<Presenter> {
        void getPidRsp(int err, int pid);
    }

    interface Presenter extends BasePresenter {
        void getPid(String sn);
    }
}
