package com.cylan.jiafeigou.rx;

import android.content.Intent;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsgCount;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGDoorBellCaller;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.entity.jniCall.JFGResult;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.n.engine.DataSource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by cylan-hunt on 16-7-6.
 */
public class RxEvent {

//    public static class NeedLoginEvent {
//        public static final String KEY = "show_login_fragment";
//        public Bundle bundle;
//
//        public NeedLoginEvent(Bundle bundle) {
//            this.bundle = bundle;
//        }
//    }

    /**
     * 系统TimeTick广播
     */
    public static class TimeTickEvent {

    }

    @Deprecated //账号状态不依据这个消息
    public static class OnlineStatusRsp {
        public boolean state;

        public OnlineStatusRsp(boolean state) {
            this.state = state;
        }
    }

    /**
     * 分享账号,列表响应
     */
    public static class GetShareListRsp {
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

        @Override
        public String toString() {
            return "ResultLogin{" +
                    "code=" + code +
                    '}';
        }
    }


    public static final class ResultUserLogin {
        public int code;

        public ResultUserLogin(int code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return "ResultUserLogin{" +
                    "code=" + code +
                    '}';
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

//    /**
//     * 获取到http请求的结果
//     */
//    public static final class GetHttpDoneResult {
//        public JFGMsgHttpResult jfgMsgHttpResult;
//
//        public GetHttpDoneResult(JFGMsgHttpResult jfgMsgHttpResult) {
//            this.jfgMsgHttpResult = jfgMsgHttpResult;
//        }
//    }

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
        public int bindResult;
        public String uuid;

        public BindDeviceEvent(int jfgResult) {
            this.bindResult = jfgResult;
        }

        public BindDeviceEvent(int jfgResult, String uuid) {
            this.bindResult = jfgResult;
            this.uuid = uuid;
        }
    }

    public static final class UnBindDeviceEvent {
        public JFGResult jfgResult;

        public UnBindDeviceEvent(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    public static final class DeviceUnBindedEvent {
        public String uuid;

        public DeviceUnBindedEvent(String uuid) {
            this.uuid = uuid;
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
    @Deprecated
    public static final class DeviceListRsp {
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

//    /**
//     * 解绑设备
//     */
//    public static final class UnbindJFGDevice {
//        public String uuid;
//    }

    /**
     * 这个消息从{@link DataSource#OnRobotCountDataRsp(long, String, ArrayList)}
     * 传到{@link }
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
     * 历史录像数据响应
     */
    public static final class JFGHistoryVideoParseRsp {
        public String uuid;

        public JFGHistoryVideoParseRsp(String uuid) {
            this.uuid = uuid;
        }
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

        public JFGDoorBellCaller caller;
        public boolean isFromLocal = false;
        public LocalUdpMsg msg;

        public BellCallEvent() {
        }

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
    public static final class DeleteAddReqBack {
        public JFGResult jfgResult;

        public DeleteAddReqBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 检测是否已注册回调
     */
    public static final class CheckRegsiterBack {
        public JFGResult jfgResult;

        public CheckRegsiterBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 设置设备别名
     */
    public static final class SetAlias {
        public JFGResult result;

        public SetAlias(JFGResult result) {
            this.result = result;
        }
    }

    public static class AppHideEvent {
    }

    @Deprecated
    public static class EFamilyMsgpack {
        public int msgId;
        public byte[] data;
    }

    public static class CallResponse {
        public CallResponse(boolean self) {
            this.self = self;
        }

        public boolean self;
    }

//    public static class GetDataResponse {
//        public long seq;
//        public long msgId;
//        public boolean changed;
//    }

//    @Deprecated
//    public static class ParseResponseCompleted {
//        public long seq;
//        public String uuid;
//    }

    public static class DeviceSyncRsp {
        public DeviceSyncRsp setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public DeviceSyncRsp setUuid(String uuid, ArrayList<Long> idList) {
            this.uuid = uuid;
            this.idList = idList;
            return this;
        }

        public ArrayList<Long> idList;
        public String uuid;
    }

    public static class DeleteDataRsp {
        public long seq;
        public String peer;
        public int resultCode;

        public DeleteDataRsp(long l, String s, int i) {
            this.seq = l;
            this.peer = s;
            this.resultCode = i;
        }
    }

    public static class ErrorRsp extends RuntimeException {
        public int code;

        public ErrorRsp(int code) {
            this.code = code;
        }
    }

    public static class DeleteWonder {
        public int position;
    }

    public static class DeleteWonderRsp {
        public boolean success;
        public int position;

        public DeleteWonderRsp(boolean b, int position) {
            this.position = position;
            success = b;
        }
    }

    public static class SdcardClearRsp {
        public long seq;

        public SdcardClearRsp(long seq, ArrayList<JFGDPMsgRet> arrayList) {
            this.seq = seq;
            this.arrayList = arrayList;
        }

        public ArrayList<JFGDPMsgRet> arrayList;

    }

    public static class CheckDevVersionRsp implements Serializable {
        public boolean hasNew;
        public String url;

        public CheckDevVersionRsp(boolean hasNew, String url, String version, String tip, String md5) {
            this.hasNew = hasNew;
            this.url = url;
            this.version = version;
            this.tip = tip;
            this.md5 = md5;
        }

        public String version;
        public String tip;
        public String md5;
    }

    public static class LiveResponse {
        public boolean success;
        public Object response;

        public LiveResponse(Object disconnect, boolean success) {
            this.success = false;
            this.response = disconnect;
        }

        public LiveResponse(Object resolution) {
            this.success = true;
            this.response = resolution;
        }
    }

    public static class NetWorkChangeIntent {
        public boolean available;
        public Intent intent;
    }

    public static class NetConnectionEvent {
        public boolean available;

        public NetConnectionEvent(boolean available) {
            this.available = available;
        }
    }


    public static final class SetFriendMarkNameBack {
        public JFGResult jfgResult;

        public SetFriendMarkNameBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    public static final class RessetPhoneBack {
        public JFGResult jfgResult;

        public RessetPhoneBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    public static class SetDataRsp {
        public long seq;
        public ArrayList<JFGDPMsgRet> rets;

        public SetDataRsp(long l, ArrayList<JFGDPMsgRet> arrayList) {
            this.seq = l;
            this.rets = arrayList;
        }
    }

    public static class ClearDataEvent {
        public int msgId;

        public ClearDataEvent(int msgId) {
            this.msgId = msgId;
        }
    }

    public static class ThirdLoginTab {
        public boolean isThird;

        public ThirdLoginTab(boolean isThird) {
            this.isThird = isThird;
        }
    }

    public static class ShowWonderPageEvent {
    }
}
