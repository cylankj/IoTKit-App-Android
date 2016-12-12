package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineSetRemarkNameContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.tencent.open.utils.HttpUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/23
 * 描述：
 */
public class MineSetRemarkNamePresenterImp extends AbstractPresenter<MineSetRemarkNameContract.View> implements MineSetRemarkNameContract.Presenter {

    public MineSetRemarkNamePresenterImp(MineSetRemarkNameContract.View view) {
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
    public boolean isEditEmpty(String string) {
        return TextUtils.isEmpty(string) ? true : false;
    }

    /**
     * 发送修改备注名的请求
     * @param friendBean
     */
    @Override
    public void sendSetmarkNameReq(final String newName, final RelAndFriendBean friendBean) {
        rx.Observable.just(friendBean)
              .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<RelAndFriendBean>() {
                    @Override
                    public void call(RelAndFriendBean bean) {
                        try {
                            JfgCmdInsurance.getCmd().setFriendMarkName(friendBean.account, newName);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendSetmarkNameReq: " + throwable.getLocalizedMessage());
                    }
                });
    }
}
