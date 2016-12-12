package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetPassWordContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/20
 * 描述：
 */
public class MineInfoSetPassWordPresenterImp extends AbstractPresenter<MineInfoSetPassWordContract.View> implements MineInfoSetPassWordContract.Presenter {

    public MineInfoSetPassWordPresenterImp(MineInfoSetPassWordContract.View view) {
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

    /**
     * 发送修改密码请求
     * @param account
     */
    @Override
    public void sendChangePassReq(final String account, final String oldPass, final String newPass) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            JfgCmdInsurance.getCmd().resetPassword(account,oldPass,newPass);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendChangePassReq"+throwable.getLocalizedMessage());
                    }
                });
    }

}
