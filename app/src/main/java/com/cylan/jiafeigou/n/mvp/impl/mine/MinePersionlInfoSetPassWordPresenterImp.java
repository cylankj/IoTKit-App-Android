package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import com.cylan.jiafeigou.n.mvp.contract.mine.MinePersionlInfoSetPassWordContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/20
 * 描述：
 */
public class MinePersionlInfoSetPassWordPresenterImp extends AbstractPresenter<MinePersionlInfoSetPassWordContract.View> implements MinePersionlInfoSetPassWordContract.Presenter{

    public MinePersionlInfoSetPassWordPresenterImp(MinePersionlInfoSetPassWordContract.View view) {
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
    public boolean checkOldPassword(String inputPass) {
        String oldPass = "123456";
        return inputPass.equals(oldPass);
    }

    @Override
    public boolean checkNewPassword(String oldPass, String newPass) {
        return oldPass.equals(newPass);
    }

    @Override
    public boolean checkNewPasswordLength(String newPass) {
        return (newPass.length()<6 || newPass.length() > 12);
    }

}
