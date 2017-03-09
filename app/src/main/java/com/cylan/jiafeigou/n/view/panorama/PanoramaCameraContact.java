package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.PropertyView;
import com.cylan.jiafeigou.base.view.ViewablePresenter;
import com.cylan.jiafeigou.base.view.ViewableView;

/**
 * Created by yanzhendong on 2017/3/7.
 */

public interface PanoramaCameraContact {

    interface View extends PropertyView, ViewableView {
        enum SPEED_MODE {
            AUTO {
                @Override
                public SPEED_MODE prev() {
                    return HD;
                }

                @Override
                public SPEED_MODE next() {
                    return FLUENCY;
                }
            }, FLUENCY {
                @Override
                public SPEED_MODE prev() {
                    return AUTO;
                }

                @Override
                public SPEED_MODE next() {
                    return NORMAL;
                }
            }, NORMAL {
                @Override
                public SPEED_MODE prev() {
                    return FLUENCY;
                }

                @Override
                public SPEED_MODE next() {
                    return HD;
                }
            }, HD {
                @Override
                public SPEED_MODE prev() {
                    return NORMAL;
                }

                @Override
                public SPEED_MODE next() {
                    return AUTO;
                }
            };

            public abstract SPEED_MODE prev();

            public abstract SPEED_MODE next();
        }

        void onSwitchSpeedMode(SPEED_MODE mode);
    }

    interface Presenter extends ViewablePresenter {

    }
}
