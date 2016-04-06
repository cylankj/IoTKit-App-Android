package com.cylan.publicApi;

public class JniPlay {
    /**
     * 初始化jni
     *
     * @param obj      需要实现 handleMsg(int msgId, byte[] bytes)
     * @param isCamera true为摄像头，false为客户端
     * @param workDir  jni会在workDir下写日志
     * @return true 成功，false失败
     */
    public native static boolean NativeInit(Object obj, boolean isCamera, String workDir);

    /**
     * 连接服务器
     *
     * @param server 服务器域名或IP地址
     * @param port   服务器消息服务端口
     * @return 0成功，其他值失败，为异步调用，真正值需要在HandleMsg中处理一下
     */
    public native static int ConnectToServer(String server, int port);

    /**
     * 断开服务器连接
     *
     * @return
     */
    public native static int DisconnectFromServer();

    /**
     * 客户端连接摄像头
     *
     * @param peer        对端cid
     * @param enableVideo true 启用视频，false不启用
     * @param netType     本地网络类型
     * @param isHistory   是否历史录像呼叫
     * @param peerOS      对端OS类型
     * @param peerRelay   peerRelay参数是MsgRelaymaskRsp  的msgpack 字串
     * @param forceRelay  // 不管它，主要方便测试用。
     * @return 0成功，其他值失败
     */
    public native static int ConnectToPeer(String peer, boolean enableVideo, int netType, boolean isHistory,
                                           int peerOS, int[] peerRelay, boolean forceRelay, boolean fastp2p);

    /**
     * 摄像头应答，目前是自动实现的
     *
     * @param videoQual 该参数目前没用
     * @return 0成功，其他值失败
     */
    public native static int AnswerCall(boolean videoQual);

    /**
     * 客户端/摄像头 断开当前呼叫连接
     *
     * @return 0成功，其他值失败
     */
    public native static int DisconnectFromPeer();

    /**
     * 判断当前是不是在呼叫当中
     *
     * @return true 当前是在呼叫中， false当前不在呼叫中
     */
    public native static boolean IsInCall();

    /**
     * 发起HTTP GET请求
     *
     * @param host        HTTP 主机
     * @param port        HTTP 主机端口
     * @param requestPath 请求路径
     * @return 返回 requestid标识，小于0失败
     */
    public native static int HttpGet(String host, int port, String requestPath);

    /**
     * 发起HTTP POST请求
     *
     * @param host        HTTP 主机
     * @param port        HTTP 主机端口
     * @param requestPath 请求路径
     * @param filePath    文件路径
     * @return 返回requestid标识，小于0失败
     */
    public native static int HttpPostFile(String host, int port, String requestPath, String filePath);

    /**
     * 启动http服务器
     *
     * @param root_dir 文件夹路径
     */
    public native static int SetHttpRoot(String root_dir);

    /**
     * 发送MsgPack消息
     *
     * @param msgpackStr msgpack 打包的消息体
     * @return
     */
    public native static boolean SendBytes(byte[] msgpackStr);

    /**
     * 设置呼叫需要的RelayServer地址
     *
     * @param relayServerStr 其中relayServerStr 是msgpack的字串
     */
    public native static void UpdateRelayServer(byte[] relayServerStr);

    /*** 以下是媒体类操作接口***/

    /**
     * 打开喇叭
     *
     * @param enable true打开喇叭，false关闭喇叭
     * @return 0成功，其他值失败
     */
    public native static int EnableSpeaker(boolean enable);

    /**
     * 打开麦克风
     *
     * @param enable true打开麦克风，false关闭麦克风
     * @return 0成功，其他值失败
     */
    public native static int EnableMike(boolean enable);

    /**
     * 客户端看实施视频时抓拍
     * 2.4.6增加local参数, 为true时截取本地摄像头, false 时截取远程视频
     *
     * @return Bitmap 数据
     */
    public native static byte[] TakeSnapShot(boolean local);


    /**
     * 摄像头启用媒体服务，将自动打开摄像头，然后需要手动调用StartRenderLocalView 来看试图
     *
     * @param audioEncodeType 音频编码 0 ： opus， 1： pcmu，2: g729 默认 0
     * @param videoEncodeType 音频编码 0 : VP8, 1 : H264,默认1
     */
    public native static void StartMediaService(int audioEncodeType,
                                                int videoEncodeType);

    /**
     * 设置默认录像配置。
     *
     * @param width        分辨率：640, Android 默认值
     * @param height       分辨率：480, Android 默认值
     * @param startBitrate 初始码率：300， Android 默认值
     * @param minBitrate   最小码率：30, Android 默认值
     * @param maxBitrate   最大码率：500, Android 默认值
     * @param fps          帧率： 输入10， Android 默认值
     * @param videoRorate  录像显示方向。0,180  目录只支持。
     */
    public native static void SetDefaultVideoParameter(
            int width, int height, int startBitrate,
            int minBitrate, int maxBitrate, int fps, int videoRorate);

