package com.cylan.jiafeigou.rx;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGDoorBellCaller;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGResult;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.module.PanoramaEvent;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.module.FeedBackBean;
import com.cylan.jiafeigou.misc.ver.AbstractVersion;
import com.cylan.udpMsgPack.JfgUdpMsg;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cylan.jiafeigou.rx.RxEvent.UpdateType.GOOGLE_PLAY;
import static com.cylan.jiafeigou.rx.RxEvent.UpdateType._8HOUR;


/**
 * Created by cylan-hunt on 16-7-6.
 */
public class RxEvent {


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
        public JFGResult result;

        public ResultVerifyCode(int code) {
            this.code = code;
        }

        public ResultVerifyCode setResult(JFGResult result) {
            this.result = result;
            return this;
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
        public int ret;

        public ForgetPwdByMail(String account) {
            this.account = account;
        }

        public ForgetPwdByMail setRet(int ret) {
            this.ret = ret;
            return this;
        }

    }

    public static final class PageScrolled {
        public PageScrolled(int index) {
            this.index = index;
        }

        public int index;

    }

    /**
     * desc:获取好友列表类
     */
    public static final class GetFriendList {
    }

    /**
     * desc：获取添加请求类
     */
    public static final class GetAddReqList {
    }


    public static final class LocalUdpMsg {
        //消息的时间,可以用来判断有效性.
        public long time;
        public String ip;
        public short port;
        public byte[] data;

        public LocalUdpMsg() {
        }

        public LocalUdpMsg(long time, String ip, short port, byte[] data) {
            this.time = time;
            this.ip = ip;
            this.port = port;
            this.data = data;
        }

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
        public String reason;

        public BindDeviceEvent(int jfgResult) {
            this.bindResult = jfgResult;
        }

        public BindDeviceEvent(int jfgResult, String uuid, String reason) {
            this.bindResult = jfgResult;
            this.uuid = uuid;
            this.reason = reason;
        }

        @Override
        public String toString() {
            return "BindDeviceEvent{" +
                    "bindResult=" + bindResult +
                    ", uuid='" + uuid + '\'' +
                    ", reason='" + reason + '\'' +
                    '}';
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
        public int code;

        public CheckAccountCallback(int code, String account, String alias, boolean isFriend) {
            this.code = code;
            this.account = account;
            this.alias = alias;
            this.isFriend = isFriend;
        }

        public String account;
        public String alias;
        public boolean isFriend;
    }

    /**
     * 获取到已经分享的好友的回调
     */
    public static final class UnShareListByCidEvent {
        public int i;
        public ArrayList<JFGFriendAccount> arrayList;

        public UnShareListByCidEvent(int i, ArrayList<JFGFriendAccount> arrayList) {
            this.i = i;
            this.arrayList = arrayList;
        }
    }

    /**
     * 取消分享的回调
     */
    public static final class UnShareDeviceCallBack {
        public int i;
        public String cid;
        public String account;

        public UnShareDeviceCallBack(int i, String cid, String account) {
            this.i = i;
            this.cid = cid;
            this.account = account;
        }
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

//    /**
//     * 这个消息从{@link DataSourceService#OnRobotCountDataRsp(long, String, ArrayList)}
//     * 传到{@link }
//     */
//    public static final class UnreadCount {
//        public String uuid;
//        public long seq;
//        public ArrayList<JFGDPMsgCount> msgList;
//
//        public UnreadCount() {
//        }
//
//        public UnreadCount(String uuid, long seq, ArrayList<JFGDPMsgCount> counts) {
//            this.uuid = uuid;
//            this.seq = seq;
//            this.msgList = counts;
//        }
//    }

    public static final class HistoryBack {
        public boolean isEmpty = false;

        public HistoryBack(boolean isEmpty) {
            this.isEmpty = isEmpty;
        }
    }

    /**
     * 历史录像数据响应
     */
    public static final class JFGHistoryVideoParseRsp {
        public String uuid;
        public long timeStart;

        public JFGHistoryVideoParseRsp(String uuid) {
            this.uuid = uuid;
        }

        public JFGHistoryVideoParseRsp setTimeStart(long time) {
            this.timeStart = time;
            return this;
        }
    }

    /**
     * 系统反馈回复
     */
    public static final class GetFeedBackRsp {
        public ArrayList<FeedBackBean> newList;

