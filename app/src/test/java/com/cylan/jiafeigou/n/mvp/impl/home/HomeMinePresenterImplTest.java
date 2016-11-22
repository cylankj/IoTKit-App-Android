package com.cylan.jiafeigou.n.mvp.impl.home;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * 作者：zsl
 * 创建时间：2016/11/3
 * 描述：
 */
public class HomeMinePresenterImplTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testStart() throws Exception {
        HomeMinePresenterImpl homeMinePresenter =Mockito.mock(HomeMinePresenterImpl.class);
        assertTrue(homeMinePresenter.createRandomName().contains("d"));

    }

    @Test
    public void testStop() throws Exception {

    }

    @Test
    public void testRequestLatestPortrait() throws Exception {

    }

    @Test
    public void testPortraitBlur() throws Exception {

    }

    @Test
    public void testPortraitUpdateByUrl() throws Exception {
    }

    @Test
    public void testWhichLoginMethd() throws Exception {

    }

    @Test
    public void testCreateRandomName() throws Exception {

    }

    @Test
    public void testInitData() throws Exception {

    }
}