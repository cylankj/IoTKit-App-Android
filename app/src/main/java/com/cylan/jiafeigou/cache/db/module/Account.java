package com.cylan.jiafeigou.cache.db.module;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.ext.annotations.DPProperty;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.cache.db.view.IEntity;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by yanzhendong on 2017/3/4.
 */

@Entity(active = true)
public class Account extends DataPoint implements IEntity<Account> {
    @Id
    private Long _id;
    @Unique
    private String account;
    private String phone;
    private String token;
    private String alias;
    private boolean enablePush;
    private boolean enableSound;
    private String email;
    private boolean enableVibrate;
    private String photoUrl;
    private String action;
    private String state;
    private String option;

    @DPProperty(msgId = 601)
    public transient DpMsgDefine.DPPrimary<String> account_state;

    @DPProperty(msgId = 602)
    public transient DpMsgDefine.DPSet<DpMsgDefine.DPWonderItem> account_wonderful_msg;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 335469827)
    private transient AccountDao myDao;


    public Account(JFGAccount account) {
        this.account = account.getAccount();
        this.phone = account.getPhone();
        this.token = account.getToken();
        this.alias = account.getAlias();
        this.enablePush = account.isEnablePush();
        this.enableSound = account.isEnableSound();
        this.email = account.getEmail();
        this.enableVibrate = account.isEnableVibrate();
        this.photoUrl = account.getPhotoUrl();
        this.action = DBAction.SAVED.action();
        this.state = DBState.SUCCESS.state();
    }

    @Generated(hash = 882125521)
    public Account() {
    }

    @Generated(hash = 447675577)
    public Account(Long _id, String account, String phone, String token, String alias,
            boolean enablePush, boolean enableSound, String email, boolean enableVibrate,
            String photoUrl, String action, String state, String option) {
        this._id = _id;
        this.account = account;
        this.phone = phone;
        this.token = token;
        this.alias = alias;
        this.enablePush = enablePush;
        this.enableSound = enableSound;
        this.email = email;
        this.enableVibrate = enableVibrate;
        this.photoUrl = photoUrl;
        this.action = action;
        this.state = state;
        this.option = option;
    }

    public String getAccount() {
        return this.account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean getEnablePush() {
        return this.enablePush;
    }

    public void setEnablePush(boolean enablePush) {
        this.enablePush = enablePush;
    }

    public boolean getEnableSound() {
        return this.enableSound;
    }

    public void setEnableSound(boolean enableSound) {
        this.enableSound = enableSound;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getEnableVibrate() {
        return this.enableVibrate;
    }

    public void setEnableVibrate(boolean enableVibrate) {
        this.enableVibrate = enableVibrate;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    public String getAction() {
        return this.action;
    }


    @Override
    public Account setAction(DBAction action) {
        if (action != null) {
            this.action = action.action();
        }
        return this;
    }

    @Override
    public DBAction action() {
        return DBAction.valueOf(this.action);
    }

    @Override
    public Account setState(DBState state) {
        if (state != null) {
            this.state = state.state();
        }
        return this;
    }

    @Override
    public DBState state() {
        return DBState.valueOf(this.state);
    }

    @Override
    public Account setOption(DBOption option) {
        if (option != null) {
            this.option = option.option();
        }
        return this;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    @Override
    public <R extends DBOption> R option(Class<R> clz) {
        return null;
    }

    public Account setAction(String action) {
        if (action != null) {
            this.action = action;
        }
        return this;
    }

    public String getState() {
        return this.state;
    }

    public Account setState(String state) {
        this.state = state;
        return this;
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1812283172)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAccountDao() : null;
    }


}
