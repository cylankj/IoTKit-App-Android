//package com.cylan.jiafeigou;
//
//import com.cylan.BuildConfig;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.ArgumentMatcher;
//import org.mockito.Mockito;
//import org.robolectric.annotation.Config;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.argThat;
//import static org.mockito.Mockito.atLeastOnce;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.mockito.internal.verification.VerificationModeFactory.atLeast;
//import static org.mockito.internal.verification.VerificationModeFactory.atMost;
//
///**
// * Created by cylan-hunt on 16-9-30.
// */
//@RunWith(MyTestRunner.class)
//@Config(constants = BuildConfig.class, sdk = 21)
//public class MockExampleTest {
//    @Test
//    public void mockTest() {
////        List mockedList = mock(List.class);
////
////        //using mock object
////        mockedList.add("one");
////        mockedList.clearLocal();
////
////        //verification
////        verify(mockedList).add("one");
////        verify(mockedList).clearLocal();
//        //You can mock concrete classes, not just interfaces
//        //例子
//        // http://waylau.com/mockito-quick-start/
//        LinkedList mockedList = mock(LinkedList.class);
//
//        //stubbing
//        when(mockedList.get(0)).thenReturn("first");
////        when(mockedList.get(1)).thenThrow(new RuntimeException());
//
//        //following prints "first"
//        System.out.println(mockedList.get(0));
//
//        //following throws runtime exception
//        System.out.println(mockedList.get(1));
//
//        //following prints "null" because get(999) was not stubbed
//        System.out.println(mockedList.get(999));
//
//        //Although it is possible to verify a stubbed invocation, usually it'account just redundant
//        //If your code cares what get(0) returns, then something else breaks (often even before verify() gets executed).
//        //If your code doesn't care what get(0) returns, then it should not be stubbed. Not convinced? See here.
//        verify(mockedList).get(0);
//    }
//
//    @Test//验证行为是否发生。
//    public void testVerify() {
//        List<Integer> list = Mockito.mock(List.class);
//        list.add(0);
//        //验证行为是否发生。
//        verify(list).add(0);
//        list.add(1);
//        verify(list).add(1);
//    }
//
//    @Test//模拟预期结果
//    public void test_thenReturn() {
//        Iterator iterator = mock(Iterator.class);
//        //第1次返回 hello ,第1+n次返回world
//        when(iterator.next()).thenReturn("hello").thenReturn("world");
//        String result = "0: " + iterator.next() + " 1:" + iterator.next() + " 2:" + iterator.next();
//        System.out.println("result: " + result);
//        assertEquals(result, "0: hello 1:world 2:world");
//    }
//
//    @Test(expected = IOException.class)
//    public void when_thenThrow() throws IOException {
//        OutputStream outputStream = mock(OutputStream.class);
//        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
//        //预设当流关闭时抛出异常
//        doThrow(new IOException()).when(outputStream).close();
//        outputStream.close();
//        writer.close();
//    }
//
//    //参数匹配
//    @Test
//    public void test_withArgument() {
//        Comparable<String> comparable = mock(Comparable.class);
//        //预设根据不同的参数返回不同的结果
//        when(comparable.compareTo("Test")).thenReturn(1);
//        when(comparable.compareTo("Omg")).thenReturn(2);
//        assertEquals(1, comparable.compareTo("Test"));
//        assertEquals(2, comparable.compareTo("Omg"));
//        //对于没有预设的情况会返回默认值
//        assertEquals(0, comparable.compareTo("Not stub"));
//    }
//
//    //不确定参数
//    @Test
//    public void with_unspecified_arguments() {
//        List list = mock(List.class);
//        //匹配任意参数
//        when(list.get(anyInt())).thenReturn(1);
//        when(list.contains(argThat(new IsValid()))).thenReturn(true);
//        assertEquals(1, list.get(1));
//        assertEquals(1, list.get(999));
////        assertTrue(list.contains(1));
//        assertTrue(!list.contains(3));
//    }
//
//    private class IsValid implements ArgumentMatcher<List> {
//        @Override
//        public boolean matches(List argument) {
//            return argument.size() > 0;
//        }
//    }
//
//    //验证确切的调用次数
//    @Test
//    public void verifying_number_of_invocations() {
//        List<Integer> list = mock(List.class);
//        list.add(1);
//        list.add(2);
//        list.add(2);
//        list.add(3);
//        list.add(3);
//        list.add(3);
//        //验证是否被调用一次，等效于下面的times(1)
//        verify(list).add(1);
//        verify(list, times(1)).add(1);
//        //验证是否被调用2次
//        verify(list, times(2)).add(2);
//        //验证是否被调用3次
//        verify(list, times(3)).add(3);
//        //验证是否从未被调用过
//        verify(list, never()).add(4);
//        //验证至少调用一次
//        verify(list, atLeastOnce()).add(1);
//        //验证至少调用2次
//        verify(list, atLeast(2)).add(2);
//        //验证至多调用3次
//        verify(list, atMost(3)).add(3);
//    }
//
//    //模拟方法体抛出异常
//    @Test(expected = RuntimeException.class)
//    public void doThrow_when() {
//        List<Integer> list = mock(List.class);
//        doThrow(new RuntimeException()).when(list).add(1);
//        list.add(1);
//    }
//}