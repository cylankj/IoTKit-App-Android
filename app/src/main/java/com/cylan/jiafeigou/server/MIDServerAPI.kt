package com.cylan.jiafeigou.server

import com.cylan.entity.jniCall.JFGDPMsg
import com.cylan.jiafeigou.n.base.BaseApplication
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

    fun sendMessage(msgId: Int? = 0, caller: String? = "", callee: String? = "", seq: Long? = 0, bytes: ByteArray? = byteArrayOf()) {
        val bufferPacker = getPacker()


        bufferPacker
                .packArrayHeader(5)
                .packInt(msgId ?: 0)
                .packString(caller ?: "")
                .packString(callee ?: "")
                .packLong(seq ?: 0)
                .writePayload(bytes ?: byteArrayOf())

        val byteArray = bufferPacker.toByteArray()

        TODO("发送到服务器的逻辑")
    }

    fun sendMessageWithReqList(msgId: Int?, caller: String?, callee: String?, seq: Long?, reqList: Array<ByteArray>?) {
        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(reqList?.size ?: 0)
                .apply { reqList?.forEach { writePayload(it) } }

        val byteArray = bufferPacker.toByteArray()

        sendMessage(msgId, caller, callee, seq, byteArray)
    }

//    MIDHistoryList=2514

//    int,      timeBegin    查询开始时间点
//    int,      search_way   查询方式：search_way_bymin = 0 按分钟查(响应数据的粒度为分钟)； search_way_byday = 1 按天查（响应数据的粒度为天）；
//    int,      search_num

    fun MIDHistoryList(caller: String?, callee: String?, seq: Long?, timeBegin: Int? = 0, search_way: Int? = 0, search_num: Int? = 0) {
        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packInt(timeBegin ?: 0)
                .packInt(search_way ?: 0)
                .packInt(search_num ?: 0)

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
    fun MIDCheckVersion(caller: String?, callee: String?, seq: Long?, pid: Int? = 0, version: String? = "", cid: String? = "") {
        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packInt(pid ?: 0)
                .packString(version ?: "")
                .packString(cid ?: "")

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
    fun MIDGetURLByOSVersionReq(caller: String?, callee: String?, seq: Long?, pid: Int? = 0, version: String? = "") {
        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packInt(pid ?: 0)
                .packString(version ?: "")

        val byteArray = bufferPacker.toByteArray()
        sendMessage(3206, caller, callee, seq, byteArray)
    }

    /**
     * MIDCidGetCredentialsReq=3208

    3.0 版本使用该接口
    设备端获取临时安全凭证的读写权限。
     *
     */
    fun MIDCidGetCredentialsReq(caller: String?, callee: String?, seq: Long?) {

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

    fun MIDCidCheckVersionPartsReq(caller: String?, callee: String?, seq: Long?, pseq: Long? = 0, pid: Int? = 0, partType: Int? = 0, version: String? = "", cid: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(4)
                .packLong(pseq ?: 0)
                .packInt(pid ?: 0)
                .packArrayHeader(2).packInt(partType ?: 0).packString(version ?: "")
                .packString(cid ?: "")

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

    fun MIDCidGetOSSApiUrlReq(caller: String?, callee: String?, seq: Long?) {
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

    fun MIDCidGetDSTReq(caller: String?, callee: String?, seq: Long?, pseq: Long? = 0, timezone: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packLong(pseq ?: 0)
                .packString(timezone ?: "")

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

    fun MIDClientRegisterReq(caller: String?, callee: String?, seq: Long?, language_type: Int? = 0, account: String? = "", pass: String? = "", register_type: Int? = 0, token: String? = "", vid: String? = "", vkey: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(7)
                .packInt(language_type ?: 0)
                .packString(account ?: "")
                .packString(pass ?: "")
                .packInt(register_type ?: 0)
                .packString(token ?: "")
                .packString(vid ?: "")
                .packString(vkey ?: "")

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

    fun MIDClientGetCodeReq(caller: String?, callee: String?, seq: Long?, language_type: Int? = 0, account: String? = "", type: Int? = 0, vid: String? = "", vkey: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(5)
                .packInt(language_type ?: 0)
                .packString(account ?: "")
                .packInt(type ?: 0)
                .packString(vid ?: "")
                .packString(vkey ?: "")

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

    fun MIDClientCheckCodeReq(caller: String?, callee: String?, seq: Long?, account: String? = "", code: String? = "", token: String? = "", vid: String? = "", vkey: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(5)
                .packString(account ?: "")
                .packString(code ?: "")
                .packString(token ?: "")
                .packString(vid ?: "")
                .packString(vkey ?: "")

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

    fun MIDClientSetPassReq(caller: String?, callee: String?, seq: Long?, account: String? = "", password: String? = "", code: String? = "", vid: String? = "", vkey: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(5)
                .packString(account ?: "")
                .packString(password ?: "")
                .packString(code ?: "")
                .packString(vid ?: "")
                .packString(vkey ?: "")

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

    fun MIDClientChangePassReq(caller: String?, callee: String?, seq: Long?, account: String? = "", password: String? = "", newPass: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packString(account ?: "")
                .packString(password ?: "")
                .packString(newPass ?: "")

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

    fun MIDClientForgetPassByEmailReq(caller: String?, callee: String?, seq: Long?, language_type: Int? = 0, account: String? = "", vid: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packInt(language_type ?: 0)
                .packString(account ?: "")
                .packString(vid ?: "")

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

    fun MIDClientGetAccountRegisterStatusReq(caller: String?, callee: String?, seq: Long?, account: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(account ?: "")

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

    fun DefClientMsgIDByReqForLogout(caller: String?, callee: String?, seq: Long?) {
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

    fun DefClientMsgIDByReqForLogin(caller: String?, callee: String?, seq: Long?, language_type: Int? = 0, account: String? = "", pass: String? = "", pid: Int? = 0, net: Int? = 0, name: String? = "", bundleId: String? = "", deviceToken: String? = "", sessid: String? = "", vid: String? = "", vkey: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(11)
                .packInt(language_type ?: 0)
                .packString(account ?: "")
                .packString(pass ?: "")
                .packInt(pid ?: 0)
                .packInt(net ?: 0)
                .packString(name ?: "")
                .packString(bundleId ?: "")
                .packString(deviceToken ?: "")
                .packString(sessid ?: "")
                .packString(vid ?: "")
                .packString(vkey ?: "")

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

    fun MIDClientOpenLoginReq(caller: String?, callee: String?, seq: Long?, language_type: Int? = 0, open_id: String? = "", access_token: String? = "", pid: Int? = 0, net: Int? = 0, name: String? = "", bundleId: String? = "", deviceToken: String? = "", sessid: String? = "", vid: String? = "", vkey: String? = "", loginType: Int? = 0) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(12)
                .packInt(language_type ?: 0)
                .packString(open_id ?: "")
                .packString(access_token ?: "")
                .packInt(pid ?: 0)
                .packInt(net ?: 0)
                .packString(name ?: "")
                .packString(bundleId ?: "")
                .packString(deviceToken ?: "")
                .packString(sessid ?: "")
                .packString(vid ?: "")
                .packString(vkey ?: "")
                .packInt(loginType ?: 0)

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

    fun MIDClientBindCidReq(caller: String?, callee: String?, seq: Long?, random: String? = "", mac: String? = "", isRebind: Int? = 0) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packString(random ?: "")
                .packString(mac ?: "")
                .packInt(isRebind ?: 0)

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

    fun MIDClientUnBindCidReq(caller: String?, callee: String?, seq: Long?, cid: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(cid ?: "")

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

    fun MIDClientCidListReq(caller: String?, callee: String?, seq: Long?, timestamp: Long?) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packLong(timestamp ?: 0)

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

    fun MIDClientSetCidAliasReq(caller: String?, callee: String?, seq: Long?, alias: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(alias ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16212, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientGetCidAliasReq=16214

    旧版消息1022


    3.0 结构体优化，cid字段复用callee。
     * */

    fun MIDClientGetCidAliasReq(caller: String?, callee: String?, seq: Long?) {
        sendMessage(16214, caller, callee, seq)
    }

    /**
     *MIDClientGetUnshareAccountByCidReq(3.0)=16216

    3.0消息，获取设备未分享的好友信息


    callee填充为cid
     * */

    fun MIDClientGetUnshareAccountByCidReq(caller: String?, callee: String?, seq: Long?) {
        sendMessage(16216, caller, callee, seq)
    }

    /**
     * MIDClientCheckCidVersionReq=16220

    客户端检测设备最新包


    callee填充为设备cid


    无消息体
     * */

    fun MIDClientCheckCidVersionReq(caller: String?, callee: String?, seq: Long?) {
        sendMessage(16220, caller, callee, seq)
    }

    /**
     *MIDClientGetAccountInfoReq(3.0)=16300

    旧版消息1024


    int64     timestamp //客户端缓存的账号信息变更时间
     * */

    fun MIDClientGetAccountInfoReq(caller: String?, callee: String?, seq: Long?, timestamp: Long? = 0) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packLong(timestamp ?: 0)

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

    fun MIDClientSetAccountInfoReq(caller: String?, callee: String?, seq: Long?, flag: Int? = 0, smsPhone: String? = "", token: String? = "", alias: String? = "", pushEnable: Int? = 0, sound: Int? = 0, email: String? = "", vibrate: Int? = 0, photo: Int? = 0, weixin_open_id: String? = "", wechat_push_flag: Int? = 0) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(11)
                .packInt(flag ?: 0)
                .packString(smsPhone ?: "")
                .packString(token ?: "")
                .packString(alias ?: "")
                .packInt(pushEnable ?: 0)
                .packInt(sound ?: 0)
                .packString(email ?: "")
                .packInt(vibrate ?: 0)
                .packInt(photo ?: 0)
                .packString(weixin_open_id ?: "")
                .packInt(wechat_push_flag ?: 0)

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

    fun MIDClientSetTokenReq(caller: String?, callee: String?, seq: Long?, token: String? = "", id: String? = "", servciceType: Int? = 0) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packString(token ?: "")
                .packString(id ?: "")
                .packInt(servciceType ?: 0)

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

    fun MIDClientSetTokenReq(caller: String?, callee: String?, seq: Long?, regionType: Int? = 0) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packInt(regionType ?: 0)

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

    fun MIDClientSetEmailReq(caller: String?, callee: String?, seq: Long?, email: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(email ?: "")

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

    fun MIDClientSetPhoneNumReq(caller: String?, callee: String?, seq: Long?, code: String? = "", phoneNum: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packString(code ?: "")
                .packString(phoneNum ?: "")

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

    fun MIDClientSetAliasReq(caller: String?, callee: String?, seq: Long?, alias: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(alias ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16312, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientGetAliasReq(3.0)=16314

    3.0消息，获取昵称
     * */

    fun MIDClientGetAliasReq(caller: String?, callee: String?, seq: Long?) {
        sendMessage(16314, caller, callee, seq)
    }

    /**
     *MIDClient3rdSetPassReq(3.0)=16316

    3.0消息，第三方账号设置密码


    string, password
    int,    type                  0-绑定手机 1-绑定邮箱
    string, token                 消息16003中的token, 如果绑定手机号，需要填充
     * */

    fun MIDClient3rdSetPassReq(caller: String?, callee: String?, seq: Long?, password: String? = "", type: Int? = 0, token: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(3)
                .packString(password ?: "")
                .packInt(type ?: 0)
                .packString(token ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16316, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientCheckVersionReq(3.0)=16318

    3.0消息，客户端检测版本
     * */

    fun MIDClientCheckVersionReq(caller: String?, callee: String?, seq: Long?) {

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

    fun MIDClientGetVideoShareUrlReq(caller: String?, callee: String?, seq: Long?, fileName: String? = "", content: String? = "", ossType: Int? = 0, shareType: Int? = 0) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(4)
                .packString(fileName ?: "")
                .packString(content ?: "")
                .packInt(ossType ?: 0)
                .packInt(shareType ?: 0)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16320, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientGetAliSTSReq(3.0)=16322

    获取oss临时访问凭证


    int,    regionType
    string, cid          // cid or account
     * */

    fun MIDClientGetAliSTSReq(caller: String?, callee: String?, seq: Long?, regionType: Int? = 0, cid: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packInt(regionType ?: 0)
                .packString(cid ?: "")

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

    fun MIDClientShareReq(caller: String?, callee: String?, seq: Long?, cid: String? = "", toAccount: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packString(cid ?: "")
                .packString(toAccount ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16700, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientUnShareReq(3.0)=16702

    3.0消息，旧版消息1048


    [
    string,  cid
    string,  toAccount  //被分享账号
    ]
     * */

    fun MIDClientUnShareReq(caller: String?, callee: String?, seq: Long?, cid: String? = "", toAccount: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packString(cid ?: "")
                .packString(toAccount ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16702, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientShareListReq=16704

    旧版消息1050
     * */

    fun MIDClientShareListReq(caller: String?, callee: String?, seq: Long?) {
        sendMessage(16704, caller, callee, seq)
    }

    /**
     *MIDClientCidsShareInfoReq(3.0)=16706

    3.0消息, 获取设备分享状态


    [
    array,    cidList
    ]
     * */

    fun MIDClientCidsShareInfoReq(caller: String?, callee: String?, seq: Long?, cids: Array<String>? = arrayOf()) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packArrayHeader(cids?.size ?: 0)
                .apply { cids?.forEach { packString(it) } }

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16706, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientShareMultiReq(3.0)=16708

    3.0消息，同时分享给多个账号或者同时分享多个设备


    [
    array,   cids
    array,   toAccounts
    ]
     * */

    fun MIDClientShareMultiReq(caller: String?, callee: String?, seq: Long?, cids: Array<String>? = arrayOf(""), toAccounts: Array<String>? = arrayOf("")) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packArrayHeader(cids?.size ?: 0)
                .apply { cids?.forEach { packString(it) } }
                .packArrayHeader(toAccounts?.size ?: 0)
                .apply { toAccounts?.forEach { packString(it) } }

        val byteArray = bufferPacker.toByteArray()

        sendMessage(16708, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientGetFriendListReq(3.0)=17000

    3.0消息, 获取好友列表
     * */

    fun MIDClientGetFriendListReq(caller: String?, callee: String?, seq: Long?) {

        sendMessage(1700, caller, callee, seq)

    }

    /**
     *MIDClientGetFriendRequestListReq(3.0)=17002

    3.0消息, 获取添加请求列表
     * */

    fun MIDClientGetFriendRequestListReq(caller: String?, callee: String?, seq: Long?) {

        sendMessage(17002, caller, callee, seq)
    }

    /**
     *MIDClientAddFriendReq(3.0)=17004

    3.0消息,添加好友


    [
    string,   to //对方账户
    string,   sayHi  //附加消息
    ]
     * */

    fun MIDClientAddFriendReq(caller: String?, callee: String?, seq: Long?, to: String? = "", sayHi: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packString(to ?: "")
                .packString(sayHi ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(17004, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientDelFriendReq(3.0)=17006

    3.0消息,删除好友


    [
    string,   to //对方账户
    ]
     * */

    fun MIDClientDelFriendReq(caller: String?, callee: String?, seq: Long?, to: String? = "") {


        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(to ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(17006, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientConsentFriendReq(3.0)=17008

    3.0消息, 同意好友申请


    [
    string,   from //对方账户
    ]
     * */

    fun MIDClientConsentFriendReq(caller: String?, callee: String?, seq: Long?, from: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(from ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(17008, caller, callee, seq, byteArray)
    }

    /**
     *MIDClientSetMarkNameReq(3.0)=17010

    3.0消息, 好友备注


    [
    string,    to//对方账户
    string,    markName
    ]
     * */

    fun MIDClientSetMarkNameReq(caller: String?, callee: String?, seq: Long?, to: String? = "", markName: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packString(to ?: "")
                .packString(markName ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(17010, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientGetFriendInfoReq(3.0)=17012

    3.0消息, 获取备注名
     * */

    fun MIDClientGetFriendInfoReq(caller: String?, callee: String?, seq: Long?) {

        sendMessage(17012, caller, callee, seq)
    }

    /**
     *MIDClientCheckAccountReq(3.0)=17014

    3.0消息, 检测账号是否存在，存在返回基本信息


    [
    string,    account
    ]
     * */

    fun MIDClientCheckAccountReq(caller: String?, callee: String?, seq: Long?, account: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(account ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(17014, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientGetFeedbackListReq(3.0)=17100

    3.0消息, 请求反馈记录，只返回未读
     * */

    fun MIDClientGetFeedbackListReq(caller: String?, callee: String?, seq: Long?) {

        sendMessage(17100, caller, callee, seq)
    }

    /**
     *MIDClientPutFeedbackReq(3.0)=17102

    3.0消息, 客户端反馈信息


    [
    int64,    time
    string,   content
    bool,     isLog           //是否附带日志
    int,      regiontype      // 详见DPIDCloudStorage
    ]
     * */

    fun MIDClientPutFeedbackReq(caller: String?, callee: String?, seq: Long?, time: Long? = 0, content: String? = "", isLog: Boolean? = false, regiontype: Int? = 0) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(4)
                .packLong(time ?: 0)
                .packString(content ?: "")
                .packBoolean(isLog ?: false)
                .packInt(regiontype ?: 0)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(17102, caller, callee, seq, byteArray)
    }

    /**
     * MIDClientAdGetPolicyReq=17200


    放开权限检查，允许未登录调用


    [
    string，   vid            企业唯一标识。示例： 加菲狗 = 0001， doby = 0002
    int，      language_type  语言
    string，   version        版本号。注：服务器精准匹配。
    int，      os             iOS或Android的标识。
    string，   resolution     分辨率。示例："640x960"。注：服务器判断picUrl文件名是否包含该分辨率。
    ]

    客户端其他分辨率的处理方式：客户端将其映射到4种已有分辨率的一种。
     *
     */
    fun MIDClientAdGetPolicyReq(caller: String?, callee: String?, seq: Long?, vid: String? = "", language_type: Int? = 0, version: String? = "", os: Int? = 0, resolution: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(5)
                .packString(vid ?: "")
                .packInt(language_type ?: 0)
                .packString(version ?: "")
                .packInt(os ?: 0)
                .packString(resolution ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(17200, caller, callee, seq, byteArray)

    }

    /**
     *MIDClientAdHrefClick=17202


    放开权限检查，允许未登录调用
    统计图片热点链接的点击率


    [
    string，   vid            企业唯一标识。示例： 加菲狗 = 0001， doby = 0002
    int，      language_type  语言
    string，   version        版本号
    int，      os             iOS或Android
    string，   picHrefUrl     图片的热点链接（跳转的目标网址，如京东众筹页面）
    ]
     * */

    fun MIDClientAdHrefClick(caller: String?, callee: String?, seq: Long?, vid: String? = "", language_type: Int? = 0, version: String? = "", os: Int? = 0, picHrefUrl: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(5)
                .packString(vid ?: "")
                .packInt(language_type ?: 0)
                .packString(version ?: "")
                .packInt(os ?: 0)
                .packString(picHrefUrl ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(17202, caller, callee, seq, byteArray)

    }

    /**
     *MIDRobotMsgForward=20000


    服务器不存储该消息，只负责转发消息给终端。


    2016.3.24 , ZLL

    消息体为：
    [
    array,     dst             1.如果是客户端发起，则为设备CID数组；
    2.如果是设备端发起：
    a. 服务器查询主账号，再查询sessid，填充后转发给客户端；
    b. dst为账号数组时，服务器查询sessid，填充后转发给客户端（暂未支持）； --- 第三方账号，绑定关系不在加菲狗平台。

    int,       isAck           非零需要对端响应，零不需要对端响应
    int,       sn              序列号
    string,    msg             最大长度64K。SD卡状态DPID消息示例： msgpack(204)
    ]

     * */

    fun MIDRobotMsgForward(caller: String?, callee: String?, seq: Long?, dst: Array<String>? = arrayOf(), isAck: Int? = 0, sn: Int? = 0, bytes: ByteArray? = byteArrayOf()) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(4)
                .packArrayHeader(dst?.size ?: 0).apply { dst?.forEach { packString(it) } }
                .packInt(isAck ?: 0)
                .packInt(sn ?: 0)
                .writePayload(bytes ?: byteArrayOf())

        val byteArray = bufferPacker.toByteArray()

        sendMessage(20000, caller, callee, seq, byteArray)

    }

    /**
     *MIDRobotSetMsgConfig=20002


    服务器存储该消息，并转发消息给终端。
    该接口逐渐弃用。


    旧版消息 1114。
    3.* APP 和 3.* 设备端统一使用DPIDCameraHangMode = 509 及 DPIDCameraCoord = 510。
    2.* 设备仍在使用1114，服务器处理 3.* APP 与 2.* 设备兼容。


    2016.3.15 , ZLL

    将value存储到KVS（服务器使用cid与key的组合做唯一KEY）。
    消息体为：
    [
    string,   key            厂家需要存储的key。
    string,   value          厂家需要存储的value，最大长度64K。
    ]

    如果是客户端发起，callee 填充为cid，服务器会同时转发消息给设备端 2016.4.8
    如果是设备端发起，callee 置空，服务器会转发消息给客户端 2016.09.14

    1 陀螺仪： key=tly 默认值为'1'，平视。
    消息体为：

    string: v    '0'俯视, '1' 平视。


    2 圆形：  key=round 无默认值。

    消息体为：
    msgpack([
    int32，  x        横坐标
    int32，  y        竖坐标
    int32，  r        半径
    int32，  w        分辨率 宽
    int32，  h        分辨率 高
    ])

     * */

    fun MIDRobotSetMsgConfig(caller: String = "", callee: String = "", seq: Long) {

    }

    /**
     *MIDRobotGetMsgConfig=20004

    旧版消息 1116。
    3.* APP 和 3.* 设备端统一使用DPIDCameraHangMode = 509 及 DPIDCameraCoord = 510。
    2.* 设备仍在使用1114，服务器处理 3.* APP 与 2.* 设备兼容。


    2016.3.15 , ZLL

    消息体为：
    [
    string,   key            厂家需要存储的key。
    ]

    服务器从kvs缓存中读取value并响应。
     * */

    fun MIDRobotGetMsgConfig(caller: String?, callee: String?, seq: Long?, key: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(1)
                .packString(key ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(20004, caller, callee, seq, byteArray)

    }

    /**
     *MIDRobotForwardDataV2=20006


    服务器不存储该消息，只负责转发消息给终端。



    消息体为：
    [
    array,     dst             1.如果是客户端发起，则为设备CID数组；
    2.如果是设备端发起：
    a. 服务器查询主账号，再查询sessid，填充后转发给客户端；
    b. dst为账号数组时，服务器查询sessid，填充后转发给客户端（暂未支持）； --- 第三方账号，绑定关系不在加菲狗平台。

    int,       isAck           非零需要对端响应，零不需要对端响应
    int,       type            功能定义。见下表定义
    byte[]，   msg             最大长度64K
    ]






    type
    功能
    描述
    设备型号




    TYPE_FILE_DOWNLOAD_REQ = 1
    下载请求
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_DOWNLOAD_RSP = 2
    下载响应
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_DELETE_REQ   = 3
    删除请求
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_DELETE_RSP   = 4
    删除响应
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_LIST_REQ     = 5
    列表请求
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_LIST_RSP     = 6
    列表响应
    图片和视频文件管理
    DOG_5W


    TYPE_TAKE_PICTURE_REQ  = 7
    拍照请求
    图片和视频文件管理
    DOG_5W


    TYPE_TAKE_PICTURE_RSP  = 8
    拍照响应
    图片和视频文件管理
    DOG_5W


    TYPE_VIDEO_BEGIN_REQ   = 9
    开始录像请求
    图片和视频文件管理
    DOG_5W


    TYPE_VIDEO_BEGIN_RSP   = 10
    开始录像响应
    图片和视频文件管理
    DOG_5W


    TYPE_VIDEO_END_REQ     = 11
    停止录像请求
    图片和视频文件管理
    DOG_5W


    TYPE_VIDEO_END_RSP     = 12
    停止录像响应
    图片和视频文件管理
    DOG_5W


    TYPE_VIDEO_STATUS_REQ  = 13
    查询录像状态请求
    图片和视频文件管理
    DOG_5W


    TYPE_VIDEO_STATUS_RSP  = 14
    查询录像状态响应
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_LOGO_REQ     = 15
    设置水印请求
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_LOGO_RSP     = 16
    设置水印响应
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_RESOLUTION_REQ = 17
    设置视频分辨率请求
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_RESOLUTION_RSP = 18
    视频分辨率响应
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_GET_LOGO_REQ     = 19
    查询水印请求
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_GET_LOGO_RSP     = 20
    查询水印响应
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_GET_RESOLUTION_REQ = 21
    查询视频分辨率请求
    图片和视频文件管理
    DOG_5W


    TYPE_FILE_GET_RESOLUTION_RSP = 22
    查询视频分辨率响应
    图片和视频文件管理
    DOG_5W









    type
    功能
    DP 结构数组
    设备型号




    TYPE_DPID_REQ = 51
    专用于查询DPID类消息，APP与设备直连时使用
    array(REQ)
    DOG_5W


    TYPE_DPID_RSP = 52
    专用于响应DPID类消息，APP与设备直连时使用
    array(RSP)
    DOG_5W









    type
    功能
    MID 结构数组
    设备型号




    TYPE_MID_REQ = 61
    专用于查询MID类消息，APP与设备直连时使用
    array(REQ)
    全部设备


    TYPE_MID_RSP = 62
    专用于响应MID类消息，APP与设备直连时使用
    array(RSP)
    全部设备







    客户端和设备端：
    1. 两者处于公网情况下，使用该TCP消息转发。
    2. 两者处于局域网情况下，使用设备端开启的HTTP服务处理。

    1. MSG_TYPE_FILE_DOWNLOAD_REQ = 1：
    ｛
    string，  fileName   文件名, 注：根据后缀区分是图片或视频
    int，     begin      起始位置
    int，     offset     偏移量
    ｝

    2. MSG_TYPE_FILE_DOWNLOAD_RSP = 2：
    ｛
    int，     ret       错误码
    string，  fileName  文件名, 注：根据后缀区分是图片或视频
    int，     begin     起始位置
    int，     offset    偏移量
    byte[],   buffer    文件内容
    ｝


    3. MSG_TYPE_FILE_DELETE_REQ = 3：
    ｛
    array，  fileNameList    (string)  文件名列表, 注：根据后缀区分是图片或视频
    int,    //优先级高于array(fileName)  1：正向删除（删除array数组的文件）， -1：反向删除（保留array数组的文件，删除其余文件）， 0： 删除全部文件（此时不考虑array数组）
    ｝

    4. MSG_TYPE_FILE_DELETE_RSP = 4:
    ｛
    int，    ret       错误码
    array，  fileNameList    (string)  文件名列表, 注：根据后缀区分是图片或视频
    ｝

    5. MSG_TYPE_FILE_LIST_REQ = 5：
    ｛
    int，      beginTime  查询开始时间， unix timestamp 单位秒。填充 0 时， 设备端从本地最早的文件开始查询。
    int，      endTime    查询截止时间， unix timestamp 单位秒。
    int，      limit      查询条数
    ｝

    6. MSG_TYPE_FILE_LIST_RSP = 6：
    ｛
    array，    fileNameList    (string)  文件名列表， 命名格式[timestamp].jpg 或 [timestamp]_[secends].avi， timestamp是文件生成时间的unix时间戳，secends是视频录制的时长,单位秒。根据后缀区分是图片或视频。
    ｝

    7. MSG_TYPE_TAKE_PICTURE_REQ = 7：
    ｛

    ｝

    8. MSG_TYPE_TAKE_PICTURE_RSP = 8：
    ｛
    int，    ret      错误码
    array，  fileNameList    (string)  文件名列表， 命名格式[timestamp].jpg 或 [timestamp]_[secends].avi， timestamp是文件生成时间的unix时间戳，secends是视频录制的时长,单位秒。根据后缀区分是图片或视频。
    ｝

    9. MSG_TYPE_VIDEO_BEGIN_REQ = 9：
    ｛
    int,    videoType 特征值定义： videoTypeShort = 1 8s短视频； videoTypeLong = 2 长视频；
    ｝

    10. MSG_TYPE_VIDEO_BEGIN_RSP = 10：
    ｛
    int，    ret       错误码
    }

    11. MSG_TYPE_VIDEO_END_REQ = 11：
    ｛
    int,    videoType 特征值定义： videoTypeShort = 1 8s短视频； videoTypeLong = 2 长视频；
    ｝

    12. MSG_TYPE_VIDEO_END_RSP = 12：
    ｛
    int，     ret       错误码
    array，   fileNameList  文件名， 命名格式[timestamp].jpg 或 [timestamp]_[secends].avi， timestamp是文件生成时间的unix时间戳，secends是视频录制的时长,单位秒。根据后缀区分是图片或视频。
    }

    13. MSG_TYPE_VIDEO_STATUS_REQ = 13：
    {
    }

    14. MSG_TYPE_VIDEO_STATUS_RSP = 14：
    ｛
    int，     ret       错误码
    int，     secends   视频录制的时长,单位秒
    int,      videoType   特征值定义： videoTypeShort = 1 8s短视频； videoTypeLong = 2 长视频；
    ｝

    15. TYPE_FILE_LOGO_REQ = 15:
    {
    int，     type      水印特征值：｛1，2，3，4｝
    }

    16. TYPE_FILE_LOGO_RSP = 16:
    ｛
    int，     ret       错误码
    }

    17. TYPE_FILE_RESOLUTION_REQ = 17:
    {
    int，     type      分辨率特征值：｛0（标清1280*640），1（高清2560*1280）｝
    }

    18. TYPE_FILE_RESOLUTION_RSP = 18:
    ｛
    int，     ret       错误码:  ret=-1 设置失败
    }

    19. TYPE_FILE_GET_LOGO_REQ = 19:
    {
    }

    20. TYPE_FILE_GET_LOGO_RSP = 20:
    {
    int，     ret       错误码
    int，     type      水印特征值：｛1，2，3，4｝
    }

    21. TYPE_FILE_GET_RESOLUTION_REQ = 21:
    {
    }

    22. TYPE_FILE_GET_RESOLUTION_RSP = 22:
    {
    int，     ret       错误码
    int，     type      分辨率特征值：｛0（标清1280*640），1（高清2560*1280）｝
    }

    [
    array,     dst             1.如果是客户端发起，则为设备CID数组；
    2.如果是设备端发起：
    a. 服务器查询主账号，再查询sessid，填充后转发给客户端；
    b. dst为账号数组时，服务器查询sessid，填充后转发给客户端（暂未支持）； --- 第三方账号，绑定关系不在加菲狗平台。

    int,       isAck           非零需要对端响应，零不需要对端响应
    int,       type            功能定义。见下表定义
    byte[]，   msg             最大长度64K
    ]
     * */

    fun MIDRobotForwardDataV2(caller: String?, callee: String?, seq: Long?, dst: Array<String>? = arrayOf(), isAck: Int? = 0, type: Int? = 0, msg: ByteArray? = byteArrayOf()) {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(4)
                .packArrayHeader(dst?.size ?: 0).apply { dst?.forEach { packString(it) } }
                .packInt(isAck ?: 0)
                .packInt(type ?: 0)
                .writePayload(msg ?: byteArrayOf())

        val byteArray = bufferPacker.toByteArray()

        sendMessage(20006, caller, callee, seq, byteArray)

    }

    /**
     *MIDRobotCIDRegisterReq=20100

    设备端根据sn获取CID标识 2016.5.27

    [
    string,   vid        必填：4位字符串，从萝卜头平台获取： http://[yf|test|open].robotscloud.com 登录 -> 企业信息 -> 帐号信息 -> 企业编号 。
    uint32,   pid        必填：原os，设备类型值
    string,   sn         必填：加菲狗即为原设备标识，如200000000001，其他厂商为其自有的序列号。
    string,   signature  a.初始版本为3.*，则必填，设备端私钥对sn的签名，服务端验证； b. 2.* 升级到3.* 版本时signature允许该参数为空，服务器判断sn存在即通过验证。
    ]

     * */

    fun MIDRobotCIDRegisterReq(caller: String?, callee: String?, seq: Long?, vid: String? = "", pid: Int? = 0, sn: String? = "", signature: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(4)
                .packString(vid ?: "")
                .packInt(pid ?: 0)
                .packString(sn ?: "")
                .packString(signature ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(20100, caller, callee, seq, byteArray)

    }

    /**
     *MIDRobotCIDLogin=20102

    设备端登录消息 2016.5.16

    [
    uint32,   pid
    string,   sn         加菲狗即为原设备标识，如200000000001，其他厂商为其自有的序列号。
    string,   signature  设备端私钥对sn的签名，服务端验证
    string,   cid        4位vid + 9位随机数； 12位cid则与sn一致。
    ]

    signature:
    2.* 升级到3.* 版本时signature允许该参数为空，服务器判断sn存在即通过验证。
     * */

    fun MIDRobotCIDLogin(caller: String?, callee: String?, seq: Long?, pid: Int? = 0, sn: String? = "", signature: String? = "", cid: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(4)
                .packInt(pid ?: 0)
                .packString(sn ?: "")
                .packString(signature ?: "")
                .packString(cid ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(20102, caller, callee, seq, byteArray)

    }

    /**
     *MIDRobotCidRandom=20104



    绑定Cid与随机数关联 2016.06.17

    [
    string,   random     绑定随机数
    string,   cid        4位vid + 9位随机数（旧版CID仍然用12位CID）
    ]



    对应客户端消息 MIDClientBindCidReq： MIDClientBindCidReq=16200
     * */

    fun MIDRobotCidRandom(caller: String?, callee: String?, seq: Long?, random: String? = "", cid: String? = "") {

        val bufferPacker = getPacker()

        bufferPacker
                .packArrayHeader(2)
                .packString(random ?: "")
                .packString(cid ?: "")

        val byteArray = bufferPacker.toByteArray()

        sendMessage(20104, caller, callee, seq, byteArray)

    }

    /**
     *MIDRobotGetData=20200


    MID-DP 单设备查询请求




    定义
    类型
    描述




    id
    int
    TCP消息唯一标识


    caller
    string
    源端标识


    callee
    string
    目的端标识


    seq
    int64
    序列号


    limit
    int
    查询条数


    asc
    bool
    对time的正向、逆向查询


    reqList
    array
    被查询功能的消息列表


    equal
    bool
    是否等于time的查询， 暂未生效







    定义
    类型
    描述




    dpid
    int
    开放功能唯一标识


    time
    int64
    单位毫秒。查询时间点




    说明：


    time 统一以服务器时间戳为准。
    如果 asc = true,  equal = true， 正向查询，返回 >=time 的数据。
    如果 asc = true,  equal = false，正向查询，返回 >time  的数据。
    如果 asc = false, equal = true， 逆向查询，返回 <=time  的数据。
    如果 asc = false, equal = false，逆向查询，返回 <time 的数据。
    如果time置为0, 默认使用服务器当前时间。
    如果查询单条记录，limit置为1。
    如果查询列表，limit >1 && limit <=100。
    服务器或目的端将响应的seq号与请求的seq保持相同，以保证消息的一一对应。
     * */

    fun MIDRobotGetData(caller: String?, callee: String?, seq: Long?, limit: Int? = 0, asc: Boolean? = false, reqList: Array<ByteArray>? = arrayOf(), equal: Boolean? = false) {

        val bufferPacker = getPacker()

        bufferPacker
                .packInt(limit ?: 0)
                .packBoolean(asc ?: false)
                .packArrayHeader(reqList?.size ?: 0).apply { reqList?.forEach { writePayload(it) } }
                .packBoolean(equal ?: false)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(20200, caller, callee, seq, byteArray)

    }

    /**
     *MIDRobotSetData=20202


    MID-DP 设备上报消息 + 配置请求




    定义
    类型
    描述




    id
    int
    TCP消息唯一标识


    caller
    string
    源端标识


    callee
    string
    目的端标识


    seq
    int64
    序列号


    reqList
    array
    消息数组







    元素定义
    类型
    描述




    dpid
    int
    开放功能唯一标识


    time
    int64
    单位毫秒：更新时间点，服务端取系统当前时间戳存储到数据库。


    value
    msgpack(int, string, struct...)
    msgpack值
     * */

    fun MIDRobotSetData(caller: String?, callee: String?, seq: Long?, reqList: Array<ByteArray>? = arrayOf()) {

        sendMessageWithReqList(20202, caller, callee, seq, reqList)

    }

    /**
     *MIDRobotDelData=20204


    MID-DP 删除请求

    id int TCP消息唯一标识


    caller
    string
    源端标识


    callee
    string
    目的端标识


    seq
    int64
    序列号


    reqList
    array
    消息数组







    元素定义
    类型
    描述




    dpid
    int
    开放功能唯一标识


    time
    int64
    单位毫秒。删除时间点





    time 如果 time > 0 ，删除匹配条件的单条记录。如果 time = -1, 则删除该消息的全部消息。
     * */

    fun MIDRobotDelData(caller: String?, callee: String?, seq: Long?, reqList: Array<ByteArray>? = arrayOf()) {

        sendMessageWithReqList(20204, caller, callee, seq, reqList)
    }

    /**
     *MIDRobotCountUnReadData=20206


    MID-DP 单设备未读消息计数查询
     * */

    fun MIDRobotCountUnReadData(caller: String?, callee: String?, seq: Long?, act: Int? = 0, reqList: Array<ByteArray>? = arrayOf()) {

        val bufferPacker = getPacker()

        bufferPacker
                .packInt(act ?: 0)
                .packArrayHeader(reqList?.size ?: 0)
                .apply { reqList?.forEach { writePayload(it) } }

        val byteArray = bufferPacker.toByteArray()

        sendMessage(20206, caller, callee, seq, byteArray)
    }

    /**
     *MIDRobotGetDataEx=20208
    MID-DP 组合查询请求
     * */

    fun MIDRobotGetDataEx(caller: String?, callee: String?, seq: Long?, asc: Boolean? = false, time: Long? = 0, reqList: Array<ByteArray>? = arrayOf()) {

        val bufferPacker = getPacker()

        bufferPacker
                .packBoolean(asc ?: false)
                .packLong(time ?: 0)
                .packArrayHeader(reqList?.size ?: 0)
                .apply { reqList?.forEach { writePayload(it) } }

        val byteArray = bufferPacker.toByteArray()

        sendMessage(20208, caller, callee, seq, byteArray)

    }

    /**
     *MIDRobotGetMultiData=20212
    MID-DP 多设备查询请求
     * */

    fun MIDRobotGetMultiData(caller: String?, callee: String?, seq: Long?, limit: Int? = 0, asc: Boolean? = false, cidReqListMap: Array<ByteArray>? = arrayOf(), equal: Boolean? = false) {

        val bufferPacker = getPacker()

        bufferPacker
                .packInt(limit ?: 0)
                .packBoolean(asc ?: false)
                .packArrayHeader(cidReqListMap?.size ?: 0)
                .apply { cidReqListMap?.forEach { writePayload(it) } }
                .packBoolean(equal ?: false)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(20212, caller, callee, seq, byteArray)

    }

    /**
     * MIDRobotCountMultiUnReadData=20214
    MID-DP 多设备未读消息计数
     * */

    fun MIDRobotCountMultiUnReadData(caller: String?, callee: String?, seq: Long?, act: Int? = 0, cidReqListMapList: Array<ByteArray>? = arrayOf()) {

        val bufferPacker = getPacker()

        bufferPacker
                .packInt(act ?: 0)
                .packArrayHeader(cidReqListMapList?.size ?: 0)
                .apply { cidReqListMapList?.forEach { writePayload(it) } }

        val byteArray = bufferPacker.toByteArray()

        sendMessage(20214, caller, callee, seq, byteArray)

    }

    /**
     *MIDRobotSetDataByTime=20216
    MID-DP 新增一条消息记录：其中time字段根据终端上发的time设置
     * */

    fun MIDRobotSetDataByTime(caller: String?, callee: String?, seq: Long?, reqList: Array<ByteArray>? = arrayOf()) {

        sendMessageWithReqList(20216, caller, callee, seq, reqList)

    }

    /**
     *MIDRobotGetDataByTime=20218


    MID-DP 根据时间条件的单设备精准查询请求：数据库中存在该记录则返回数据，否则返回空。
     * */

    fun MIDRobotGetDataByTime(caller: String?, callee: String?, seq: Long?, reqList: Array<ByteArray>? = arrayOf()) {

        sendMessageWithReqList(20218, caller, callee, seq, reqList)
    }

    /**
     * MIDRobotCountData=20220
    MID-DP 单设备消息总数查询
     * */

    fun MIDRobotCountData(caller: String?, callee: String?, seq: Long?, reqList: Array<ByteArray>? = arrayOf()) {

        sendMessageWithReqList(20220, caller, callee, seq, reqList)

    }

    /**
     *MIDRobotDelDataEx=20222
    MID-DP 删除某一时间段内消息的请求
     * */

    fun MIDRobotDelDataEx(caller: String?, callee: String?, seq: Long?, reqList: Array<ByteArray>? = arrayOf()) {

        sendMessageWithReqList(20222, caller, callee, seq, reqList)

    }

    /**
     *MIDRobotForwardDataV3=20224


    客户端和设备端之间的实时通信的请求消息。
    服务端处理方式： 1. 如果对端在线，服务器直接转发（不存入数据库）; 2.如果对端不在线，该消息将被丢弃。
    使用场景示例：获取设备电量，SD卡容量等。
    目的端将响应的seq号与请求的seq保持相同，以保证消息的一一对应。
     * */

    fun MIDRobotForwardDataV3(caller: String?, callee: String?, seq: Long?, reqList: Array<ByteArray>? = arrayOf()) {

        sendMessageWithReqList(20224, caller, callee, seq, reqList)
    }

    /**
     *MIDGetMIDData=20260

    通用请求消息
     * */

    fun MIDGetMIDData(caller: String?, callee: String?, seq: Long?, msgTypeId: Int? = 0, req: ByteArray? = byteArrayOf()) {

        val bufferPacker = getPacker()

        bufferPacker
                .packInt(msgTypeId ?: 0)
                .writePayload(req)

        val byteArray = bufferPacker.toByteArray()

        sendMessage(20260, caller, callee, seq, byteArray)

    }

    fun getPageMessage(page: PAGE_MESSAGE, vararg os_pid: Int) {

        val params: HashMap<String, Array<JFGDPMsg>> = hashMapOf()

        os_pid.forEach {
//          OS_PROPERTY.valueOf()
        }

        BaseApplication.getAppComponent().cmd.robotGetMultiData(params, 1, false, 0)

    }

}