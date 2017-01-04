package com.cylan.jiafeigou.base.view;

/**
 * Created by yzd on 16-12-30.
 */

public interface CallablePresenter extends ViewablePresenter {

    /**
     * 可拨打的view都应该具备接听的能力
     */
    void pickup();

    /**
     * 可拨打的view都应该具备响应call的能力
     */
    void newCall(Caller caller);

    class Caller {
        public String caller;//呼叫者
        public String picture;//呼叫者的图像
    }
}
