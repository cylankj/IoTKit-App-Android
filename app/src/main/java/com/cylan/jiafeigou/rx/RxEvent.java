package com.cylan.jiafeigou.rx;

import android.os.Bundle;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgCount;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGDoorBellCaller;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.entity.jniCall.JFGResult;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DataPointManager;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by cylan-hunt on 16-7-6.
 */
public class RxEvent {

    public static class NeedLoginEvent {
        public static final String KEY = "show_login_fragment";
        public Bundle bundle;

        public NeedLoginEvent(Bundle bundle) {
            this.bundle = bundle;
        }
    }

    /**
     * 系统TimeTick广播
     */
    public static class TimeTickEvent {

    }

    public static class LoginRsp {
        public boolean state;

        public LoginRsp(boolean state) {
            this.state = state;
        }
    }

    public static class ActivityResult {
        public Bundle bundle;
    }

    public static class CloudLiveDelete {

    }

    /**
     * The type Result event.
     */
    public static class ResultEvent {
        /**
         * The constant JFG_RESULT_VERIFY_SMS.
         */
        public static final int JFG_RESULT_VERIFY_SMS = 0;
        /**
         * The constant JFG_RESULT_REGISTER.
         */
        public static final int JFG_RESULT_REGISTER = 1;
        /**
         * The constant JFG_RESULT_LOGIN.
         */
        public static final int JFG_RESULT_LOGIN = 2;
        /**
         * The constant JFG_RESULT_BINDDEV.
         */
        public static final int JFG_RESULT_BINDDEV = 3;

        /**
         * The constant JFG_RESULT_UNBINDDEV.
         */
        public static final int JFG_RESULT_UNBINDDEV = 4;


        /**
         * The constant JFG_RESULT_UPDATE_ACCOUNT.
         */
        public static final int JFG_RESULT_UPDATE_ACCOUNT = 5;

        /**
         * 删除好友的结果
         */
        public static final int JFG_RESULT_DEL_FRIEND = 6;
        /**
         * 同意添加好友的结果
         */
        public static final int JFG_RESULT_CONSENT_ADD_FRIEND = 7;

        /**
         * 设置好友备注名
         */
        public static final int JFG_RESULT_SET_FRIEND_MARKNAME = 8;
    }

    /**
     * The type Sms code result.
     */
    public static class SmsCodeResult {

        /**
         * The Error.
         */
        public int error;
        /**
         * The Token.
         */
        public String token;

        /**
         * Instantiates a new Sms code result.
         *
         * @param error the error
         * @param token the token
         */
        public SmsCodeResult(int error, String token) {
            this.error = error;
            this.token = token;
        }

        @Override
        public String toString() {
            return "SmsCodeResult{" +
                    "error=" + error +
                    ", token='" + token + '\'' +
                    '}';
        }
    }

    /**
     * {@link com.cylan.jfgapp.jni.JfgAppCallBack#OnSendSMSResult(int, String)}
     */
    public static final class ResultVerifyCode {
        public int code;

        public ResultVerifyCode(int code) {
            this.code = code;
        }
    }

    /**
     * 调用 {@link com.cylan.jfgapp.jni.JfgAppCmd#register(String, String, int, String)}
     */
    public static final class ResultRegister {
        public int code;

        public ResultRegister(int code) {
            this.code = code;
        }
    }

    public static final class ResultLogin {
        public int code;

        public ResultLogin(int code) {
            this.code = code;
        }
    }

    public static final class ResultBind {
        public int code;

        public ResultBind(int code) {
            this.code = code;
        }
    }


    public static final class ResultUnBind {
        public int code;
    }

    /**
     * 切换 “登陆” “注册”
     */
    public static final class SwitchBox {
//        public String account;

//        public SwitchBox(String account) {
//            this.account = account;
//        }
    }


    public static final class LoginPopBack {
        public String account;

        public LoginPopBack(String account) {
            this.account = account;
        }

    }

    public static final class ForgetPwdByMail {
        public String account;

        public ForgetPwdByMail(String account) {
            this.account = account;
        }
    }

    public static final class PageScrolled {
    }

    /**
     * desc:获取好友列表类
     */
    public static final class GetFriendList {

        public int i;
        public ArrayList<JFGFriendAccount> arrayList;

        public GetFriendList(int i, ArrayList<JFGFriendAccount> arrayList) {
            this.i = i;
            this.arrayList = arrayList;
        }
    }

