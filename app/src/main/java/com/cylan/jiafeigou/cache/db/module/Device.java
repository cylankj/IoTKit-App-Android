package com.cylan.jiafeigou.cache.db.module;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.cache.db.view.IAction;
import com.cylan.jiafeigou.cache.db.view.IDeviceAction;
import com.cylan.jiafeigou.cache.db.view.IDeviceState;
import com.cylan.jiafeigou.cache.db.view.IEntity;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by yanzhendong on 2017/3/4.
 */

@Entity(active = true)
public class Device implements IEntity<Device> {
    @Id
    private Long id;
    @Unique
    private String uuid;
    private String sn;
    private String alias;
    private String shareAccount;
    private int pid;
    private String vid;
    private String account;
    private String action;
    private String state;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 371273952)
    private transient DeviceDao myDao;

    @Generated(hash = 652905847)
    public Device(Long id, String uuid, String sn, String alias, String shareAccount, int pid,
            String vid, String account, String action, String state) {
        this.id = id;
        this.uuid = uuid;
        this.sn = sn;
        this.alias = alias;
        this.shareAccount = shareAccount;
        this.pid = pid;
        this.vid = vid;
        this.account = account;
        this.action = action;
        this.state = state;
    }

    public Device(JFGDevice device) {
        this.uuid = device.uuid;
        this.sn = device.sn;
        this.alias = device.alias;
        this.shareAccount = device.shareAccount;
        this.pid = device.pid;
        this.vid = device.vid;
        this.action = IDeviceAction.SAVED.action();
        this.state = IDeviceState.SUCCESS.state();
    }

    @Generated(hash = 1469582394)
    public Device() {
    }

    @Override
    public Device setAction(IAction action) {
        this.action = action.action();
        return this;
    }

    @Override
    public String ACTION() {
        return IAction.BaseAction.$(action, IAction.BaseAction.class).ACTION();
    }

    @Override
    public Device setAction(String action) {
        this.action = action;
        return this;
    }

    @Override
    public String getAction() {
        return this.action;
    }

    @Override
    public Device setState(String state) {
        this.state = state;
        return this;
    }

    @Override
    public String getState() {
        return this.state;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSn() {
        return this.sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getShareAccount() {
        return this.shareAccount;
    }

    public void setShareAccount(String shareAccount) {
        this.shareAccount = shareAccount;
    }

    public int getPid() {
        return this.pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getVid() {
        return this.vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
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

    public String getAccount() {
        return this.account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1755220927)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDeviceDao() : null;
    }
}
