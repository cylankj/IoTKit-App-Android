package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.wrapper.BasePresenter;

import javax.inject.Inject;

/**
 * Created by holy on 2017/3/19.
 */
public class Pan720FullPresenter extends BasePresenter<Pan720FullContract.View> implements Pan720FullContract.Presenter {

    @Inject
    public Pan720FullPresenter(Pan720FullContract.View view) {
        super(view);
    }
}
