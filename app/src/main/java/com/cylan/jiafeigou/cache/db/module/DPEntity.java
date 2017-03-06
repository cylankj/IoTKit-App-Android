package com.cylan.jiafeigou.cache.db.module;

import com.cylan.jiafeigou.cache.db.view.IAction;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by yanzhendong on 2017/2/27.
 */
@Entity(active = true, generateGettersSetters = false)
public class DPEntity extends BaseDPEntity {
    @Id
    private Long id;
    private String account;
    private String server;
    private String uuid;
    private Long version;
    private Integer msgId;
    private byte[] bytes;
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
    @Generated(hash = 1268361579)
    private transient DPEntityDao myDao;

    @Generated(hash = 568731944)
    public DPEntity(Long id, String account, String server, String uuid, Long version,
                    Integer msgId, byte[] bytes, String action, String state) {
        this.id = id;
        this.account = account;
        this.server = server;
        this.uuid = uuid;
        this.version = version;
        this.msgId = msgId;
        this.bytes = bytes;
        this.action = action;
        this.state = state;
    }

    @Generated(hash = 592767460)
    public DPEntity() {
    }

    public IDPEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public IDPEntity setAccount(String account) {
        this.account = account;
        return this;
    }

    public String getAccount() {
        return account;
    }

    public IDPEntity setServer(String server) {
        this.server = server;
        return this;
    }

    public String getServer() {
        return server;
    }

    public IDPEntity setBytes(byte[] bytes) {
        this.bytes = bytes;
        return this;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public IDPEntity setMsgId(Integer msgId) {
        this.msgId = msgId;
        return this;
    }

    @Override
    public Integer getMsgId() {
        return this.msgId;
    }

    @Override
    public IDPEntity setVersion(Long version) {
        this.version = version;
        return this;
    }

    @Override
    public Long getVersion() {
        return this.version;
    }

    @Override
    public IDPEntity setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    @Override
    public IDPEntity setAction(IAction action) {
        this.action = action.action();
        return this;
    }

    @Override
    public String ACTION() {
        return IAction.BaseAction.$(action, IAction.BaseAction.class).ACTION();
    }

    @Override
    public DPEntity setAction(String action) {
        this.action = action;
        return this;
    }

    @Override
    public String getAction() {
        return this.action;
    }

    @Override
    public DPEntity setState(String state) {
        this.state = state;
        return this;
    }

    @Override
    public String getState() {
        return this.state;
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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 69931815)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDPEntityDao() : null;
    }

}
