package com.cylan.jiafeigou.n.mvp.impl.home;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddReqDetailContract;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.google.common.base.Verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

import rx.functions.Action1;

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