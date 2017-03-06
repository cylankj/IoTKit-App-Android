package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/4.
 */

public interface IState {
    IState SUCCESS = new SuccessState();

    String state();

    class BaseState implements IState {
        protected String state;

        public BaseState(String state) {
            this.state = state;
        }

        @Override
        public String state() {
            return this.state;
        }
    }

    class SuccessState extends BaseState implements IDPState {

        public SuccessState() {
            super("SUCCESS");
        }
    }
}
