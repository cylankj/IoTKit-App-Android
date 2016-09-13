package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToRelativeAndFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToRelativeAndFriendPresenterImp extends AbstractPresenter<MineShareToRelativeAndFriendContract.View>
        implements MineShareToRelativeAndFriendContract.Presenter {

    public MineShareToRelativeAndFriendPresenterImp(MineShareToRelativeAndFriendContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
