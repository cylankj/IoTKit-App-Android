package com.cylan.jiafeigou.server

import org.msgpack.core.MessageBufferPacker
import org.msgpack.core.MessagePack
import org.msgpack.core.MessageUnpacker

/**
 * Created by yanzhendong on 2017/8/19.
 */

object MIDServerAPI {

    val packer: ThreadLocal<MessageBufferPacker>  by lazy {
        object : ThreadLocal<MessageBufferPacker>() {
            override fun initialValue() = MessagePack.newDefaultBufferPacker()
        }
    }

    fun getPacker(): MessageBufferPacker {
        val bufferPacker = packer.get()
        bufferPacker.flush()
        return bufferPacker
    }

    private val unpacker: ThreadLocal<MessageUnpacker>  by lazy {
        object : ThreadLocal<MessageUnpacker>() {
            override fun initialValue() = MessagePack.newDefaultUnpacker(byteArrayOf())
        }
    }


    fun sendMessage(msgId: Int, caller: String = "", callee: String = "", seq: Long, bytes: ByteArray = byteArrayOf()) {
        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(5)
                .packInt(msgId)
                .packString(caller)
                .packString(callee)
                .packLong(seq)
                .writePayload(bytes)

        val byteArray = bufferPacker.toByteArray()

        TODO("发送到服务器的逻辑")
    }

//    MIDHistoryList=2514

//    int,      timeBegin    查询开始时间点
//    int,      search_way   查询方式：search_way_bymin = 0 按分钟查(响应数据的粒度为分钟)； search_way_byday = 1 按天查（响应数据的粒度为天）；
//    int,      search_num

    fun MIDHistoryList(caller: String = "", callee: String = "", seq: Long, timeBegin: Int, search_way: Int, search_num: Int) {
        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packInt(timeBegin)
                .packInt(search_way)
                .packInt(search_num)

        val byteArray = bufferPacker.toByteArray()
        sendMessage(2514, caller, callee, seq, byteArray)
    }

    /**
    检测升级，摄像头，中控，PC客户端共用
    [
    uint32,   pid             os 升级为pid
    string,   version
    string,   cid
    ]
     */
    fun MIDCheckVersion(caller: String = "", callee: String = "", seq: Long, pid: Int, version: String, cid: String) {
        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packInt(pid)
                .packString(version)
                .packString(cid)

        val byteArray = bufferPacker.toByteArray()
        sendMessage(3204, caller, callee, seq, byteArray)
    }

    /**
     * MIDGetURLByOSVersionReq=3206
     *门铃的摄像头主板, 根据型号和版本号获取包的下载地址
    [
    uint32,          pid             os 升级为pid
    string,          version
    ]
     * */
    fun MIDGetURLByOSVersionReq(caller: String = "", callee: String = "", seq: Long, pid: Int, version: String) {
        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packInt(pid)
                .packString(version)

        val byteArray = bufferPacker.toByteArray()
        sendMessage(3206, caller, callee, seq, byteArray)
    }

    /**
     * MIDCidGetCredentialsReq=3208

    3.0 版本使用该接口
    设备端获取临时安全凭证的读写权限。
     *
     */
    fun MIDCidGetCredentialsReq(caller: String = "", callee: String = "", seq: Long) {

        sendMessage(3208, caller, callee, seq)
    }

    /**
     *MIDCidCheckVersionPartsReq=3210

    分包升级检测版本。


    [
    int64    seq    // 此参数为兼容参数；
    1. 使用2.0消息号（LOGIN = 100）登录的设备/客户端特有。填唯一随机数，唯一标识该消息。
    2. 使用3.0消息号（MIDClientLoginReq(3.0)=
    3. ， MIDClientOpenLoginReq(3.0)=
    4. ，MIDRobotCIDLogin=20102）
    登录的设备/客户端不能有此参数，因为3.0版本消息头header已经定义了该参数。
    int      pid    // 设备类型
    array    lParts //设备当前各区的版本信息数组
    string   cid
    ]


    lPart元素
    [
    int      partType      //linux设备端 DOG-2W：Uboot包，Kernel包，File system包，Program包，Config包。
    string   version       // 当前版本
    ]
     * */

