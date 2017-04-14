package com.cylan.jiafeigou.cache.db.module;

import android.support.annotation.IntDef;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yanzhendong on 2017/3/4.
 */

@Entity
public class Device extends BasePropertyHolder<Device> {
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

    private transient boolean available = false;

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
        if (device == null) return this;
        this.uuid = device.uuid;
        this.sn = device.sn;
        this.alias = device.alias;
        this.shareAccount = device.shareAccount;
        this.pid = device.pid;
        this.vid = device.vid;
        this.regionType = device.regionType;
        this.action = DBAction.SAVED.action();
        this.state = DBState.SUCCESS.state();
        this.available = true;
        return this;
    }

    @Generated(hash = 1469582394)
    public Device() {
    }

    public boolean available() {
        return available;
    }

    public Device setAction(String action) {
        this.action = action;
        return this;
    }

    public final static Device ghost;

    static {
        ghost = new Device();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Device device = (Device) o;

        return uuid != null ? uuid.equals(device.uuid) : device.uuid == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DPDevice{" +
                "uuid='" + uuid + '\'' +
                ", sn='" + sn + '\'' +
                ", alias='" + alias + '\'' +
                ", shareAccount='" + shareAccount + '\'' +
                ", pid=" + pid +
                ", vid='" + vid + '\'' +
                ", account='" + account + '\'' +
                ", regionType=" + regionType +
                ", server='" + server + '\'' +
                ", action='" + action + '\'' +
                ", state='" + state + '\'' +
                '}';
    }

    @Override
    protected int pid() {
        return this.pid;
    }

    @IntDef({})
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @interface MsgId {
    }

    @Override
    public <V> V $(@MsgId int msgId, V defaultValue) {
        return super.$(msgId, defaultValue);
    }

    @Override
    protected String uuid() {
        return uuid;
    }
}
