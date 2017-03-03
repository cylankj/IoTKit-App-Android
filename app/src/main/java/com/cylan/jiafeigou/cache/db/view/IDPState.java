package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/3.
 */

public interface IDPState {

    IDPState SUCCESS = new DPSuccessState();
    IDPState NOT_CONFIRM = new DPNotConfirmState();

    String state();

    abstract class BaseDPState implements IDPState {
        protected String state;

        public BaseDPState(String state) {
            this.state = state;
        }

        @Override
        public String state() {
            return this.state;
        }
    }

    class DPSuccessState extends BaseDPState {

        public DPSuccessState() {
            super("SUCCESS");
        }
    }

    class DPNotConfirmState extends BaseDPState {


        public DPNotConfirmState() {
            super("NOT_CONFIRM");
        }
    }
}
