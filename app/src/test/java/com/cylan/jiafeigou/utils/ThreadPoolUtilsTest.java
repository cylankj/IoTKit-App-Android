package com.cylan.jiafeigou.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;

import org.junit.After;
import org.junit.Before;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hunt on 16-5-6.
 */
//@RunWith(MyTestRunner.class)
public class ThreadPoolUtilsTest {
    @Before
    public void setup() throws Exception {
        //别用内部类
        System.out.println("setup");

    }

    @After
    public void tearDown() throws Exception {
        System.out.println("tearDown");
    }

//    @Test
    public void testRun() throws InterruptedException {
        final int count = 20000;
        HandlerThread mHandlerThread = new HandlerThread("worker-handler-thread");
        mHandlerThread.start();
        Handler mHandler = new
                Handler(mHandlerThread.getLooper());
        for (int i = 0; i < count; i++) {
            mHandler.postDelayed(new TestRunnable(i), 1);
        }
        System.out.println("end");
        Thread.sleep(2000);
    }

    private static final int TEST_WHAT = 1;

    private boolean mGotMessage = false;
    private int mGotMessageWhat = -1;
    private volatile boolean mDidSetup = false;
    private volatile int mLooperTid = -1;

//    @Test
    public void testHandlerThread() throws Exception {
        HandlerThread th1 = new HandlerThread("HandlerThreadTest") {
            protected void onLooperPrepared() {
                synchronized (ThreadPoolUtilsTest.this) {
                    mDidSetup = true;
                    mLooperTid = Process.myTid();
                    ThreadPoolUtilsTest.this.notify();
                }
            }
        };

        assertFalse(th1.isAlive());
        assertNull(th1.getLooper());

        th1.start();

        assertTrue(th1.isAlive());
        assertNotNull(th1.getLooper());

        // The call to getLooper() internally blocks until the looper is
        // available, but will call onLooperPrepared() after that.  So we
        // need to block here to wait for our onLooperPrepared() to complete
        // and fill in the values we expect.
        synchronized (this) {
            while (!mDidSetup) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }

        // Make sure that the process was set.
        assertNotSame(-1, mLooperTid);
        // Make sure that the onLooperPrepared() was called on activity_cloud_live_mesg_video_talk_item different thread.
//        System.out.println(Process.myPid());
//        System.out.println(mLooperTid);
//        assertNotSame(Process.myTid(), mLooperTid);

        final Handler h1 = new Handler(th1.getLooper()) {
            public void handleMessage(Message msg) {
                assertEquals(TEST_WHAT, msg.what);
                // Ensure that we are running on the same thread in which the looper was setup on.
                assertEquals(mLooperTid, Process.myTid());

                mGotMessageWhat = msg.what;
                mGotMessage = true;
                synchronized (this) {
                    notifyAll();
                }
            }
        };

        Message msg = h1.obtainMessage(TEST_WHAT);

        synchronized (h1) {
            // wait until we have the lock before sending the message.
            h1.sendMessage(msg);
            try {
                // wait for the message to be handled
                h1.wait();
            } catch (InterruptedException e) {
            }
        }

        assertTrue(mGotMessage);
        assertEquals(TEST_WHAT, mGotMessageWhat);
    }


}