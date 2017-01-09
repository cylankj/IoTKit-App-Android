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


    // 设备端鉴权
    // 厂家CID达到配额。关联消息：注册。
    public static final int ECIDExceedQuota = 140;

    // SN签名验证失败。关联消息：登陆。
    public static final int ErrorCIDSNVerifyFailed = 141;


    // 客户端登陆类.
    // vid, bundleID, vkey校验失败。
    public static final int ErrorLoginInvalidVKey = 160;

    // 帐号或密码错误。
    public static final int ErrorLoginInvalidPass = 161;


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

    //  此手机号码已被绑定。关联消息：帐号、手机号、邮箱绑定。
    public static final int ErrorPhoneExist = 186;

    // 此邮箱已被绑定。关联消息：帐号、手机号、邮箱绑定。
    public static final int ErrorEmailExist = 187;

    // 手机号码不合规
    public static final int ErrorIsNotPhone = 188;

    // 邮箱账号不合规
    public static final int ErrorIsNotEmail = 189;


    // 客户端绑定设备类.
    // CID不存在。关联消息：客户端绑定。
    public static final int ErrorCIDNotExist = 200;

    // 绑定中，正在等待摄像头上传随机数与CID关联关系，随后推送绑定通知
    public static final int ErrorCIDBinding = 201;

    // 设备别名已存在。
    public static final int ErrorCIDAliasExist = 202;


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
    // 添加好友失败 对方账户未注册
    public static final int ErrorFriendInvalidAccount = 240;

    // 已经是好友关系
    public static final int ErrorFriendAlready = 241;

    // 不能添加自己为好友
    public static final int ErrorFriendToSelf = 242;

    // 好友请求消息过期
    public static final int ErrorFriendInvalidRequest = 243;


    // 获取临时安全凭证
    // 获取失败
    public static final int ErrorGetCredentials = 260;


    // APP 侧错误号
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

}
