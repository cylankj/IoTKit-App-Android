package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.n.view.adapter.item.ShareContentItem;

import java.util.List;

/**
 * Created by yanzhendong on 2017/5/26.
 */

public interface MineShareContentContract {
    interface View extends JFGView {

        void onShareContentResponse(List<ShareContentItem> shareContentItems, boolean refresh);

        void onUnShareContentResponse(int resultCode, Iterable<Integer> selection);
    }

    interface Presenter extends JFGPresenter<View> {

        void loadFromServer(long version, boolean refresh);

        void unShareContent(List<ShareContentItem> items, Iterable<Integer> selection);
    }
}
