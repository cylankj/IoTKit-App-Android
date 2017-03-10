package com.cylan.jiafeigou.misc;

/**
 * Created by cylan-hunt on 16-10-24.
 */

public class JError {


    // EOK 成功
    public static final int ErrorOK = 0;

// P2P 错误码

    public static final int ErrorP2PDns = 1;

    public static final int ErrorP2PSocket = 2;

    public static final int ErrorP2PCallerRelay = 3;

    public static final int ErrorP2PCallerStun = 4;

    public static final int ErrorP2PCalleeStun = 5;

    public static final int ErrorP2PCalleeWaitCallerCheckNetTimeOut = 6;

    public static final int ErrorP2PPeerTimeOut = 7;

    public static final int ErrorP2PUserCancel = 8;

    public static final int ErrorP2PConnectionCheck = 9;

    public static final int ErrorP2PChannel = 10;

    public static final int ErrorP2PDisconetByUser = 11;

    public static final int ErrorP2PUnKnown12 = 12;

    public static final int ErrorP2PRTCPTimeout = 13;


    // 对端不在线
    public static final int ErrorVideoPeerNotExist = 100;

    // 对端断开
    public static final int ErrorVideoPeerDisconnect = 101;

    // 正在查看中
    public static final int ErrorVideoPeerInConnect = 102;

    // 本端未登陆
    public static final int ErrorVideoNotLogin = 103;

    // 通用类
// 未知错误
    public static final int ErrorUnknown = 120;

    // 数据库错误
    public static final int ErrorDataBase = 121;

    // 未登录或无效的会话，客户端和设备端通用
    public static final int ErrorInvalidSession = 122;

    // 消息格式错误
    public static final int ErrorInvalidMsg = 123;

    // 消息速率超过限制，请控制合理流速（100个每秒）
    public static final int ErrorMsgRateExceedLimit = 124;


    // 设备端鉴权
// 厂家CID达到配额，请到萝卜头平台申请配额。关联消息：注册。
    public static final int ErrorCIDExceedQuota = 140;

    // SN签名验证失败， sn、signature及公钥不匹配。 关联消息：登陆。
    public static final int ErrorCIDSNVerifyFailed = 141;

    // 公钥不存在, 请到萝卜头平台上传您的公钥（注意，请保管好您的私钥，不要泄漏）。关联消息：登陆。
    public static final int ErrorPublicKeyNotExist = 142;

    // CID重复。关联消息：登陆。
    public static final int ErrorCIDIsDuplicate = 143;


    // 客户端登陆类.
// vid, bundleID, vkey校验失败。
    public static final int ErrorLoginInvalidVKey = 160;

    // 帐号或密码错误。
    public static final int ErrorLoginInvalidPass = 161;

    // 第三方帐号登陆： access_token 验证失败。
    public static final int ErrorOpenLoginInvalidToken = 162;

    // SDK正在初始化，请等待
    public static final int ErrorIniting = 163;

    public static final int LoginTimeOut = 1;

    public static final int StartLoginPage = 2;

    public static final int NoNet = 3;

    // 客户端帐号类.
// 短信验证码错误。
    public static final int ErrorSMSCodeNotMatch = 180;

    // 短信验证码超时。
    public static final int ErrorSMSCodeTimeout = 181;
    // 帐号不存在。
    public static final int ErrorAccountNotExist = 182;

    // 帐号已存在。
    public static final int ErrorAccountAlreadyExist = 183;

    // 原始密码与新密码相同。关联消息：修改密码。
    public static final int ErrorSamePass = 184;

    // 原密码错误。关联消息：修改密码。
    public static final int ErrorInvalidPass = 185;

    // 此手机号码已被绑定。关联消息：帐号、手机号、邮箱绑定。
    public static final int ErrorPhoneExist = 186;

    // 此邮箱已被绑定。关联消息：帐号、手机号、邮箱绑定。
    public static final int ErrorEmailExist = 187;

    // 手机号码不合规
    public static final int ErrorIsNotPhone = 188;

    // 邮箱账号不合规
    public static final int ErrorIsNotEmail = 189;

    // 忘记密码时，邮箱或手机号不存在时报错
    public static final int ErrorInvalidPhoneNumber = 190;

    // 第三方账号设置密码超时
    public static final int ErrorSetPassTimeout = 191;

    // 十分钟内获取验证码超过3次
    public static final int ErrorGetCodeTooFrequent = 192;


