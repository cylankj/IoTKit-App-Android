package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/4.
 */

public interface IAccountState extends IState {

    IAccountState ACTIVE = new AccountActiveState();
    IAccountState NORMAL = new AccountNormalState();


    class AccountActiveState extends BaseState implements IAccountState {

        public AccountActiveState() {
            super("ACTIVE");
        }
    }

    class AccountNormalState extends BaseState implements IAccountState {

        public AccountNormalState() {
            super("NORMAL");
        }
    }
}
