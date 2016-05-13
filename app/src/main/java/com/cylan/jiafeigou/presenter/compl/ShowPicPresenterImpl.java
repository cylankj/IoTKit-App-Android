package com.cylan.jiafeigou.presenter.compl;

import android.text.TextUtils;

import com.cylan.jiafeigou.presenter.ShowPicPresenter;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.view.ShowBigPicView;
import com.cylan.support.DswLog;
import com.cylan.utils.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import support.uil.core.ImageLoader;

/**
 * Created by hunt on 16-5-5.
 */
public class ShowPicPresenterImpl implements ShowPicPresenter {

    private ShowBigPicView showBigPicView;
    private String[] err = {"bitmap is null", ""};
    private static final String REG_PIC_NAME = "[1][4]\\d{8}\\.jpg";
    private static final String REG_PIC_NAME_1 = "[1][4]\\d{8}\\_[0-9].jpg";

    public ShowPicPresenterImpl(ShowBigPicView picView) {
        this.showBigPicView = picView;
        picView.initView();
        start();
    }

    @Override
    public void share() {

    }

    @Override
    public void download(final String url) {
        Observable.just(url)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        File bitmap = ImageLoader.getInstance().getDiskCache().get(s);
                        if (bitmap == null)
                            throw new NullPointerException(err[0]);
                        String time = findMatch(s);
                        if (!TextUtils.isEmpty(time) && time.length() > 10) {
                            final String newFileName = convertSimpleName2Name(time);
                            final String targetPath = PathGetter.getJiaFeiGouPhotos() + newFileName;
                            FileUtils.copyFile(bitmap.getAbsolutePath(), targetPath);
                        } else throw new NullPointerException("cant find name");
                        showBigPicView.downloadFinish(1);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showBigPicView.downloadFinish(-1);
                        DswLog.ex(throwable.toString());
                    }
                });
    }


    @Override
    public void start() {

    }

    /**
     * get file name from url
     *
     * @param url
     * @return
     */
    public String findMatch(String url) {
        Pattern p0 = Pattern.compile(REG_PIC_NAME);
        Matcher m = p0.matcher(url);
        if (m.find()) {
            return m.group();
        }
        p0 = Pattern.compile(REG_PIC_NAME_1);
        m = p0.matcher(url);
        if (m.find()) {
            return m.group();
        } else return "";
    }

    /**
     * 文件名有两种形式
     *
     * @param fileName
     * @return
     */
    public String convertSimpleName2Name(String fileName) {
        String timeInLong = fileName.substring(0, 10);
        if (TextUtils.isDigitsOnly(timeInLong)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            fileName = simpleDateFormat.format(new Date(Long.valueOf(timeInLong) * 1000L));
        }
        fileName = fileName.replace(timeInLong, "");
        return fileName;
    }
}
