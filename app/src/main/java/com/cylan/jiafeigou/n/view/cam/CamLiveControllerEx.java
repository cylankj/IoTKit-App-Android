package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;

/**
 * Created by hds on 17-4-19.
 */

public class CamLiveControllerEx extends ConstraintLayout implements ICamLiveLayer {


    public CamLiveControllerEx(Context context) {
        this(context, null);
    }

    public CamLiveControllerEx(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CamLiveControllerEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void showLayoutTopBar(int visibility) {

    }

    @Override
    public void showLayoutFlow(int visibility) {

    }

    @Override
    public void showLayoutLoading(int visibility) {

    }

    @Override
    public void showLayoutTime(int visibility) {

    }

    @Override
    public void showLayoutWheel(int visibility) {

    }

    @Override
    public void showLayoutBottom(int visibility) {

    }
}