        public GetFeedBackRsp(ArrayList<FeedBackBean> newList) {
            this.newList = newList;
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

        public BellCallEvent(JFGDoorBellCaller caller, boolean isFromLocal) {
            this.caller = caller;
            this.isFromLocal = isFromLocal;
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

    public static final class SendLogRsp {
        public FeedBackBean bean;

        public SendLogRsp setTime(FeedBackBean bean) {
            this.bean = bean;
            return this;
        }
    }

    /**
     * 发送反馈的返回
     */
    public static final class SendFeedBack {
        public JFGResult jfgResult;

        public SendFeedBack(JFGResult jfgResult) {
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
    public static final class CheckRegisterBack {
        public JFGResult jfgResult;

        public CheckRegisterBack(JFGResult jfgResult) {
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

        public DeviceSyncRsp setUuid(String uuid, ArrayList<Long> idList, ArrayList<JFGDPMsg> dpList) {
            this.uuid = uuid;
            this.idList = idList;
            this.dpList = dpList;
            return this;
        }

        public ArrayList<JFGDPMsg> dpList;
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

    public static class DeletePanoramaItem {
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

//    public static class SdcardClearReqRsp {
//        public long seq;
//
//        public SdcardClearReqRsp(long seq, ArrayList<JFGDPMsgRet> arrayList) {
//            this.seq = seq;
//            this.arrayList = arrayList;
//        }
//
//        public ArrayList<JFGDPMsgRet> arrayList;
//
//    }


    public static class CheckVersionRsp implements Parcelable {
        public long seq;
        public boolean hasNew;
        public int forceUpdate;
        public long fileSize;
        public int downloadState;
        public long lastUpdateTime;
        public String url;
        public String version;
        public String versionCode;
        public String tip;
        public String md5;
        public String fileDir;
        public String fileName;
        public String uuid;
        //存储该类在Pre中的key
        public String preKey;

        public CheckVersionRsp setSeq(long seq) {
            this.seq = seq;
            return this;
        }

        public CheckVersionRsp(boolean hasNew,
                               String url,
                               String version,
                               String tip,
                               String md5) {
            this.hasNew = hasNew;
            this.url = url;
            this.version = version;
            this.tip = tip;
            this.md5 = md5;
        }

        public CheckVersionRsp setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public CheckVersionRsp setPreKey(String preKey) {
            this.preKey = preKey;
            return this;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.seq);
            dest.writeByte(this.hasNew ? (byte) 1 : (byte) 0);
            dest.writeLong(this.fileSize);
            dest.writeInt(this.downloadState);
            dest.writeLong(this.lastUpdateTime);
            dest.writeString(this.url);
            dest.writeString(this.version);
            dest.writeString(this.versionCode);
            dest.writeString(this.tip);
            dest.writeString(this.md5);
            dest.writeString(this.fileDir);
            dest.writeString(this.fileName);
            dest.writeString(this.uuid);
            dest.writeString(this.preKey);
        }

        protected CheckVersionRsp(Parcel in) {
            this.seq = in.readLong();
            this.hasNew = in.readByte() != 0;
            this.fileSize = in.readLong();
            this.downloadState = in.readInt();
            this.lastUpdateTime = in.readLong();
            this.url = in.readString();
            this.version = in.readString();
            this.versionCode = in.readString();
            this.tip = in.readString();
            this.md5 = in.readString();
            this.fileDir = in.readString();
            this.fileName = in.readString();
            this.uuid = in.readString();
            this.preKey = in.readString();
        }

        @Override
        public String toString() {
            return "CheckVersionRsp{" +
                    "seq=" + seq +
                    ", hasNew=" + hasNew +
                    ", fileSize=" + fileSize +
                    ", downloadState=" + downloadState +
                    ", lastUpdateTime=" + lastUpdateTime +
                    ", url='" + url + '\'' +
                    ", version='" + version + '\'' +
                    ", versionCode='" + versionCode + '\'' +
                    ", tip='" + tip + '\'' +
                    ", md5='" + md5 + '\'' +
                    ", fileDir='" + fileDir + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", uuid='" + uuid + '\'' +
                    ", preKey='" + preKey + '\'' +
                    '}';
        }

        public static final Creator<CheckVersionRsp> CREATOR = new Creator<CheckVersionRsp>() {
            @Override
            public CheckVersionRsp createFromParcel(Parcel source) {
                return new CheckVersionRsp(source);
            }

            @Override
            public CheckVersionRsp[] newArray(int size) {
                return new CheckVersionRsp[size];
            }
        };
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
        public boolean isOnLine = false;
        public boolean available;
        public NetworkInfo mobile;
        public NetworkInfo wifi;

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

    public static final class RessetAccountBack {
        public JFGResult jfgResult;

        public RessetAccountBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    public static class SetDataRsp {
        public long seq;
        public String uuid;
        public ArrayList<JFGDPMsgRet> rets;

        public SetDataRsp(long l, String uuid, ArrayList<JFGDPMsgRet> arrayList) {
            this.seq = l;
            this.uuid = uuid;
            this.rets = arrayList;
        }

        public SetDataRsp setUuid(String uuid) {
            this.uuid = uuid;
            return this;
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

    public static class DevicesArrived {
        public List<Device> devices;

        public DevicesArrived(List<Device> devices) {
            this.devices = devices;
        }
    }

    public static class AccountArrived {
        public Account account;
        public JFGAccount jfgAccount;

        public AccountArrived(Account account) {
            this.account = account;
        }
    }

    public static final class PwdHasResetEvent {
        public PwdHasResetEvent(int code) {
            this.code = code;
        }

        public int code;
    }

    //DataSouManager 专用,其他场景不要用这个类
    public static final class SerializeCacheAccountEvent {

        public final JFGAccount account;

        public SerializeCacheAccountEvent(JFGAccount jfgAccount) {
            this.account = jfgAccount;
        }
    }

    //DataSouManager 专用,其他场景不要用这个类
    public static final class SerializeCacheDeviceEvent {

        public final JFGDevice[] devices;

        public SerializeCacheDeviceEvent(JFGDevice[] jfgDevices) {
            this.devices = jfgDevices;
        }
    }

    //DataSouManager 专用,其他场景不要用这个类
    public static final class SerializeCacheGetDataEvent {

        public final RobotoGetDataRsp getDataRsp;

        public SerializeCacheGetDataEvent(RobotoGetDataRsp robotoGetDataRsp) {
            this.getDataRsp = robotoGetDataRsp;
        }
    }

    //DataSouManager 专用,其他场景不要用这个类
    public static final class SerializeCacheSyncDataEvent {

        public final boolean b;
        public final String s;
        public final ArrayList<JFGDPMsg> arrayList;

        public SerializeCacheSyncDataEvent(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {
            this.b = b;
            this.s = s;
            this.arrayList = arrayList;
        }
    }

//    /**
//     * {@link com.cylan.jfgapp.interfases.AppCallBack#OnRobotSyncData(boolean, String, ArrayList)}
//     * {@link com.cylan.jiafeigou.dp.DpMsgMap#ID_505_CAMERA_ALARM_MSG}
//     * {@link com.cylan.jiafeigou.dp.DpMsgMap#ID_512_CAMERA_ALARM_MSG_V3}
//     * {@link com.cylan.jiafeigou.dp.DpMsgMap#ID_401_BELL_CALL_STATE}
//     */
//    public static final class ForSystemNotification {
//
//        public final boolean isFriend;
//        public final String account;
//        public final ArrayList<JFGDPMsg> arrayList;
//
//        public ForSystemNotification(boolean isFriend, String account, ArrayList<JFGDPMsg> arrayList) {
//            this.isFriend = isFriend;
//            this.account = account;
//            this.arrayList = arrayList;
//        }
//    }

    /**
     * 从我的界面登录标记
     */
    public static final class LoginMeTab {
        public LoginMeTab(boolean b) {
            this.b = b;
        }

        public boolean b;
    }

    public static final class PanoramaConnection {

    }

    public static final class ClientCheckVersion {
        public int ret;
        public String result;
        public int forceUpgrade;//强制升级

        public ClientCheckVersion(int ret, String result, int forceUpgrade) {
            this.ret = ret;
            this.result = result;
            this.forceUpgrade = forceUpgrade;
        }

        @Override
        public String toString() {
            return "ClientCheckVersion{" +
                    "ret=" + ret +
                    ", result='" + result + '\'' +
                    ", forceUpgrade=" + forceUpgrade +
                    '}';
        }
    }

    public static final class ClientUpdateEvent {
        public long currentByte;
        public long totalByte;
        public int state;
        public Throwable throwable;
        public int forceUpdate;

        public ClientUpdateEvent setCurrentByte(long currentByte) {
            this.currentByte = currentByte;
            return this;
        }

        public ClientUpdateEvent setTotalByte(long totalByte) {
            this.totalByte = totalByte;
            return this;
        }

        public ClientUpdateEvent setState(int state) {
            this.state = state;
            return this;
        }

        public ClientUpdateEvent setThrowable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        public ClientUpdateEvent setForceUpdate(int forceUpdate) {
            this.forceUpdate = forceUpdate;
            return this;
        }
    }

    public static final class SetWifiAck {
        public JfgUdpMsg.DoSetWifiAck data;

        public SetWifiAck(JfgUdpMsg.DoSetWifiAck data) {
            this.data = data;
        }
    }

    public static class NeedUpdateGooglePlayService {

    }

    public static class VideoLoadingEvent {
        public final boolean slow;

        public VideoLoadingEvent(boolean slow) {
            this.slow = slow;
        }
    }

    public static class GlobalInitFinishEvent {
        public static GlobalInitFinishEvent INSTANCE = new GlobalInitFinishEvent();
    }
//

    //
    public static final class FirmwareUpdateRsp {
        public String uuid;

        public FirmwareUpdateRsp(String uuid) {
            this.uuid = uuid;
        }
    }

    public static final class VersionRsp {
        public AbstractVersion.BinVersion version;
        public String uuid;

        public AbstractVersion.BinVersion getVersion() {
            return version;
        }

        public String getUuid() {
            return uuid;
        }

        public VersionRsp setVersion(AbstractVersion.BinVersion version) {
            this.version = version;
            return this;
        }

        public VersionRsp setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        @Override
        public String toString() {
            return "VersionRsp{" +
                    "version=" + version +
                    ", uuid='" + uuid + '\'' +
                    '}';
        }
    }
//    public static final class VersionRsp<T extends IVersion.BaseVersion> {
//        public String uuid;
//        public T version;
//
//        public VersionRsp<T> setUuid(String uuid) {
//            this.uuid = uuid;
//            return this;
//        }
//
//        public VersionRsp<T> setVersion(T version) {
//            this.version = version;
//            return this;
//        }
//
//        public T getVersion() {
//            return version;
//        }
//    }

//    public static class NewVersionApkDesc {
//        public String url;
//        public String fileName;
//        public String fileDir;
//        public String desc;
//        public String versionName;
//        public String versionCode;
//        public int downloadState;
//
//        public NewVersionApkDesc() {
//        }
//
//        @Override
//        public String toString() {
//            return "NewVersionApkDesc{" +
//                    "url='" + url + '\'' +
//                    ", fileName='" + fileName + '\'' +
//                    ", fileDir='" + fileDir + '\'' +
//                    ", desc='" + desc + '\'' +
//                    ", versionName='" + versionName + '\'' +
//                    ", versionCode='" + versionCode + '\'' +
//                    ", downloadState=" + downloadState +
//                    '}';
//        }
//
//        public NewVersionApkDesc setVersionName(String versionName) {
//            this.versionName = versionName;
//            return this;
//        }
//
//        public NewVersionApkDesc setVersionCode(String versionCode) {
//            this.versionCode = versionCode;
//            return this;
//        }
//
//        public NewVersionApkDesc setUrl(String url) {
//            this.url = url;
//            return this;
//        }
//
//        public NewVersionApkDesc setDesc(String desc) {
//            this.desc = desc;
//            return this;
//        }
//
//        public NewVersionApkDesc setFileName(String fileName) {
//            this.fileName = fileName;
//            return this;
//        }
//
//        public NewVersionApkDesc setFileDir(String fileDir) {
//            this.fileDir = fileDir;
//            return this;
//        }
//
//        public NewVersionApkDesc setDownloadState(int downloadState) {
//            this.downloadState = downloadState;
//            return this;
//        }
//    }

    public static class HelperBreaker extends RuntimeException {
        public int breakerCode;
        public RxEvent.LocalUdpMsg localUdpMsg;

        public Object object;

        public HelperBreaker setValue(RxEvent.LocalUdpMsg localUdpMsg) {
            this.localUdpMsg = localUdpMsg;
            return this;
        }

        public HelperBreaker() {
        }

        public HelperBreaker(Object o) {
            this.object = o;
        }

        public HelperBreaker(int breakerCode) {
            this.breakerCode = breakerCode;
        }


        public HelperBreaker(int breakerCode, Object breaker) {
            this.breakerCode = breakerCode;
            this.object = breaker;
        }

        public HelperBreaker(String detailMessage) {
            super(detailMessage);
        }

        public HelperBreaker(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
            this.initCause(throwable);
        }

        public HelperBreaker(Throwable throwable) {
            super(throwable);
        }

        @Override
        public String getLocalizedMessage() {
            return super.getLocalizedMessage();
        }

        @Override
        public String getMessage() {
            return super.getMessage();
        }
    }

    @IntDef({
            GOOGLE_PLAY,
            _8HOUR,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface UpdateType {
        int GOOGLE_PLAY = 100;
        int _8HOUR = 200;
    }

    public static class ApkDownload {
        public String filePath;
        public int forceUpdate;
        public
        @UpdateType
        int updateType;//google play或者 直接安装

        public ApkDownload(String filePath) {
            this.filePath = filePath;
        }

        public ApkDownload setForceUpdate(int forceUpdate) {
            this.forceUpdate = forceUpdate;
            return this;
        }

        public ApkDownload setUpdateType(@UpdateType int updateType) {
            this.updateType = updateType;
            return this;
        }
    }

    public static class FetchDeviceInformation {
        public boolean success;

        public FetchDeviceInformation(boolean success) {
            this.success = success;
        }

        public static final FetchDeviceInformation STARTED = new FetchDeviceInformation(false);
        public static final FetchDeviceInformation SUCCESS = new FetchDeviceInformation(true);
    }

    public static class PanoramaApiAvailable {
        public int ApiType;

        public PanoramaApiAvailable(int apiType) {
            this.ApiType = apiType;
        }

        public static final PanoramaApiAvailable API_HTTP = new PanoramaApiAvailable(0);
        public static final PanoramaApiAvailable API_FORWARD = new PanoramaApiAvailable(1);
        /**
         * @deprecated 如果无法使用 http 接口,则默认使用透传接口,而不应该出现不可用的情况
         */
        public static final PanoramaApiAvailable API_NOT_AVAILABLE = new PanoramaApiAvailable(-1);
    }

    public static class GetVideoShareUrlEvent {
        public String url;

        public GetVideoShareUrlEvent(String s) {
            this.url = s;
        }
    }

    public static class ReportMsgEvent {
        public String cid;
        public PanoramaEvent.MsgForward forward;

        public ReportMsgEvent(String cid, PanoramaEvent.MsgForward forward) {
            this.cid = cid;
            this.forward = forward;
        }
    }

    public static class AdsRsp {
        public int ret;
        public long time;
        public String picUrl;
        public String tagUrl;

        public AdsRsp() {
        }

        public AdsRsp setRet(int ret) {
            this.ret = ret;
            return this;
        }

        public AdsRsp setTime(long time) {
            this.time = time;
            return this;
        }

        public AdsRsp setPicUrl(String picUrl) {
            this.picUrl = picUrl;
            return this;
        }

        public AdsRsp setTagUrl(String tagUrl) {
            this.tagUrl = tagUrl;
            return this;
        }

        @Override
        public String toString() {
            return "AdsRsp{" +
                    "ret=" + ret +
                    ", time=" + time +
                    ", picUrl='" + picUrl + '\'' +
                    ", tagUrl='" + tagUrl + '\'' +
                    '}';
        }
    }

    /**
     * 什么氢气列表,朋友列表,都包含
     */
    public static class AllFriendsRsp {
    }

    /**
     * 用户刷新 mine 的红点
     */
    public static final class InfoUpdate {
    }

    public static class DeviceRecordStateChanged {
        public static final DeviceRecordStateChanged INSTANCE = new DeviceRecordStateChanged();
    }

    public static class RxNotification {
        public String which;
        public Object value;
    }

    public static class MultiShareDeviceEvent {
        public int ret;
        public String device;
        public String account;

        public MultiShareDeviceEvent(int i, String s, String s1) {
            this.ret = i;
            this.device = s;
            this.account = s1;
        }
    }

    public static final class InitFrom2x {
        public static final InitFrom2x INSTANCE = new InitFrom2x();
    }

    public static class UniversalDataRsp {
        public long seq;
        public int ret;
        public byte[] data;

        public UniversalDataRsp(long seq, int ret, byte[] data) {
            this.seq = seq;
            this.ret = ret;
            this.data = data;
        }
    }

    public static class ActivityStartEvent {


    }
}


