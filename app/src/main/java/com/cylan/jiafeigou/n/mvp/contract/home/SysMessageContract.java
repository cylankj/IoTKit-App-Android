package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.cache.db.module.SysMsgBean;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.rx.RxEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public interface SysMessageContract {

    interface View extends BaseView {

        void onQuerySystemMessageRsp(ArrayList<SysMsgBean> list);

        void onDeleteSystemMessageRsp(RxEvent.DeleteDataRsp deleteDataRspClass);
    }

    interface Presenter extends BasePresenter {

        void loadSystemMessageFromServer(long v601, long v701);

        void deleteSystemMessageFromServer(List<SysMsgBean> sysMsgBeans);

    }

}
