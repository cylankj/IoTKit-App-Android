package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-12
 * Time: 14:25
 */
@Message
public class CidPushOssConfig extends MsgpackMsg.MsgHeader {
    @Index(3)
    public int type;  //地址类型，狗上报完成以后，把这个类型原本的上报到服务器
    @Index(4)
    public String hostname;
    @Index(5)
    public String bucket;
    @Index(6)
    public String AccessID;
    @Index(7)
    public String Access_Key;// AES加密, 128位。前面16个字节是动态IV，后面32字节是真实的数据，采用base64编码传输，解码先转码再解
    @Index(8)
    public long magicMD5;
// ,MD5掩码，无符号32位整数，不够的考虑用long。32位的MD5，magicMD5其中有16位为1，取出为1的16位MD5作为AES公钥
//    C/java可以用二进制向右移位进行操作。右移位对应下标高位索引。就是低位二进制对应高位MD5下标字符串索引
//    目前采用128位的ECB方式，加密的前面随机16字节数据不需要考虑。
}
