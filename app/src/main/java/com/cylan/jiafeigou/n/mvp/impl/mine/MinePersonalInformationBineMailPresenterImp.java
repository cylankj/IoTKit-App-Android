package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.contract.mine.MinePersonalInformationBindMailContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.widget.sticky.AnimatorBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/12
 * 描述：
 */
public class MinePersonalInformationBineMailPresenterImp extends AbstractPresenter<MinePersonalInformationBindMailContract.View> implements MinePersonalInformationBindMailContract.Presenter {

    public MinePersonalInformationBineMailPresenterImp(MinePersonalInformationBindMailContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public boolean checkEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    @Override
    public boolean checkEmailIsBinded(String email) {
        //TODO  查询数据库
        return false;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