    fun MIDCidCheckVersionPartsReq(caller: String = "", callee: String = "", seq: Long, pseq: Long, pid: Int, partType: Int, version: String, cid: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(4)
                .packLong(pseq)
                .packInt(pid)
                .packArrayHeader(2).packInt(partType).packString(version)
                .packString(cid)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(3210, caller, callee, seq, byteArray)

    }

    /**
     *MIDCidGetOSSApiUrlReq=3212

    无参数


    # 基础平台处理：负责到萝卜头平台请求授权URL

    ## 萝卜头平台 OSSAPI 接口说明

    ### 请求方式：
    http post

    ### 请求示例：
    https://[apiyf|apitest|api].robotscloud.com/oss/v1/get_upload_url?service_key=ooaH6CPNxib8MwTGtcTGGM3pYfc8Slhn&vid=000F&sn=200000000001&account=18927486726

    ### 响应示例:
    {"ret":0,"msg":"ok","duration":5,"url":"rtmp://dyf.robotscloud.com/live/7day-asdfasdf?OSSAccessKeyId=LTAIDFLl7c8DOk96&playlistName=playlist.m3u8&Expires=1492417571&Signature=Pq%2FTGzUkYvN%2Fg72EWOz8g3bvujs%3D"}
     * */

    fun MIDCidGetOSSApiUrlReq(caller: String = "", callee: String = "", seq: Long) {
        sendMessage(3212, caller, callee, seq)
    }

    /**
     * MIDCidGetDSTReq=3214

    server端处理：
    server根据数据库存储的timezone查询夏令时的生效时间段，该timezone由客户端配置生成（绑定时或用户手动修改）。

    设备端处理：
    1 如果 timezone 有变化，主动调用该接口更新夏令时。
    2 请做好本地缓存。如无变动，一个夏令时段取一次就妥了！！




    [
    int64    seq    // 此参数为兼容参数；
    1. 使用2.0消息号（LOGIN = 100）登录的设备/客户端特有。填唯一随机数，唯一标识该消息。
    2. 使用3.0消息号（MIDClientLoginReq(3.0)=16120， MIDClientOpenLoginReq(3.0)=16130，MIDRobotCIDLogin=20102）
    登录的设备/客户端不能有此参数，因为3.0版本消息头header已经定义了该参数。
    string   timezone  // 时区英文关键字
    ]
     * */

    fun MIDCidGetDSTReq(caller: String = "", callee: String = "", seq: Long, pseq: Long, timezone: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packLong(pseq)
                .packString(timezone)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(3214, caller, callee, seq, byteArray)
    }

    /**
     * MIDClientRegisterReq(3.0)=16000

    3.0消息，旧版消息1005


    [
    int,    language_type
    string, account
    string, pass
    int,    register_type             如果手机号注册，需要填充token。其他方式则不需要。
    string, token         //消息16003中的token
    string, vid
    string, vkey
    ]
     * */

    fun MIDClientRegisterReq(caller: String = "", callee: String = "", seq: Long, language_type: Int, account: String, pass: String, register_type: Int, token: String, vid: String, vkey: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(7)
                .packInt(language_type)
                .packString(account)
                .packString(pass)
                .packInt(register_type)
                .packString(token)
                .packString(vid)
                .packString(vkey)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16000, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientGetCodeReq(3.0)=16002

    3.0消息 旧版消息1001


    [
    int,     language_type
    string,  account
    int,     type         操作类型：忘记密码，修改密码，注册
    string,  vid
    string,  vkey

    ]

    type定义：
    const.CLIENT_SMS_TYPE_REGISTER=         0
    const.CLIENT_SMS_TYPE_FORGETPASS=       1
    const.CLIENT_SMS_TYPE_EDIT_USERINFO=    2
     * */

    fun MIDClientGetCodeReq(caller: String = "", callee: String = "", seq: Long, language_type: Int, account: String, type: Int, vid: String, vkey: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(5)
                .packInt(language_type)
                .packString(account)
                .packInt(type)
                .packString(vid)
                .packString(vkey)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16002, caller, callee, seq, byteArray)

    }

