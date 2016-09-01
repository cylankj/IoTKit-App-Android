package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.app.AlertDialog;
import android.content.Context;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MinePersionalInformationContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public class MinePersionalInformationPresenterImpl extends AbstractPresenter<MinePersionalInformationContract.View> implements MinePersionalInformationContract.Presenter {

    private Context context;

    public MinePersionalInformationPresenterImpl(MinePersionalInformationContract.View view,Context context) {
        super(view);
        view.setPresenter(this);
        this.context = context;
    }

    @Override
    public void pickPersonImageHead() {

    }

    @Override
    public void setPersonName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(R.layout.minepersoninfomation_setname);

        builder.show();
    }

    @Override
    public void bindPersonEmail() {
        getView().jump2SetEmailFragment();
    }

    @Override
    public void bindPersonPhone() {

    }

    @Override
    public void changePassword() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
