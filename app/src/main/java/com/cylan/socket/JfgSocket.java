package com.cylan.socket;

/**
 * Created by holy on 2017/3/18.
 */


public class JfgSocket {


    /**
     * @param cb
     * @return C++ 对象，如果不需要使用，请释放 {@link JFGSocket#Release(long)}
     */
    public native static long InitSocket(JFGSocketCallBack cb);

    /**
     * @param nativeObj    {@link JFGSocket#InitSocket}
     * @param ip
     * @param port
     * @param autoReconnet
     * @return
     */
    public native static boolean Connect(long nativeObj, String ip, short port, boolean autoReconnet);


    /**
     * @param nativeObj {@link JFGSocket#InitSocket}
     * @param data
     * @return
     */
    public native static boolean SendMsgpackBuff(long nativeObj, byte[] data);

    /**
     * @param nativeObj {@link JFGSocket#InitSocket}
     */
    public native static void Disconnect(long nativeObj);

    /**
     * @param nativeObj {@link JFGSocket#InitSocket}
     */
    public native static void Release(long nativeObj);

    public interface JFGSocketCallBack {
        void OnConnected();

        void OnDisconnected();

        void OnMsgpackBuff(byte[] data);
    }

}
