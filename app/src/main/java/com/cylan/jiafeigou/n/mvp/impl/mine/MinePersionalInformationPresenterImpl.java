package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MinePersionalInformationContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

import java.util.List;

import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.ImageLoader;
import cn.finalteam.galleryfinal.ThemeConfig;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public class MinePersionalInformationPresenterImpl extends AbstractPresenter<MinePersionalInformationContract.View> implements MinePersionalInformationContract.Presenter {

    private Context context;
    public static FunctionConfig functionConfig;

    public MinePersionalInformationPresenterImpl(MinePersionalInformationContract.View view,Context context) {
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
    public void start() {

    }

    @Override
    public void stop() {

    }
}