    /**
     * 关闭摄像头
     * 当摄像头输出帧数为0的时候，先停掉再调用startmediaservice
     */
    public native static void StopCamera();

    /**
     * 摄像头端打开摄像头
     */
    public native static void StartCamera();

    /**
     * 摄像头开始渲染本地视频
     *
     * @param view Surface 视图对象
     */
    public native static void StartRendeLocalView(Object view);

    /**
     * 客户端开始渲染远程视频
     *
     * @param view Surface 视图对象
     *             渲染远端图像, 增加callid 参数. 此参数是为了支持 单个客户端,看多个摄像头的功能,其中callid用来标示每个不同的摄像头连接
     */
    public native static void StartRendeRemoteView(int callid, Object view);

    /**
     * 摄像头停止渲染本地视频，一般是视图被遮盖时调用
     */
    public native static void StopRendeLocalView();

    /**
     * 客户端停止渲染远程视频，一般是视图被遮盖时调用
     */
    public native static void StopRendeRemoteView();

    /**
     * 摄像头设置本地摄像头旋转角度，一般为0或180
     *
     * @param degree 旋转角度
     */
    public native static void SetRotate(int degree);

    /**
     * 客户端播放在线视频地址，比如rtmp://192.168.1.1/live/200000000001
     *
     * @param url 视频地址
     */
    public native static void Play(String url);

    /**
     * 摄像头开启硬编码
     *
     * @param enable true开启硬编码，false关闭硬编码
     */
    public native static void EnableHWEncoder(boolean enable);

    /**
     * 设置录像目录
     *
     * @param dir 录像目录
     */
    public native static void SetVideoDir(String dir);

    /**
     * 摄像头开启/关闭 录像
     *
     * @param enable true 开启录像，false关闭录像
     */
    public native static void EnableRecord(boolean enable);

    /**
     * 摄像头检测是否在录像中
     *
     * @return
     */
    public native static boolean IsRecording();

    /**
     * 摄像头直接发送视频到目的地址
     *
     * @param ip   目的IP
     * @param port 目的端口
     */
    public native static void StartFactoryMediaSend(String ip, int port);

    /**
     * 摄像头停止发送视频
     */
    public native static void StopFactoryMediaSend();

    /**
     * 摄像头启动UDP广播相关
     *
     * @param obj      回调函数，需要实现 OnFactoryMessage(String ip, int port, byte[] bytes);
     * @param isCamera 区分摄像头与客户端 modify on 2015/10/28
     */
    public native static void StartFactoryWorker(Object obj, boolean isCamera);

    /**
     * 发送Factory相关消息
     *
     * @param ip    目的IP，维持和OnFactoryMessage调用一致
     * @param port  目的端口，维持和OnFactoryMessage调用一致
     * @param bytes msgpack打包的消息内容
     */
    public native static void FactorySendMessage(String ip, int port, byte[] bytes);

    /**
     * 记录日志到smartCall_t.txt
     *
     * @param log 日志字串
     */
    public native static void LogMsg(String log);

    /**
     * 开启/关闭日志
     *
     * @param enable true 开启，false 关闭
     */
    public native static void EnableLog(boolean enable);

    /**
     * 开启移动侦测
     *
     * @param enable    开关
     * @param threshold 灵敏度 2.0左右. 越大灵敏度越低。
     */
    public native static void EnableMDAlarm(boolean enable, float threshold);

    /**
     * 设置心跳间隔
     *
     * @param time 间隔秒数。
     */
    public native static void SetHeartbeatInterval(int time);


    /**
     * 用于局域网呼叫狗
     *
     * @param port 是本地开放的音视频端口接收端口
     */
    public native static void StartFactoryMediaRecv(int port);


    /**
     * 解密
     *
     * @param input 需要解密的数据
     * @param key   密钥
     * @return 解密结果
     */
    public native static byte[] AESDecrypt(byte[] input, byte[] key);


    /**
     * 2.4.6
     * 配合StartRendeRemoteView 功能使用, 其中peer为对端标示,CID
     */
    public native static int GetCallid(String peer);

    /**
     * 2.4.6
     * 指定是否开启前置摄像头, 如果只有一个摄像头,参数会忽略.相应JNI接口也已更新.
     *
     * @param front
     */
    public native static void StartCamera(boolean front);

    /**
     * 客户端/狗 在登录成功之后需要调用
     *
     * @param userName 用于呼叫时获取对端账号名称
     * @param session
     */
    public native static void SetLocalID(String userName, String session);

    /**
     * 控制rtcp日志开关.
     *
     * @param enable
     */
    public native static void EnableRTCPLog(boolean enable);

    /**
     * 获取smartCall 代码commit id
     *
     * @return commit id
     */
    public native static String GetVersion();

}
