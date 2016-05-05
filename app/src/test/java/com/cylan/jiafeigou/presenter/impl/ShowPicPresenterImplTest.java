package com.cylan.jiafeigou.presenter.impl;

import android.content.Context;

import com.cylan.jiafeigou.MyTestRunner;
import com.cylan.jiafeigou.view.ShowBigPicView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * Created by hunt on 16-5-5.
 */
@RunWith(MyTestRunner.class)
public class ShowPicPresenterImplTest {


    @Before
    public void setup() throws Exception {
        System.out.println("setup");

    }

    @After
    public void tearDown() throws Exception {
        System.out.println("tearDown");
    }

    @Test
    public void testfindMatch() {
        ShowPicPresenterImpl showPicPresenter = new ShowPicPresenterImpl(new ShowBigPicView() {
            @Override
            public void shareFinish() {

            }

            @Override
            public void downloadFinish(int state) {

            }

            @Override
            public void initView() {

            }

            @Override
            public Context getContext() {
                return null;
            }
        });
        String url0 = "http://jiafeigou.oss-cn-hangzhou.aliyuncs.com/500000001123%2F1462358869.jpg?OSSAccessKeyId=xjBdwD1du8lf2wMI&Expires=1462963707&Signature=NglFLKoEURHI7OnitwEpK7dd9%2Fw%3D_768x1280";
        String url1 = "http://jiafeigou.oss-cn-hangzhou.aliyuncs.com/500000001123%2F1462358869_3.jpg?OSSAccessKeyId=xjBdwD1du8lf2wMI&Expires=1462963707&Signature=NglFLKoEURHI7OnitwEpK7dd9%2Fw%3D_768x1280";
        String result0 = showPicPresenter.findMatch(url0);
        assertEquals(result0, "1462358869.jpg");
        String date = showPicPresenter.convertSimpleName2Name(result0);

        System.out.println(date);
        String result1 = showPicPresenter.findMatch(url1);
        assertEquals(result1, "1462358869_3.jpg");
        String date1 = showPicPresenter.convertSimpleName2Name(result1);
        System.out.println(date1);

    }
}