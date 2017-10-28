package com.cylan.jiafeigou.n.mvp.impl;

import com.cylan.jiafeigou.n.mvp.BaseFragmentView;

/**
 * 一个基本模型的Presenter
 * Created by cylan-hunt on 16-6-30.
 */
public abstract class AbstractFragmentPresenter<T extends BaseFragmentView> extends AbstractPresenter<T>  {

    public AbstractFragmentPresenter(T view) {
        super(view);
        this.uuid = mView.uuid();
    }

    protected boolean check() {
        return mView != null && mView.isAdded();
    }
//    public boolean isEarpiecePlug() {
//        super.isEarpiecePlug()
//        if (headsetObserver == null) {
//            headsetObserver = HeadsetObserver.getHeadsetObserver();
//        }
//        return headsetObserver.isHeadsetOn();
//    }
//
//    public void switchEarpiece(boolean enable) {
//        getAudioManager().setMode(enable ? AudioManager.MODE_CURRENT : AudioManager.MODE_IN_CALL);
//        getAudioManager().setSpeakerphoneOn(!enable);
//    }
}
