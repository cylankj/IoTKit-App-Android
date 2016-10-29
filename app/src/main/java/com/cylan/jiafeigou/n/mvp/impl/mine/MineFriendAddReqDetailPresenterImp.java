package com.cylan.jiafeigou.n.mvp.impl.mine;


import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddReqDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineFriendAddReqDetailPresenterImp extends AbstractPresenter<MineFriendAddReqDetailContract.View> implements MineFriendAddReqDetailContract.Presenter {

    public MineFriendAddReqDetailPresenterImp(MineFriendAddReqDetailContract.View view) {
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
