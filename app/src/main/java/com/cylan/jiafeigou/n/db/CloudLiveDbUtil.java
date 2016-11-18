package com.cylan.jiafeigou.n.db;

import android.os.Environment;

import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.DbManagerImpl;
import com.cylan.jiafeigou.support.db.table.TableEntity;
import com.cylan.jiafeigou.utils.ContextUtils;

/**
 * 作者：zsl
 * 创建时间：2016/10/11
 * 描述：
 */
public class CloudLiveDbUtil {

    private static final String DB_NAME = "cloud_live_db";

    private static CloudLiveDbUtil uniqueInstance = null;

    public static DbManager dbManager;

    private CloudLiveDbUtil(String dbName) {
        if (dbManager == null) {
            dbManager = DbManagerImpl.getInstance(getDaoconfig(dbName));
        }
    }

    public static CloudLiveDbUtil getInstance(String dbName) {
        synchronized (CloudLiveDbUtil.class) {
            if (uniqueInstance == null) {
                uniqueInstance = new CloudLiveDbUtil(dbName);
            }
            return uniqueInstance;
        }
    }

    private DbManager.DaoConfig getDaoconfig(String dbName) {
        return new DbManager.DaoConfig()
                .setAllowTransaction(true)
                .setContext(ContextUtils.getContext())
                .setDbDir(Environment.getExternalStorageDirectory())
                .setDbName(dbName)
                .setDbVersion(1)
                .setDbOpenListener(new DbManager.DbOpenListener() {
                    @Override
                    public void onDbOpened(DbManager db) {
                        db.getDatabase().enableWriteAheadLogging();
                    }
                })
                .setDbUpgradeListener(new MyDbLisenter());
    }

    private class MyDbLisenter implements DbManager.DbUpgradeListener {

        @Override
        public void onUpgrade(DbManager DbManager, int oldVersion, int newVersion) {
            //TODO 数据库的升级
        }
    }

}
