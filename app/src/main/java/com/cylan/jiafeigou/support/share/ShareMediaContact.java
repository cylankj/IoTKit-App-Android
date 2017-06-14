package com.cylan.jiafeigou.support.share;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/**
 * Created by yanzhendong on 2017/6/1.
 */

public interface ShareMediaContact {

    interface View extends JFGView {

    }

    interface Presenter extends JFGPresenter<View> {
    }

    interface ShareOptionItemView extends JFGView {

    }

    interface ShareOptionItemPresenter extends JFGPresenter<ShareOptionItemView> {

    }
}
