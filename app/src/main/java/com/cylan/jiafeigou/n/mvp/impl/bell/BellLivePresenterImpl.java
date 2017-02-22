package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.graphics.Bitmap;

import com.cylan.jfgapp.interfases.CallBack;
import com.cylan.jiafeigou.base.wrapper.BaseCallablePresenter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.io.File;

/**
 * Created by cylan-hunt on 16-8-10.
 */
public class BellLivePresenterImpl extends BaseCallablePresenter<BellLiveContract.View> implements
        BellLiveContract.Presenter {

    public BellLivePresenterImpl() {
    }


    @Override
    public void capture() {
        JfgCmdInsurance.getCmd().screenshot(false, new CallBack<Bitmap>() {
            @Override
            public void onSucceed(Bitmap bitmap) {
                ToastUtil.showPositiveToast("截图成功!");
                String filePath = JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + ".png";
                BitmapUtils.saveBitmap2file(bitmap, filePath);
            }

            @Override
            public void onFailure(String s) {
                ToastUtil.showNegativeToast("截图失败!");
            }
        });
    }
}
