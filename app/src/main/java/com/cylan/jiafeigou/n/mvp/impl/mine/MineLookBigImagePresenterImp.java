package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineLookBigImageContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineLookBigImagePresenterImp extends AbstractPresenter<MineLookBigImageContract.View> implements MineLookBigImageContract.Presenter {

    private Subscription saveSub;

    public MineLookBigImagePresenterImp(MineLookBigImageContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    /**
     * 保存图片
     */
    @Override
    public void saveImage(Bitmap bmp) {
        // 首先保存图片
        saveSub = Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(o -> {
                    File appDir = new File(Environment.getExternalStorageDirectory(), "jfg");
                    if (!appDir.exists()) {
                        appDir.mkdir();
                    }
                    String fileName = System.currentTimeMillis() + ".jpg";
                    File file = new File(appDir, fileName);
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // 其次把文件插入到系统图库
                    try {
                        MediaStore.Images.Media.insertImage(getView().getContext().getContentResolver(),
                                file.getAbsolutePath(), fileName, null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o1 -> {
                    // 最后通知图库更新
                    getView().getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                });

    }

    @Override
    public void stop() {
        super.stop();
        if (saveSub != null && !saveSub.isUnsubscribed()) {
            saveSub.unsubscribe();
        }
    }
}