    /**
     * desc：获取添加请求类
     */
    public static final class GetAddReqList {

        public int i;

        public ArrayList<JFGFriendRequest> arrayList;

        public GetAddReqList(int i, ArrayList<JFGFriendRequest> arrayList) {
            this.i = i;
            this.arrayList = arrayList;
        }

    }

    /**
     * desc：获取到分享设备的信息
     */
    public static final class GetShareListCallBack {
        public int i;

        public ArrayList<JFGShareListInfo> arrayList;

        public GetShareListCallBack(int i, ArrayList<JFGShareListInfo> arrayList) {
            this.i = i;
            this.arrayList = arrayList;
        }
    }



    /**
     * 设备列表返回,粗糙数据,任然需要通过查询
     */
    public static final class DeviceRawList {
        public JFGDevice[] devices;

        public DeviceRawList(JFGDevice[] list) {
            this.devices = list;

        }
    }

    /**
     * 获取登录用户的信息
     */
    public static final class GetUserInfo {

        public JFGAccount jfgAccount;

        public GetUserInfo(JFGAccount jfgAccount) {
            this.jfgAccount = jfgAccount;
        }
    }

    /**
     * 获取到http请求的结果
     */
    public static final class GetHttpDoneResult {
        public JFGMsgHttpResult jfgMsgHttpResult;

        public GetHttpDoneResult(JFGMsgHttpResult jfgMsgHttpResult) {
            this.jfgMsgHttpResult = jfgMsgHttpResult;
        }
    }

    public static final class LocalUdpMsg {
        //消息的时间,可以用来判断有效性.
        public long time;
        public String ip;
        public short port;
        public byte[] data;

        @Override
        public String toString() {
            return "LocalUdpMsg{" +
                    "ip='" + ip + '\'' +
                    ", port=" + port +
                    ", data=" + Arrays.toString(data) +
                    '}';
        }
    }

    /**
     * 分享设备的回调
     */
    public static final class ShareDeviceCallBack {

        public int requestId;

        public String cid;

        public String account;

        public ShareDeviceCallBack(int requestId, String cid, String account) {
            this.requestId = requestId;
            this.cid = cid;
            this.account = account;
        }
    }

    public static final class BindDeviceEvent {
        public JFGResult jfgResult;

