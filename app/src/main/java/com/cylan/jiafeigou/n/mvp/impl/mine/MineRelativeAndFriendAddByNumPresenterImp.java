package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativeAndFriendAddByNumContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineRelativeAndFriendAddByNumPresenterImp extends AbstractPresenter<MineRelativeAndFriendAddByNumContract.View>
        implements MineRelativeAndFriendAddByNumContract.Presenter  {

    public MineRelativeAndFriendAddByNumPresenterImp(MineRelativeAndFriendAddByNumContract.View view) {
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
