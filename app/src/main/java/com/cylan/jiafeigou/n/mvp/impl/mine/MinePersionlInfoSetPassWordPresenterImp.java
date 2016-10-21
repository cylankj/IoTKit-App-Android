package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.n.mvp.contract.mine.MinePersonalInfoSetPassWordContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * 作者：zsl
 * 创建时间：2016/9/20
 * 描述：
 */
public class MinePersionlInfoSetPassWordPresenterImp extends AbstractPresenter<MinePersonalInfoSetPassWordContract.View> implements MinePersonalInfoSetPassWordContract.Presenter {

    public MinePersionlInfoSetPassWordPresenterImp(MinePersonalInfoSetPassWordContract.View view) {
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
        return (newPass.length() < 6 || newPass.length() > 12);
    }

}
