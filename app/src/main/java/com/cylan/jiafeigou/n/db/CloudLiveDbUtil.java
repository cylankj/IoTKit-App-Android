package com.cylan.jiafeigou.n.db;

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

    private CloudLiveDbUtil() {
        if (dbManager == null) {
            dbManager = DbManagerImpl.getInstance(getDaoconfig());
        }
    }

    public static CloudLiveDbUtil getInstance() {
        synchronized (CloudLiveDbUtil.class) {
            if (uniqueInstance == null) {
                uniqueInstance = new CloudLiveDbUtil();
            }
            return uniqueInstance;
        }
    }

    private DbManager.DaoConfig getDaoconfig() {
        return new DbManager.DaoConfig()
                .setAllowTransaction(true)
                .setContext(ContextUtils.getContext())
                .setDbName(DB_NAME)
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

    /**
     * desc：生成数据库的名字
     *
     * @return
     */
    private String generateDbName() {
        String db_name = "";

        //TODO 获取到用户的账号

        return db_name;
    }
}