        public BindDeviceEvent(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    public static final class UnBindDeviceEvent {
        public JFGResult jfgResult;

        public UnBindDeviceEvent(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 检验邮箱是否注册过回调
     */
    public static final class CheckAccountCallback {
        public int i;

        public CheckAccountCallback(int i, String s, String s1, boolean b) {
            this.i = i;
            this.s = s;
            this.s1 = s1;
            this.b = b;
        }

        public String s;
        public String s1;
        public boolean b;
    }

    /**
     * 获取到已经分享的好友的回调
     */
    public static final class GetHasShareFriendCallBack {
        public int i;
        public ArrayList<JFGFriendAccount> arrayList;

        public GetHasShareFriendCallBack(int i, ArrayList<JFGFriendAccount> arrayList) {
            this.i = i;
            this.arrayList = arrayList;
        }
    }

    /**
     * 取消分享的回调
     */
    public static final class UnshareDeviceCallBack {
        public int i;
        public String cid;
        public String account;

        public UnshareDeviceCallBack(int i, String cid, String account) {
            this.i = i;
            this.cid = cid;
            this.account = account;
        }
    }

    /**
     * 获取设备列表
     */
    public static final class DeviceList {
        public List<JFGDevice> jfgDevices;

        public DeviceList(List<JFGDevice> jfgDevices) {
            this.jfgDevices = jfgDevices;
        }
    }


    /**
     * 只有一个属性,设置页面更新的某一个属性
     */
    public static final class JfgDpMsgUpdate {
        public String uuid;
        public DpMsgDefine.DpMsg dpMsg;
    }

//    public static final class JfgAlarmMsg {
//        public String uuid;
//        public ArrayList<DpMsgDefine.DpMsg> jfgdpMsgs;
//    }

    public static final class JFGRobotSyncData {
        public String identity;
        public boolean state;
        public ArrayList<JFGDPMsg> dataList;
    }

    /**
     * {@link DataPointManager}消息池,消息已经变化.
     */
    public static final class DataPoolUpdate {
        public String uuid;
        public int id;
        public BaseValue value;
    }

    /**
     * 修改设备属性
     */
    /**
     * @Deprecated use {@link com.cylan.jiafeigou.cache.pool.GlobalDataProxy#update(String, BaseValue)}
     */
    @Deprecated
    public static final class JFGAttributeUpdate extends DpMsgDefine.DpMsg {
        public String uuid;
    }

    /**
     * 获取好友的信息回调
     */
    public static final class GetFriendInfoCall {
        public int i;

        public GetFriendInfoCall(int i, JFGFriendAccount jfgFriendAccount) {
            this.i = i;
            this.jfgFriendAccount = jfgFriendAccount;
        }

        public JFGFriendAccount jfgFriendAccount;
    }

    /**
     * 解绑设备
     */
    public static final class UnbindJFGDevice {
        public String uuid;
    }

    /**
     * 这个消息从{@link com.cylan.jiafeigou.n.engine.DataSourceService#OnRobotCountDataRsp(long, String, ArrayList)}
     * 传到{@link DataPointManager#handleUnreadMessageCount()}
     */
    public static final class UnreadCount {
        public String uuid;
        public long seq;
        public ArrayList<JFGDPMsgCount> msgList;

        public UnreadCount() {
        }

        public UnreadCount(String uuid, long seq, ArrayList<JFGDPMsgCount> counts) {
            this.uuid = uuid;
            this.seq = seq;
            this.msgList = counts;
        }
    }

    /**
     * 未读消息数响应,以uuid为单位
     */
    public static final class UnreadCountRsp {
        public String uuid;
        public long time;
        public int count;
    }

    /**
     * 历史数据查询,带一个时间戳,m默认一次请求10条.
     */
    public static final class JFGHistoryVideoReq {
        public String uuid;
        public boolean desc;//降序,逆序
        public long version;
    }

    /**
     * 历史录像数据响应
     */
    public static final class JFGHistoryVideoRsp {
    }

    /**
     * 系统反馈回复
     */
    public static final class GetFeedBackRsp {
        public int i;
        public ArrayList<JFGFeedbackInfo> arrayList;

        public GetFeedBackRsp(int i, ArrayList<JFGFeedbackInfo> arrayList) {
            this.i = i;
            this.arrayList = arrayList;
        }
    }

    public static final class BellCallEvent {

        public final JFGDoorBellCaller caller;

        public BellCallEvent(JFGDoorBellCaller jfgDoorBellCaller) {
            this.caller = jfgDoorBellCaller;
        }
    }

    public static class BellLiveEvent {
        public boolean hold;

        public BellLiveEvent(boolean hold) {
            this.hold = hold;
        }
    }

    /**
     * 修改密码的返回
     */
    public static final class ChangePwdBack {
        public JFGResult jfgResult;

        public ChangePwdBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 修改密码的返回
     */
    public static final class ResetPwdBack {
        public JFGResult jfgResult;

        public ResetPwdBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 添加亲友的返回
     */
    public static final class AddFriendBack {
        public JFGResult jfgResult;

        public AddFriendBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 删除亲友的返回
     */
    public static final class DelFriendBack {
        public JFGResult jfgResult;

        public DelFriendBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 同意添加亲友的返回
     */
    public static final class ConsentAddFriendBack {
        public JFGResult jfgResult;

        public ConsentAddFriendBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 同意添加亲友的返回
     */
    public static final class SetFriendAliasBack {
        public JFGResult jfgResult;

        public SetFriendAliasBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 发送反馈的返回
     */
    public static final class SendFeekBack {
        public JFGResult jfgResult;

        public SendFeekBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 三方绑定手机设置密码时的返回
     */
    public static final class OpenLogInSetPwdBack {
        public JFGResult jfgResult;

        public OpenLogInSetPwdBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 删除好友添加请求
     */
    public static final class DeleteAddReqBack{
        public JFGResult jfgResult;

        public DeleteAddReqBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    public static class AppHideEvent {
    }

    public static class EFamilyMsgpack {
        public int msgId;
        public byte[] data;
    }

    public static class CallAnswerd {

    }

    public static class GetDataResponse {
        public long seq;
        public long msgId;
        public boolean changed;
    }

    public static class DeviceSyncRsp {
        public DeviceSyncRsp setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public String uuid;
    }
}
