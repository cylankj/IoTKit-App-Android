package com.cylan.jiafeigou;

import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;

/**
 * Created by cylan-hunt on 16-10-11.
 */
public class SmartcallActivityTest extends ActivityUnitTestCase<SmartcallActivity> {

    private SmartcallActivity smartcallActivity;

    public SmartcallActivityTest() {
        super(SmartcallActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        smartcallActivity = getActivity();
    }

    @Override
    public void tearDown() throws Exception {
        smartcallActivity = null;
    }

    @MediumTest
    public void testPrepare() {
        System.out.println("null?" + smartcallActivity);
        assertNotNull("smartcallActivity is null? ", smartcallActivity);
    }

}