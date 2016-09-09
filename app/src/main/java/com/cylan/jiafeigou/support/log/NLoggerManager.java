package com.cylan.jiafeigou.support.log;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by cylan-hunt on 16-8-17.
 */
public class NLoggerManager {

    private static HashMap<String, NLogger> loggerMap = new HashMap<>();

    public static NLogger getLogger(final String filePath) throws IOException {
        if (filePath == null || filePath.length() == 0)
            return null;
        NLoggerConfigurator configurator = new NLoggerConfigurator.Builder()
                .setFilePath(filePath)
                .build();
        return getLogger(configurator);
    }

    public static NLogger getLogger(NLoggerConfigurator configurator) throws IOException {
        NLogger logger = loggerMap.get(configurator.getFilePath());
        if (logger == null) {
            logger = new NLogger(configurator);
            loggerMap.put(configurator.getFilePath(), logger);
        }
        return logger;
    }

}
