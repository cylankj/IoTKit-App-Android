package com.cylan.jiafeigou.cache.db.module;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by yanzhendong on 2017/3/4.
 */

@Entity
public class Account extends BasePropertyHolder<Account> {
    @Id
    private Long _id;
    @Unique
    private String account;
    private String server;
    private String password;
    private int loginType;
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
    private String accountJson;
    private transient boolean isOnline;
    private transient boolean available = false;

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return this.isOnline;
    }

    public Account(JFGAccount account) {
        setAccount(account);
    }

    public Account setAccount(JFGAccount account) {
        this.accountJson = new Gson().toJson(account);
        this.account = account.getAccount();
        this.phone = account.getPhone();
//        this.token = account.getToken();
        this.alias = account.getAlias();
        this.enablePush = account.isEnablePush();
        this.enableSound = account.isEnableSound();
        this.email = account.getEmail();
        this.enableVibrate = account.isEnableVibrate();
        this.photoUrl = account.getPhotoUrl();
        if (TextUtils.isEmpty(this.action)) {
            this.action = DBAction.SAVED.action();
        }
        if (TextUtils.isEmpty(this.state)) {
            this.state = DBState.ACTIVE.state();
        }
        this.available = true;
        return this;
    }
@Keep
    public Account() {
    }

    @Keep
    public Account(Long _id, String account, String server, String password, int loginType, String phone,
                   String token, String alias, boolean enablePush, boolean enableSound, String email,
                   boolean enableVibrate, String photoUrl, String action, String state, String option,
                   String accountJson) {
        this._id = _id;
        this.account = account;
        this.server = server;
        this.password = password;
        this.loginType = loginType;
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
        this.accountJson = accountJson;
        this.available = true;
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

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLoginType() {
        return this.loginType;
    }

    public void setLoginType(int loginType) {
        this.loginType = loginType;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        this.server = server;
    }


    @Override
    protected int pid() {
        return -1;
    }

    @Override
    protected String uuid() {
        return null;
    }

    public String getAccountJson() {
        return this.accountJson;
    }

    public void setAccountJson(String accountJson) {
        this.accountJson = accountJson;
    }

    public boolean isAvailable() {
        return available;
    }
}
