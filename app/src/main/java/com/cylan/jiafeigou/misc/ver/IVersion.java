package com.cylan.jiafeigou.misc.ver;

import rx.Observable;

/**
 * Created by hds on 17-5-28.
 */


public interface IVersion<T extends IVersion.BaseVersion> {

    /**
     * 什么条件才去检查
     *
     * @return
     */
    boolean checkCondition();


    /**
     * 开始检测
     */
    Observable<T> startCheck();


    class BaseVersion {
        private String desc;
        private int versionCode;
        private String versionName;
        private String url;
        private String saveDir;
        private String filaName;
    }
}
