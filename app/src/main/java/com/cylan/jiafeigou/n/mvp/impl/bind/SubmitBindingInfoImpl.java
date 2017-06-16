package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.jiafeigou.misc.bind.SubmitListener;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public class SubmitBindingInfoImpl extends AbstractPresenter<SubmitBindingInfoContract.View>
        implements SubmitBindingInfoContract.Presenter, SubmitListener {


    public SubmitBindingInfoImpl(SubmitBindingInfoContract.View view, String uuid) {
        super(view);
    }


    @Override
    public void start() {
        super.start();
    }


    @Override
    public void onSubmitStart() {

    }

    @Override
    public void onSubmitErr(int errCode) {

    }

    @Override
    public void onSubmitProgress(int progress) {

    }

    @Override
    public void onSubmitSuccess() {

    }

    @Override
    public void startCounting() {

    }

    @Override
    public void endCounting() {

    }

    @Override
    public void clean() {

    }
}
