package com.cylan.jiafeigou.cache.db.module;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.ext.annotations.DPProperty;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.cache.db.view.IEntity;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.google.gson.Gson;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by yanzhendong on 2017/3/4.
 */

@Entity(active = true)
public class Device extends DataPoint implements IEntity<Device> {
    @Id
    public Long _id;
    public String uuid;
    public String sn;
    public String alias;
    public String shareAccount;
    public int pid;
    public String vid;
    public String account;
    public int regionType;
    public String server;
    public String action;
    public String state;
    public String option;

    @DPProperty(msgId = 202)
    public transient DpMsgDefine.DPPrimary<String> mac;

    @DPProperty(msgId = 205)
    public transient DpMsgDefine.DPPrimary<Boolean> charging;

    @DPProperty(msgId = 207)
    public transient DpMsgDefine.DPPrimary<String> device_version;

    @DPProperty(msgId = 208)
    public transient DpMsgDefine.DPPrimary<String> device_sys_version;

    @DPProperty(msgId = 209)
    public transient DpMsgDefine.DPPrimary<Boolean> led_indicator;

    @DPProperty(msgId = 211)
    public transient DpMsgDefine.DPPrimary<Integer> app_upload_log;

    @DPProperty(msgId = 212)
    public transient DpMsgDefine.DPPrimary<String> device_upload_log;

    @DPProperty(msgId = 213)
    public transient DpMsgDefine.DPPrimary<Integer> device_p2p_version;

    @DPProperty(msgId = 220)
    public transient DpMsgDefine.DPPrimary<String> sdk_version;

    @DPProperty(msgId = 304)
    public transient DpMsgDefine.DPPrimary<Integer> device_camera_rotate;
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

    @Generated(hash = 182677992)
    public Device(Long _id, String uuid, String sn, String alias, String shareAccount,
                  int pid, String vid, String account, int regionType, String server, String action,
                  String state, String option) {
        this._id = _id;
        this.uuid = uuid;
        this.sn = sn;
        this.alias = alias;
        this.shareAccount = shareAccount;
        this.pid = pid;
        this.vid = vid;
        this.account = account;
        this.regionType = regionType;
        this.server = server;
        this.action = action;
        this.state = state;
        this.option = option;
    }

    public Device setDevice(JFGDevice device) {
        this.uuid = device.uuid;
        this.sn = device.sn;
        this.alias = device.alias;
        this.shareAccount = device.shareAccount;
        this.pid = device.pid;
        this.vid = device.vid;
        this.regionType = device.regionType;
        this.action = DBAction.SAVED.action();
        this.state = DBState.SUCCESS.state();
        return this;
    }

    public Device fill(Device device) {
        this.uuid = device.uuid;
        this.sn = device.sn;
        this.alias = device.alias;
        this.shareAccount = device.shareAccount;
        this.pid = device.pid;
        this.vid = device.vid;
        this.regionType = device.regionType;
        this.action = device.action;
        this.state = device.state;
        this.option = device.option;
        return this;
    }


    @Generated(hash = 1469582394)
    public Device() {
    }

    public Device setAction(String action) {
        this.action = action;
        return this;
    }


    public Device setState(String state) {
        this.state = state;
        return this;
    }

    @Override
    public Device setAction(DBAction action) {
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
    public Device setState(DBState state) {
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
    public Device setOption(DBOption option) {
        if (option != null) {
            this.option = option.option();
        }
        return this;
    }

    @Override
    public <R extends DBOption> R option(Class<R> clz) {
        return new Gson().fromJson(this.option, clz);
    }

    public String getAction() {
        return this.action;
    }

    public String getState() {
        return this.state;
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


    public String getOption() {
        return this.option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getRegionType() {
        return this.regionType;
    }

    public void setRegionType(int regionType) {
        this.regionType = regionType;
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1755220927)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDeviceDao() : null;
    }
}
