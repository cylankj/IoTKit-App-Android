package com.cylan.jiafeigou.n.view.panorama;

import android.support.annotation.IntDef;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigureContact.View.LOGO_TYPE.LOGO_TYPE_BLACK;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigureContact.View.LOGO_TYPE.LOGO_TYPE_CLOVE_DOG;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigureContact.View.LOGO_TYPE.LOGO_TYPE_NONE;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigureContact.View.LOGO_TYPE.LOGO_TYPE_WHITE;

/**
 * Created by yanzhendong on 2017/3/15.
 */

public interface PanoramaLogoConfigureContact {

    interface View extends JFGView {


        @IntDef({LOGO_TYPE_NONE, LOGO_TYPE_WHITE, LOGO_TYPE_BLACK, LOGO_TYPE_CLOVE_DOG})
        @Retention(RetentionPolicy.SOURCE)
        @interface LOGO_TYPE {

            int LOGO_TYPE_NONE = 1;
            int LOGO_TYPE_WHITE = 2;
            int LOGO_TYPE_BLACK = 3;
            int LOGO_TYPE_CLOVE_DOG = 4;
        }

        class LogoItem {

            @LOGO_TYPE
            public int type = 0;//0:无水印,1:white,2:black,3:cloveDog,-1:自定义
            public String resPath = null;

            public LogoItem(@LOGO_TYPE int type) {
                this.type = type;
            }

        }

        void onHttpConnectionToDeviceError();

        void onChangeLogoTypeSuccess(@LOGO_TYPE int logtype);

        void onChangeLogoTypeError(int position);
    }

    interface Presenter extends JFGPresenter<View> {

        void changeLogoType(int position);
    }
}
