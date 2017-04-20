package com.cylan.jiafeigou.base.view;

import android.support.annotation.IntDef;
import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yanzhendong on 2017/4/19.
 */

public interface IDialogManager {
    int DIALOG_TYPE_DEFAULT = 0;//基本的弹出框,可以设置标题,内容,确认,取消

    @IntDef({DIALOG_TYPE_DEFAULT})
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    @interface DialogType {

    }


    <T extends IDialogBuilder> T getDialogBuilder(@DialogType int dialogType);


    //Dialog 参数设置的建造者,具体展现成什么样需要对应 type 的 dialog 来解析
    interface IDialogBuilder {

        IDialogBuilder setTitle(String title);


        IDialogBuilder setContent(String content);


        IDialogBuilder setCustomContentView(View contentView);

        IDialogBuilder setCustomContentView(int resId);

        IDialogBuilder setClickListener(int resId, View.OnClickListener listener);

        IDialogBuilder addParam(String key, Object value);

    }

}
