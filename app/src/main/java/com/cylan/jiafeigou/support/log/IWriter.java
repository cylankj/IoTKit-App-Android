package com.cylan.jiafeigou.support.log;

import java.io.IOException;

/**
 * Created by cylan-hunt on 16-8-17.
 */
public interface IWriter {

    void write(final String message) throws IOException, Exception;
}
