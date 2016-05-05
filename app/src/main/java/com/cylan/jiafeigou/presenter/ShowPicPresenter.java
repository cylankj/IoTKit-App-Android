package com.cylan.jiafeigou.presenter;

/**
 * Created by hunt on 16-5-5.
 */
public interface ShowPicPresenter extends BasePresenter {

    /**
     * share by other share-sdks
     */
    void share();

    /**
     * save to local and notify media-store
     * ,this method should be invoked from io thread
     *
     * @param url : key for ImageLoader
     */
    void download(final String url);


}
