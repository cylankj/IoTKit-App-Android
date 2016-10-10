package com.cylan.jiafeigou.suit;

import com.cylan.jiafeigou.ExampleUnitTest;
import com.cylan.jiafeigou.HistoryDataTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by cylan-hunt on 16-10-9.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ExampleUnitTest.class,
        HistoryDataTest.class
})
public class TestSuite {
}