    // 客户端绑定设备类.
// CID不存在。关联消息：客户端绑定。
    public static final int ErrorCIDNotExist = 200;

    // 绑定中，正在等待摄像头上传随机数与CID关联关系，随后推送绑定通知
    public static final int ErrorCIDBinding = 201;

    // 设备别名已存在。
    public static final int ErrorCIDAliasExist = 202;

    // 设备未绑定，不可操作未绑定设备。
    public static final int ErrorCIDNotBind = 203;

    // 设备已经被其他账号绑定。
    public static final int ErrorCIDBinded = 204;


    // 客户端分享设备类.
// 此帐号还没有注册。
    public static final int ErrorShareInvalidAccount = 220;

    // 此帐号已经分享。
    public static final int ErrorShareAlready = 221;

    // 您不能分享给自己。
    public static final int ErrorShareToSelf = 222;

    // 设备分享，被分享账号不能超过5个。
    public static final int ErrorShareExceedsLimit = 223;


    // 客户端亲友关系类.
// 已经是好友关系
    public static final int ErrorFriendAlready = 241;

    // 不能添加自己为好友
    public static final int ErrorFriendToSelf = 242;

    // 好友请求消息过期
    public static final int ErrorFriendInvalidRequest = 243;


    // 云存储 获取临时安全凭证
// 获取失败
    public static final int ErrorGetCredentials = 260;


    // APP 端错误号
// 非法的调用，ex: 摄像头/APP 调用对方才有的功能
    public static final int ErrorInvalidMethod = 1000;

    // 非法的调用参数，ex: 登陆不带用户名 
    public static final int ErrorInvalidParameter = 1001;

    // 非法的状态， ex: 和摄像头在连接状态再次调用连接
    public static final int ErrorInvalidState = 1002;

    // 解析域名失败
    public static final int ErrorResolve = 1003;

    // 连接服务器失败
    public static final int ErrorConnect = 1004;


    // DP 操作错误号
// 每日精彩收藏夹达到上线（50条）
    public static final int ErrorWonderFavoriteExceedLimit = 1050;

    //SD卡错误码

    // 未知错误
    public static final int ErrorSDUnknown = 2001;

    // 输入参数有误
    public static final int ErrorSDInvParam = 2002;

    // 没有空闲空间
    public static final int ErrorSDNoSpace = 2003;

    // 没有可用的存储设备
    public static final int ErrorSDNoDevice = 2004;

    // 要写入的帧数据长度过长，簇中放不下
    public static final int ErrorSDTooLarge = 2005;

    // 没有记录
    public static final int ErrorSDNoRecords = 2006;

    // 录像中不能进行一些操作
    public static final int ErrorSDRecording = 2007;

    // 格式化过程中
    public static final int ErrorSDFormating = 2008;

    // 写失败
    public static final int ErrorSDWrite = 2009;

    // 内存申请失败
    public static final int ErrorSDNoMemory = 2010;

    // 读失败
    public static final int ErrorSDRead = 2011;

    // 检索/读取过程中不能进行一些操作
    public static final int ErrorSDOperating = 2012;

    // 列表检索过程中不能进行一些操作
    public static final int ErrorSDListSearching = 2013;

    // 已存在句柄
    public static final int ErrorSDExistHandle = 2014;

    // 要写入的帧pts异常
    public static final int ErrorSDInvPTS = 2015;

    // 存储设备上的文件系统过旧
    public static final int ErrorSDFSVersionOld = 2020;

    // 存储设备上的文件系统较新
    public static final int ErrorSDFSVersionNew = 2021;

    // 文件系统无法识别
    public static final int ErrorSDFSDamaged = 2022;

    // 存储设备读写出错
    public static final int ErrorSDFSReadWrite = 2023;

    // 未正常关闭存储设备(需要进行断电恢复)
    public static final int ErrorSDFSDirty = 2024;

    // 文件系统未初始化或已关闭
    public static final int ErrorSDFSInitialized = 2025;

    // 文件系统索引块异常(需要进行数据恢复)
    public static final int ErrorSDFIDXAbnormal = 2026;

    // 历史录像已读完
    public static final int ErrorSDHistoryAll = 2030;

    // 历史录像读取失败
    public static final int ErrorSDFileIO = 2031;

    // 历史录像卡读取失败,同 public static final int ErrorSDRead
    public static final int ErrorSDIO = 2032;


}
