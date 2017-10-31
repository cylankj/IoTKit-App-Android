package com.cylan.jiafeigou.base.view;

/**
 * Created by yzd on 16-12-28.
 */

public interface JFGPresenter {

    void subscribe();

    void unsubscribe();

    boolean isSubscribed();

    void uuid(String uuid);
}
