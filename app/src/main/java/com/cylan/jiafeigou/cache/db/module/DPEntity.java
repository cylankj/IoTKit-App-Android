package com.cylan.jiafeigou.cache.db.module;

import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.dp.DataPoint;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Arrays;

/**
 * Created by yanzhendong on 2017/2/27.
 */
@Entity(generateGettersSetters = false, active = true)
public class DPEntity extends BaseDPEntity implements Comparable<DPEntity> {
    @Id
    private Long _id;
    private String account;
    private String server;
    private String uuid;
    private Long version;
    private Integer msgId;
    private byte[] bytes;
    private String action;
    private String state;
    private String option;//json 格式的字符串

    public transient DataPoint value;
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

    @Generated(hash = 99264848)
    public DPEntity(Long _id, String account, String server, String uuid, Long version,
                    Integer msgId, byte[] bytes, String action, String state, String option) {
        this._id = _id;
        this.account = account;
        this.server = server;
        this.uuid = uuid;
        this.version = version;
        this.msgId = msgId;
        this.bytes = bytes;
        this.action = action;
        this.state = state;
        this.option = option;
    }

    @Generated(hash = 592767460)
    public DPEntity() {
    }

    public IDPEntity set_id(Long id) {
        this._id = id;
        return this;
    }

    public Long get_id() {
        return _id;
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


    public String getOption() {
        return option;
    }

    public IDPEntity setOption(String option) {
        this.option = option;
        return this;
    }

    public DPEntity setAction(String action) {
        this.action = action;
        return this;
    }

    @Override
    public IDPEntity setAction(DBAction action) {
        if (action != null) {
            this.action = action.action();
        }
        return this;
    }

    @Override
    public DBAction action() {
        return DBAction.valueOf(this.action);
    }

    public String getAction() {
        return this.action;
    }

    @Override
    public IDPEntity setState(DBState state) {
        this.state = state.state();
        return this;
    }

    @Override
    public DBState state() {
        return DBState.valueOf(this.state);
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return this.state;
    }

    @Override
    public IDPEntity setOption(DBOption option) {
        if (option != null) {
            this.option = option.option();
        }
        return this;
    }

    @Override
    public <R extends DBOption> R option(Class<R> clz) {
        return DBOption.BaseDBOption.option(this.option, clz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        DPEntity value = (DPEntity) o;
        return version == value.version && msgId == value.msgId;

    }

    @Override
    public int hashCode() {
        int result = (int) (msgId ^ (msgId >>> 32));
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }

    @Override
    public int compareTo(DPEntity another) {
        return version == another.version ? 0 : version > another.version ? -1 : 1;//降序
    }


    @Override
    public String toString() {
        return "DPEntity{" +
                "_id=" + _id +
                ", account='" + account + '\'' +
                ", server='" + server + '\'' +
                ", uuid='" + uuid + '\'' +
                ", version=" + version +
                ", msgId=" + msgId +
                ", bytes=" + Arrays.toString(bytes) +
                ", action='" + action + '\'' +
                ", state='" + state + '\'' +
                ", option='" + option + '\'' +
                '}';
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
