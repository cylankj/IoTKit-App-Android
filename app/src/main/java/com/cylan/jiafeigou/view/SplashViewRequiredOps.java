package com.cylan.jiafeigou.view;

/**
 * Created by chen on 5/12/16.
 */
public interface SplashViewRequiredOps {
        /**
         * Presenter -> View <br/>
         *
         * interface: 功能+View+Required+Ops
         * method:   名字+动词过去分词,表示任务执行完毕,回调
         * */
        void timeShowed();
        void cacheInited();
}
