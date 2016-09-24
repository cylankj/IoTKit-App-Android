package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.text.TextUtils;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineSetRemarkNameContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/23
 * 描述：
 */
public class MineSetRemarkNamePresenterImp extends AbstractPresenter<MineSetRemarkNameContract.View> implements MineSetRemarkNameContract.Presenter {

    public MineSetRemarkNamePresenterImp(MineSetRemarkNameContract.View view) {
        super(view);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isEditEmpty(String string) {
        return TextUtils.isEmpty(string) ? true : false;
    }
}
