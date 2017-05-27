package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/**
 * Created by yanzhendong on 2017/5/27.
 */

public interface PanoramaShareContact {

    interface View extends JFGView {

        void onUploadResult(int code);//-1 上传失败;200 上传成功

        void onShareH5UrlResponse(String h5);
    }

    interface Presenter extends JFGPresenter<View> {

        void upload(String fileName, String filePath);

        void share();
    }
}
