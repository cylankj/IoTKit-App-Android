package com.cylan.jiafeigou.n.mvp.impl.mine;


import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativeAndFriendAddReqDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineRelativeAndFriendAddReqDetailPresenterImp extends AbstractPresenter<MineRelativeAndFriendAddReqDetailContract.View> implements MineRelativeAndFriendAddReqDetailContract.Presenter {

    public MineRelativeAndFriendAddReqDetailPresenterImp(MineRelativeAndFriendAddReqDetailContract.View view) {
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
