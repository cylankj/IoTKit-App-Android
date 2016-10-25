package com.cylan.jiafeigou.n.mvp.impl.mag;

import com.cylan.jiafeigou.n.mvp.contract.mag.MagLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/10/20
 * 描述：
 */
public class MagLivePresenterImp extends AbstractPresenter<MagLiveContract.View> implements MagLiveContract.Presenter {

    public MagLivePresenterImp(MagLiveContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean getDoorCurrentState() {
        //TODO 获取到当前门的状态
        return true;
    }
}
