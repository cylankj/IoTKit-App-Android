package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.app.AlertDialog;
import android.content.Context;

import com.cylan.jiafeigou.n.mvp.contract.mine.MinePersionalInformationContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.UserInfoBean;

import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.ImageLoader;
import cn.finalteam.galleryfinal.ThemeConfig;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public class MinePersionalInformationPresenterImpl extends AbstractPresenter<MinePersionalInformationContract.View> implements MinePersionalInformationContract.Presenter {

    private Context context;
    public static FunctionConfig functionConfig;
    private Subscription getUserInfoSubscription;

    public MinePersionalInformationPresenterImpl(MinePersionalInformationContract.View view, Context context) {
        super(view);
        view.setPresenter(this);
        this.context = context;
    }

    @Override
    public void setPersonName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
    public void initGrallery() {
        //设置主题
        ThemeConfig theme = new ThemeConfig.Builder()
                .build();
        //配置功能
        functionConfig = new FunctionConfig.Builder()
                .setEnableCamera(true)
                .setEnableEdit(true)
                .setEnableCrop(true)
                .setEnableRotate(true)
                .setCropSquare(true)
                .setEnablePreview(true)
                .build();

        //配置imageloader
        ImageLoader imageloader = new GlideImageLoaderPresenterImpl();
        CoreConfig coreConfig = new CoreConfig.Builder(context, imageloader, theme)
                .setFunctionConfig(functionConfig)
                .build();
        GalleryFinal.init(coreConfig);
    }

    @Override
    public void getUserInfomation(String url) {
        getUserInfoSubscription = Observable.just(url)
                .map(new Func1<String, UserInfoBean>() {
                    @Override
                    public UserInfoBean call(String s) {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UserInfoBean>() {
                    @Override
                    public void call(UserInfoBean userInfoBean) {
                        getView().initPersionalInfomation(userInfoBean);
                    }
                });
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (getUserInfoSubscription != null) {
            getUserInfoSubscription.unsubscribe();
        }
    }
}