    /**
     * MIDClientCheckCodeReq(3.0)=16004

    旧版消息1003


    [
    Account  string
    Code     string
    Token    string
    string,  vid
    string,  vkey
    ]
     * */

    fun MIDClientCheckCodeReq(caller: String = "", callee: String = "", seq: Long, account: String, code: String, token: String, vid: String, vkey: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(5)
                .packString(account)
                .packString(code)
                .packString(token)
                .packString(vid)
                .packString(vkey)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16004, caller, callee, seq, byteArray)

    }

    /**
     * MIDClientSetPassReq(3.0)=16006

    3.0消息，旧版消息1007，忘记密码


    [
    string,  account
    string,  password
    string,  code
    string,  vid
    string,  vkey
    ]
     * */

    fun MIDClientSetPassReq(caller: String = "", callee: String = "", seq: Long, account: String, password: String, code: String, vid: String, vkey: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(5)
                .packString(account)
                .packString(password)
                .packString(code)
                .packString(vid)
                .packString(vkey)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16006, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientChangePassReq(3.0)=16008

    3.0消息，旧版消息1010，修改密码


    [
    string,   account
    string,   password
    string,   newPass
    ]
     * */

    fun MIDClientChangePassReq(caller: String = "", callee: String = "", seq: Long, account: String, password: String, newPass: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packString(account)
                .packString(password)
                .packString(newPass)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16008, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientForgetPassByEmailReq=16010

    旧版消息1060


    [
    int,      language_type
    string,   account
    string,   vid
    ]
     * */

    fun MIDClientForgetPassByEmailReq(caller: String = "", callee: String = "", seq: Long, language_type: Int, account: String, vid: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packInt(language_type)
                .packString(account)
                .packString(vid)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16010, caller, callee, seq, byteArray)
    }

    /**
     * MIDClientGetAccountRegisterStatusReq=16012

    3.0消息，指定账号是否注册，可在未登录调用


    [
    string,   account
    ]
     * */

    fun MIDClientGetAccountRegisterStatusReq(caller: String = "", callee: String = "", seq: Long, account: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(account)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16012, caller, callee, seq, byteArray)
    }

    /**
     *DefClientMsgIDByReqForLogout=16100

    Request消息段，登出



    MIDClientLogout=16100

    旧版消息1091


    2015.9.17
    2.4.5
    客户端退出登录请求，无消息体
     * */

    fun DefClientMsgIDByReqForLogout(caller: String = "", callee: String = "", seq: Long) {
        sendMessage(16100, caller, callee, seq)
    }

    /**
     *DefClientMsgIDByReqForLogin=16120

    Request消息段，登录



    MIDClientLoginReq(3.0)=16120

    3.0消息，旧版消息1012
    原登录接口拆分成两个接口: 16120和15501(MIDClientReportDeviceInfo)


    MIDClientReportDeviceInfo(3.0)=15501

    [
    int,    language_type
    string, account
    string, pass
    uint32, pid                       os 升级为pid
    int,    net
    string, name
    string, bundleId                  2015.8.27 如果是ios登录，该字段是证书唯一标识，例如：com.jfgou.push；
    2015.11.6 如果是android登陆，则是发布版的签名（旧版该字段为空）。发布版的签名为（8761570c1334bfb9d4bf45100f177278）。
    string, device_token              2015.8.27 如果是ios登陆，则是apns token；
    2015.11.6 如果是android登录，则是设备唯一码IMEI。 2016.5.3 支持GCM推送：海外地区取到google唯一码，则上发。国内保持不变。
    string, sessid                    上次登录的sessid,服务器做清理session动作
    string, vid                       厂家VID值 - 由萝卜头平台签发 2016.4.20
    string, vkey
    ]

    说明：oem通过vid实现.
    sessid 分配逻辑：判断本次与上次登录的时间差，如超过1小时，重新分配新sessid，清理旧的sessid； 如果小于等于1小时，保持sessid不变。
     * */

    fun DefClientMsgIDByReqForLogin(caller: String = "", callee: String = "", seq: Long, language_type: Int, account: String, pass: String, pid: Int, net: Int, name: String, bundleId: String, deviceToken: String, sessid: String, vid: String, vkey: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(11)
                .packInt(language_type)
                .packString(account)
                .packString(pass)
                .packInt(pid)
                .packInt(net)
                .packString(name)
                .packString(bundleId)
                .packString(deviceToken)
                .packString(sessid)
                .packString(vid)
                .packString(vkey)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16120, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientOpenLoginReq(3.0)=16130

    旧版消息1107


    #2.4.6  2015.12.30  zll
    #sdk登录 - 第三方认证接口
    #响应复用 CLIENT_LOGIN_RSP

    [
    int,    language_type             语言
    string, open_id                   唯一用户标识 - 第三方
    string, access_token              访问凭证  - 第三方
    uint32, pid                       os 升级为pid  手机系统类型 0是iOS 1是PC 2是Android
    int,    net                       网络类型：1是WiFi 2是3G网络
    string, name                      网络名称
    string, bundleId                  2015.8.27 如果是ios登录，该字段是证书唯一标识，例如：com.jfgou.push；
    2015.11.6 如果是android登陆，则是发布版的签名（旧版该字段为空）。发布版的签名为（8761570c1334bfb9d4bf45100f177278）。
    注：android签名不需验证。
    string, device_token              2015.8.27 如果是ios登陆，则是apns token；
    2015.11.6 如果是android登录，则是设备唯一码IMEI。
    string, sessid                    上次登录sessid
    string, vid                       厂家VID值 - 由萝卜头平台签发 2016.4.20， zll， 萝卜头支持_2.4.9
    string, vkey                      2016.4.19 zll
    int,    loginType                 3-QQ登录 4-新浪微博登录 5-萝卜头用户自定义账号系统登录
    ]
     * */

    fun MIDClientOpenLoginReq(caller: String = "", callee: String = "", seq: Long, language_type: Int, open_id: String, access_token: String, pid: Int, net: Int, name: String, bundleId: String, deviceToken: String, sessid: String, vid: String, vkey: String, loginType: Int) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(12)
                .packInt(language_type)
                .packString(open_id)
                .packString(access_token)
                .packInt(pid)
                .packInt(net)
                .packString(name)
                .packString(bundleId)
                .packString(deviceToken)
                .packString(sessid)
                .packString(vid)
                .packString(vkey)
                .packInt(loginType)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16130, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientBindCidReq=16200



    旧版消息1016


    新版随机数绑定的消息定义

    callee填充为cid
    [
    string, random 20160617 随机数绑定
    string, mac    2.* 旧版设备需要提交mac地址（防止工厂将cid刷重复）
    int,    is_rebind  根据客户端的参数值执行逻辑。1 强绑；0 不强绑
    ]

    3.0客户端 - 旧版狗， 客户端需要将callee填充为cid，服务器根据该字段和mac地址实现绑定关系。

    3.0客户端 - 新版狗（设备被初次绑定，无cid），客户端需要将random填充为有效值，服务器根据该字段实现绑定关系。
    3.0客户端 - 新版狗（设备被重复绑定，已有cid），客户端填充callee或random均可，需要iOS和Android统一方式, 服务器均可实现绑定关系。




    对应设备端消息：MIDRobotCidRandom=20104

    多app端同步消息：MIDClientPushRefresh=15008 消息体为：MIDClientCidListReq。

    重复绑定本账号下的设备，服务器不做清理报警图片消息等动作； 强制绑定其他帐号下的设备，服务器做清理动作。
     * */

    fun MIDClientBindCidReq(caller: String = "", callee: String = "", seq: Long, random: String, mac: String, isRebind: Int) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packString(random)
                .packString(mac)
                .packInt(isRebind)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16200, caller, callee, seq, byteArray)
    }

    /**
     * MIDClientUnBindCidReq=16202

    旧版消息1018


    callee填充为cid
    [
    string,  cid
    ]


    多app端同步消息：MIDClientPushRefresh=15008 消息体为：MIDClientCidListReq。

    解除绑定设备，服务器做清理报警图片消息等动作。
     * */

    fun MIDClientUnBindCidReq(caller: String = "", callee: String = "", seq: Long, cid: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(cid)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16202, caller, callee, seq, byteArray)

    }

    /**
     * MIDClientCidListReq(3.0)=16204

    3.0消息 旧版消息1028


    [
    int64     timestamp //客户端缓存的设备列表变更时间
    ]
     * */

    fun MIDClientCidListReq(caller: String = "", callee: String = "", seq: Long, timestamp: Long) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packLong(timestamp)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16204, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientSetCidAliasReq=16212

    旧版消息1020


    3.0 结构体优化，cid字段复用callee。
    [
    Alias string
    ]
     * */

    fun MIDClientSetCidAliasReq(caller: String = "", callee: String = "", seq: Long, alias: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(alias)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16212, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientGetCidAliasReq=16214

    旧版消息1022


    3.0 结构体优化，cid字段复用callee。
     * */

    fun MIDClientGetCidAliasReq(caller: String = "", callee: String = "", seq: Long) {
        sendMessage(16214, caller, callee, seq)
    }

    /**
     *MIDClientGetUnshareAccountByCidReq(3.0)=16216

    3.0消息，获取设备未分享的好友信息


    callee填充为cid
     * */

    fun MIDClientGetUnshareAccountByCidReq(caller: String = "", callee: String = "", seq: Long) {
        sendMessage(16216, caller, callee, seq)
    }

    /**
     * MIDClientCheckCidVersionReq=16220

    客户端检测设备最新包


    callee填充为设备cid


    无消息体
     * */

    fun MIDClientCheckCidVersionReq(caller: String = "", callee: String = "", seq: Long) {
        sendMessage(16220, caller, callee, seq)
    }

    /**
     *MIDClientGetAccountInfoReq(3.0)=16300

    旧版消息1024


    int64     timestamp //客户端缓存的账号信息变更时间
     * */

    fun MIDClientGetAccountInfoReq(caller: String = "", callee: String = "", seq: Long, timestamp: Long) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packLong(timestamp)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16300, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientSetAccountInfoReq(3.0)=16302

    旧版消息1026


    [
    flag         int                    二进制低位开始依次标记下面的参数。如：十进制7 二进制0b111 表示sms_phone、token、alias的值有效
    sms_phone    string                 短信接收号码
    token        string                 消息16003中的token,修改手机号码是才需要填
    alias        string                 昵称
    push_enable  int                    接收通知 0 关闭 1开启
    sound        int                    是否响铃通知
    email        string                 邮箱号码
    vibrate      int                    是否震动通知
    photo        int                    是否修改头像 0 否 1是
    weixin_open_id      string          用户账户绑定的微信openid
    wechat_push_flag    int             微信推送开关，新增字段：3.2.0 0：关闭，1：开启 其它为预留 20170615
    }


    weixin_open_id 使用说明
     * */

    fun MIDClientSetAccountInfoReq(caller: String = "", callee: String = "", seq: Long, flag: Int, smsPhone: String, token: String, alias: String, pushEnable: Int, sound: Int, email: String, vibrate: Int, photo: Int, weixin_open_id: String, wechat_push_flag: Int) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(11)
                .packInt(flag)
                .packString(smsPhone)
                .packString(token)
                .packString(alias)
                .packInt(pushEnable)
                .packInt(sound)
                .packString(email)
                .packInt(vibrate)
                .packInt(photo)
                .packString(weixin_open_id)
                .packInt(wechat_push_flag)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16302, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientSetTokenReq=16304

    旧版消息1083



    [
    string,       token               登录时，设置token为有效值，退出时，设置token为空（解决退出登录还收到消息的问题）
    string,       id                  ios， 则是证书的BundleID，例：com.jfgou.push； Android， 则是程序包名，例：com.cylan.jfgou。
    int,          servciceType        推送服务类型； 2017.5.10 3.2.0版本新增
    ]

    servciceTypeIosApns       = 1     苹果原生APNS服务, 64位token
    servciceTypeIosGetui      = 2     苹果手机，个推服务, 32位token

    servciceTypeAndroidHuawei = 10    Android 华为推送, 64位token
    servciceTypeAndroidGcm    = 11    Android Google推送, 152位token
    servciceTypeAndroidXiaomi = 12    Android 小米推送, 44位token

    servciceType 字段兼容旧版：
    旧版SDK接口不存在该字段或该值无效，server根据token长度处理。



    逻辑：
    ios: 服务器根据该字段查询对应的推送证书，推送消息到APNS（iOS登录时有处理bundleID和token）；
    Android: 1.对接华为推送； 服务器根据该字段查询对应的appid和appkey，推送消息到华为联盟。（Android登录时未处理，所以支持推送，该消息必须。）
     * */

    fun MIDClientSetTokenReq(caller: String = "", callee: String = "", seq: Long, token: String, id: String, servciceType: Int) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packString(token)
                .packString(id)
                .packInt(servciceType)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16304, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientGetAWSCredentialsReq=16306

    [
    int    regionType // 详见 DPIDCloudStorage
    ]


    regiontype及存储路径约束参考：DPIDCloudStorage = 3
     * */

    fun MIDClientSetTokenReq(caller: String = "", callee: String = "", seq: Long, regionType: Int) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packInt(regionType)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16306, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientSetEmailReq(3.0)=16308

    3.0消息，绑定/修改邮箱


    [
    string,   email
    ]
     * */

    fun MIDClientSetEmailReq(caller: String = "", callee: String = "", seq: Long, email: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(email)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16308, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientSetPhoneNumReq(3.0)=16310

    3.0消息，绑定/修改手机号码


    [
    string,   code     //短信验证码
    string,   phoneNum
    ]
     * */

    fun MIDClientSetPhoneNumReq(caller: String = "", callee: String = "", seq: Long, code: String, phoneNum: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packString(code)
                .packString(phoneNum)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16310, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientSetAliasReq(3.0)=16312

    3.0消息，修改昵称


    [
    string,   alias
    ]
     * */

    fun MIDClientSetAliasReq(caller: String = "", callee: String = "", seq: Long, alias: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(alias)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16312, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientGetAliasReq(3.0)=16314

    3.0消息，获取昵称
     * */

    fun MIDClientGetAliasReq(caller: String = "", callee: String = "", seq: Long) {
        sendMessage(16314, caller, callee, seq)
    }

    /**
     *MIDClient3rdSetPassReq(3.0)=16316

    3.0消息，第三方账号设置密码


    string, password
    int,    type                  0-绑定手机 1-绑定邮箱
    string, token                 消息16003中的token, 如果绑定手机号，需要填充
     * */

    fun MIDClient3rdSetPassReq(caller: String = "", callee: String = "", seq: Long, password: String, type: Int, token: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packString(password)
                .packInt(type)
                .packString(token)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16316, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientCheckVersionReq(3.0)=16318

    3.0消息，客户端检测版本
     * */

    fun MIDClientCheckVersionReq(caller: String = "", callee: String = "", seq: Long) {

        sendMessage(16318, caller, callee, seq)
    }

    /**
     * MIDClientGetVideoShareUrlReq(3.0)=16320

    3.0消息，客户端获取分享链接


    string, fileName
    string, content
    int,    ossType
    int,    shareType    //不填为 默认的分享h5； 1：720度全景图片分享； 2：720度全景视频分享
     * */

    fun MIDClientGetVideoShareUrlReq(caller: String = "", callee: String = "", seq: Long, fileName: String, content: String, ossType: Int, shareType: Int) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(4)
                .packString(fileName)
                .packString(content)
                .packInt(ossType)
                .packInt(shareType)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16320, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientGetAliSTSReq(3.0)=16322

    获取oss临时访问凭证


    int,    regionType
    string, cid          // cid or account
     * */

    fun MIDClientGetAliSTSReq(caller: String = "", callee: String = "", seq: Long, regionType: Int, cid: String) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packInt(regionType)
                .packString(cid)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16322, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientShareReq(3.0)=16700

    3.0消息，旧版消息1046


    [
    string,   cid
    string,   toAccount
    ]
     * */

    fun MIDClientShareReq(caller: String = "", callee: String = "", seq: Long, cid: String, toAccount: String) {

    }
}