package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/3.
 */

public interface IDPState extends IState {
    IDPState NOT_CONFIRM = new DPNotConfirmState();

    class DPNotConfirmState extends BaseState implements IDPState {


        public DPNotConfirmState() {
            super("NOT_CONFIRM");
        }
    }


}
