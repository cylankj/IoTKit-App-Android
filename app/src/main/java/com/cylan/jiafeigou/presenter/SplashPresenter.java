package com.cylan.jiafeigou.presenter;

/**
 * Created by chen on 5/12/16.
 */
public interface SplashPresenter {
    /***********
     * View -> Presenter <br/>
     *
     * interface: 功能+Presenter+Ops
     * method:   动词+名词,表示执行任务
     */
    interface Ops {
        void initCache();
        void showTime();
    }

    /************
     * Model -> Present  <br/>
     *
     * interface:  功能+Presenter+Required+Ops
     * method:    on+名词+动词过去分词,表示事情实行完毕,回调
     *****/
    interface RequiredOps {
        void onTimeShowed();
        void onCacheInited();
    }
}
