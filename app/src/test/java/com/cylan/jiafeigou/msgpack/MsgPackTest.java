package com.cylan.jiafeigou.msgpack;

import com.cylan.jiafeigou.utils.MD5Util;

import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by holy on 2017/3/17.
 */

public class MsgPackTest {

    @Test
    public void test() {
//        byte[] data = new byte[]{-108, 0, -82, 49, 52, 56, 57, 55, 53, 50, 55, 48, 53, 46, 106, 112, 103, -50, 0, 27, -79, 101, -80, 93, -83, -8, 107, 63, -68, -51, -74, 66, 2, -104, 104, -8, -56, -3, -127};
//        MessagePack mp = new MessagePack();
//        try {
//            test1 test = mp.createBufferUnpacker(data).read(test1.class);
//            System.out.printf("t:" + test);
//            System.out.printf(MD5Util.MD5(test.md5));
//        } catch (IOException e) {
//            System.out.printf("");
//        }
        try {
            InetAddress[] machines = InetAddress.getAllByName("yahoo.com");
            System.out.println(machines.length);
            for (InetAddress address : machines) {
                System.out.println(address.getHostAddress());
            }
        } catch (UnknownHostException e) {
            System.out.println("null:" + e.getLocalizedMessage());
        }

    }

    @Message
    public static class Test1 {

        @Index(0)
        public int ret;//       错误码
        @Index(1)
        public String fileName;//  文件名， 命名格式[timestamp].jpg 或 [timestamp]_[secends].avi， timestamp是文件生成时间的unix时间戳，secends是视频录制的时长,单位秒。根据后缀区分是图片或视频。
        @Index(2)
        public int fileSize;//  文件大小, bit。
        @Index(3)
        public byte[] md5;//  文件的md5值

        @Override
        public String toString() {
            return "test1{" +
                    "ret=" + ret +
                    ", fileName='" + fileName + '\'' +
                    ", fileSize=" + fileSize +
                    ", md5=" + Arrays.toString(md5) +
                    '}';
        }
    }
}
