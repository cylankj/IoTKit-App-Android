package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.text.TextUtils;

import com.cylan.jiafeigou.n.mvp.contract.mine.MinePersionalInfomationSetNameContract;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public class MinePersionalInfomationSetNamePresenterImpl implements MinePersionalInfomationSetNameContract.Presenter {

    @Override
    public void saveName() {

    }

    @Override
    public boolean isEditEmpty(String string) {

        return TextUtils.isEmpty(string) ? true:false;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
