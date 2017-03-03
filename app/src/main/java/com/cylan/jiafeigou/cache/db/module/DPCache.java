package com.cylan.jiafeigou.cache.db.module;

import com.cylan.jiafeigou.cache.db.DPCacheDao;
import com.cylan.jiafeigou.cache.db.DaoSession;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by yanzhendong on 2017/2/27.
 */
@Entity(active = true, generateGettersSetters = false)
public class DPCache implements IDPEntity {
    @Id
    private Long id;
    private String account;
    private String server;
    private String uuid;
    private Long version;
    private Integer msgId;
    private byte[] bytes;
    private String tag;
    private String state;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 559718613)
    private transient DPCacheDao myDao;

    @Generated(hash = 1364992632)
    public DPCache(Long id, String account, String server, String uuid, Long version,
                   Integer msgId, byte[] bytes, String tag, String state) {
        this.id = id;
        this.account = account;
        this.server = server;
        this.uuid = uuid;
        this.version = version;
        this.msgId = msgId;
        this.bytes = bytes;
        this.tag = tag;
        this.state = state;
    }

    @Generated(hash = 2141781192)
    public DPCache() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return this.account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUuid() {
        return this.uuid;
    }

    public IDPEntity setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public Long getVersion() {
        return this.version;
    }

    public IDPEntity setVersion(Long version) {
        this.version = version;
        return this;
    }

    public Integer getMsgId() {
        return this.msgId;
    }

    public IDPEntity setMsgId(Integer msgId) {
        this.msgId = msgId;
        return this;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getTag() {
        return this.tag;
    }

    public IDPEntity setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public String getState() {
        return this.state;
    }

    public IDPEntity setState(String state) {
        this.state = state;
        return this;
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

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 730875712)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDPCacheDao() : null;
    }
}
