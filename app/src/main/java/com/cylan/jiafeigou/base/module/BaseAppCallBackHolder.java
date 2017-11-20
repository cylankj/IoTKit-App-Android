package com.cylan.jiafeigou.base.module;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.DevUpgradeInfo;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgCount;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.JFGDPValue;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGDoorBellCaller;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGResult;
import com.cylan.entity.jniCall.JFGServerCfg;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.entity.jniCall.RobotMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jfgapp.interfases.AppCallBack;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.ver.PanDeviceVersionChecker;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.push.BellPuller;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.server.cache.CacheHolderKt;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.JFGGlideURL;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

/**
 * Created by yanzhendong on 2017/4/14.
 */

public class BaseAppCallBackHolder implements AppCallBack {
    /**
     * 不要在这个类里做复杂的逻辑处理,所有的消息都应该以 RxBus 发送出去,在对应的地方再做处理
     */
    private Gson gson = new Gson();

    @Override
    public void OnLocalMessage(String s, int i, byte[] bytes) {
//        AppLogger.d("OnLocalMessage :" + account + ",i:" + i);
        RxBus.getCacheInstance().post(new RxEvent.LocalUdpMsg(System.currentTimeMillis(), s, (short) i, bytes));
    }

    @Override
    public void OnReportJfgDevices(JFGDevice[] jfgDevices) {
        AppLogger.d("OnReportJfgDevices" + gson.toJson(jfgDevices));
//        for (JFGDevice device : jfgDevices) {
//            if (device.uuid.contains("46267")) {
//                device.alias = "hunt";
//                device.pid = 50;
//            }
//        }
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheDeviceEvent(jfgDevices));

//        CacheHolderKt.saveDevices(jfgDevices);
    }

    @Override
    public void OnUpdateAccount(JFGAccount jfgAccount) {
        AppLogger.d("OnUpdateAccount :" + gson.toJson(jfgAccount));
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheAccountEvent(jfgAccount));
    }

    @Override
    public void OnUpdateHistoryVideoList(JFGHistoryVideo jfgHistoryVideo) {
        AppLogger.d("OnUpdateHistoryVideoList :" + jfgHistoryVideo.list.size());
        BaseApplication.getAppComponent().getSourceManager().cacheHistoryDataList(jfgHistoryVideo);
    }

    @Override
    public void OnUpdateHistoryVideoV2(byte[] bytes) {

    }

    @Override
    public void OnUpdateHistoryErrorCode(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {
        AppLogger.d("OnUpdateHistoryErrorCode :" + gson.toJson(jfgHistoryVideoErrorInfo));
        RxBus.getCacheInstance().post(jfgHistoryVideoErrorInfo);
    }

    @Override
    public void OnServerConfig(JFGServerCfg jfgServerCfg) {
        AppLogger.d("OnServerConfig :" + gson.toJson(jfgServerCfg));
        RxBus.getCacheInstance().post(jfgServerCfg);
    }

    @Override
    public void OnLogoutByServer(int i) {
        AppLogger.d("OnLogoutByServer:" + i);
        RxBus.getCacheInstance().post(new RxEvent.PwdHasResetEvent(i));
        BaseApplication.getAppComponent().getSourceManager().setLoginState(new LogState(LogState.STATE_ACCOUNT_OFF));
    }

    @Override
    public void OnVideoDisconnect(JFGMsgVideoDisconn jfgMsgVideoDisconn) {
        AppLogger.d("OnVideoDisconnect :" + gson.toJson(jfgMsgVideoDisconn));
        RxBus.getCacheInstance().post(jfgMsgVideoDisconn);
    }

    @Override
    public void OnVideoNotifyResolution(JFGMsgVideoResolution jfgMsgVideoResolution) {
        AppLogger.d("OnVideoNotifyResolution" + jfgMsgVideoResolution.peer);
        RxBus.getCacheInstance().post(jfgMsgVideoResolution);
    }

    @Override
    public void OnVideoNotifyRTCP(JFGMsgVideoRtcp jfgMsgVideoRtcp) {
        Log.d("", "OnVideoNotifyRTCP" + gson.toJson(jfgMsgVideoRtcp));
        RxBus.getCacheInstance().post(jfgMsgVideoRtcp);
    }

    @Override
    public void OnHttpDone(JFGMsgHttpResult jfgMsgHttpResult) {
        AppLogger.d("OnHttpDone :" + gson.toJson(jfgMsgHttpResult));
        RxBus.getCacheInstance().post(jfgMsgHttpResult);
    }

    @Override
    public void OnRobotTransmitMsg(RobotMsg robotMsg) {
        AppLogger.d("OnRobotTransmitMsg :" + gson.toJson(robotMsg));
        RxBus.getCacheInstance().post(robotMsg);
    }

    @Override
    public void OnRobotMsgAck(int i) {
        AppLogger.d("OnRobotMsgAck :" + i);
    }

    @Override
    public void OnRobotGetDataRsp(RobotoGetDataRsp robotoGetDataRsp) {
        AppLogger.d("OnRobotGetDataRsp :" + gson.toJson(robotoGetDataRsp));
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheGetDataEvent(robotoGetDataRsp));


//        CacheHolderKt.saveProperty(robotoGetDataRsp.identity, (Map<Integer, List<?>>) (Object) robotoGetDataRsp.map, null);
    }

    @Override
    public void OnRobotGetDataExRsp(long l, String s, ArrayList<JFGDPMsg> arrayList) {
        RobotoGetDataRsp robotoGetDataRsp = new RobotoGetDataRsp();
        robotoGetDataRsp.identity = s;
        robotoGetDataRsp.seq = l;
        robotoGetDataRsp.put(-1, arrayList);//key在这种情况下无用
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheGetDataEvent(robotoGetDataRsp));
        AppLogger.d("OnRobotGetDataExRsp :" + s + "," + gson.toJson(arrayList));
    }

    @Override
    public void OnRobotSetDataRsp(long l, String uuid, ArrayList<JFGDPMsgRet> arrayList) {
        AppLogger.d("OnRobotSetDataRsp :" + l + gson.toJson(arrayList));
        RxBus.getCacheInstance().post(new RxEvent.SetDataRsp(l, uuid, arrayList));
    }

    @Override
    public void OnRobotGetDataTimeout(long l, String s) {
        AppLogger.d("OnRobotGetDataTimeout :" + l + ":" + s);
    }

    @Override
    public ArrayList<JFGDPMsg> OnQuerySavedDatapoint(String s, ArrayList<JFGDPMsg> arrayList) {
        AppLogger.e("这是一个bug");
        return null;
    }

    @Override
    public void OnlineStatus(boolean b) {
        if (b != BaseApplication.getAppComponent().getSourceManager().isOnline()) {
            AppLogger.d("OnlineStatus :" + b);
            RxBus.getCacheInstance().post(new RxEvent.OnlineStatusRsp(b));
            BaseApplication.getAppComponent().getSourceManager().setOnline(b);//设置用户在线信息
        }
    }

    @Override
    public void OnResult(JFGResult jfgResult) {
        RxBus.getCacheInstance().post(jfgResult);
        AppLogger.i("jfgResult [" + jfgResult.event + ":" + jfgResult.code + "]");
    }

    @Override
    public void OnDoorBellCall(JFGDoorBellCaller jfgDoorBellCaller) {
        AppLogger.d("OnDoorBellCall :" + gson.toJson(jfgDoorBellCaller));
//        RxBus.getCacheInstance().post(new RxEvent.BellCallEvent(jfgDoorBellCaller, false));
        try {
            String url = new JFGGlideURL(jfgDoorBellCaller.cid, jfgDoorBellCaller.time + ".jpg", jfgDoorBellCaller.regionType).toURL().toString();
             BellPuller.getInstance().launchBellLive(jfgDoorBellCaller.cid, url, jfgDoorBellCaller.time);
            AppLogger.d("门铃截图地址:" + url);
        } catch (MalformedURLException e) {
            AppLogger.e(e);
        }
    }

    @Override
    public void OnOtherClientAnswerCall(String s) {
        AppLogger.d("OnOtherClientAnswerCall:" + s);
        RxBus.getCacheInstance().post(new RxEvent.CallResponse(false));
    }

    @Override
    public void OnRobotCountDataRsp(long l, String s, ArrayList<JFGDPMsgCount> arrayList) {
        AppLogger.d("OnRobotCountDataRsp :" + l + ":" + s + "");
    }

    @Override
    public void OnRobotDelDataRsp(long l, String s, int i) {
        AppLogger.d("OnRobotDelDataRsp :" + l + " uuid:" + s + " i:" + i);
        RxBus.getCacheInstance().post(new RxEvent.DeleteDataRsp(l, s, i));
    }

    @Override
    public void OnRobotSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {
        AppLogger.d("OnRobotSyncData :" + b + " " + s + " " + new Gson().toJson(arrayList));
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheSyncDataEvent(b, s, arrayList));

        /*过渡性使用,将来会废弃*/
        CacheHolderKt.saveProperty(s, arrayList, null);
    }

    @Override
    public void OnSendSMSResult(int i, String s) {
        AppLogger.d("OnSendSMSResult :" + i + "," + s);
        //store the token .
        PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, s);
        RxBus.getCacheInstance().post(new RxEvent.SmsCodeResult(i, s));
    }

    @Override
    public void OnGetFriendListRsp(int ret, ArrayList<JFGFriendAccount> arrayList) {
        AppLogger.d("OnLocalMessage :" + arrayList.size());
//        arrayList = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            JFGFriendAccount req = new JFGFriendAccount();
//            req.account = "333" + i + "_hunt";
//            req.alias = "wth? " + i;
//            req.markName = "zi lai?";
//            arrayList.add(req);
//        }
        BaseApplication.getAppComponent().getSourceManager().setFriendsList(arrayList);
    }

    @Override
    public void OnGetFriendRequestListRsp(int ret, ArrayList<JFGFriendRequest> arrayList) {
        AppLogger.d("OnGetFriendRequestListRsp:" + arrayList.size());
//        AppLogger.d("测试专用");
//        arrayList = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            JFGFriendRequest req = new JFGFriendRequest();
//            req.account = "333" + i + "_hunt";
//            req.alias = "wth? " + i;
//            req.sayHi = " you son of...";
//            req.time = System.currentTimeMillis() - RandomUtils.getRandom(30) * 3600 * 1000;
//            arrayList.add(req);
//        }
        BaseApplication.getAppComponent().getSourceManager().setFriendsReqList(arrayList);
    }

    @Override
    public void OnGetFriendInfoRsp(int i, JFGFriendAccount jfgFriendAccount) {
        AppLogger.d("OnLocalMessage :" + new Gson().toJson(jfgFriendAccount));
        RxBus.getCacheInstance().post(new RxEvent.GetFriendInfoCall(i, jfgFriendAccount));
    }

    @Override
    public void OnCheckFriendAccountRsp(int i, String s, String s1, boolean b) {
        AppLogger.d("OnCheckFriendAccountRsp :");
        RxBus.getCacheInstance().post(new RxEvent.CheckAccountCallback(i, s, s1, b));
    }

    @Override
    public void OnShareDeviceRsp(int i, String s, String s1) {
        AppLogger.d("OnShareDeviceRsp :" + i + ":" + s + ":" + s1);
        RxBus.getCacheInstance().post(new RxEvent.ShareDeviceCallBack(i, s, s1));
    }

    @Override
    public void OnUnShareDeviceRsp(int i, String s, String s1) {
        AppLogger.d("OnUnShareDeviceRsp :" + i + "," + s + "," + s1);
        RxBus.getCacheInstance().post(new RxEvent.UnShareDeviceCallBack(i, s, s1));
    }

    @Override
    public void OnGetShareListRsp(int i, ArrayList<JFGShareListInfo> arrayList) {
        AppLogger.d("OnGetShareListRsp :" + i);
        BaseApplication.getAppComponent().getSourceManager().cacheShareList(arrayList);
    }

    @Override
    public void OnGetUnShareListByCidRsp(int i, ArrayList<JFGFriendAccount> arrayList) {
        AppLogger.d("UnShareListByCidEvent :");
        RxBus.getCacheInstance().post(new RxEvent.UnShareListByCidEvent(i, arrayList));
    }

    @Override
    public void OnUpdateNTP(int l) {
        AppLogger.d("OnUpdateNTP :" + l);
        PreferencesUtils.putInt(JConstant.KEY_NTP_INTERVAL, (int) (System.currentTimeMillis() / 1000 - l));
    }

    @Override
    public void OnForgetPassByEmailRsp(int i, String s) {
        AppLogger.d("OnForgetPassByEmailRsp :" + s);
        RxBus.getCacheInstance().post(new RxEvent.ForgetPwdByMail(s).setRet(i));
    }

    @Override
    public void OnGetAliasByCidRsp(int i, String s) {
        AppLogger.d("OnGetAliasByCidRsp :" + i + ":" + s);
    }

    @Override
    public void OnGetFeedbackRsp(int ret, ArrayList<JFGFeedbackInfo> arrayList) {
        AppLogger.d("OnGetFeedbackRsp :" + ListUtils.getSize(arrayList));
//        arrayList = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            JFGFeedbackInfo info = new JFGFeedbackInfo();
//            info.msg = "dfafa" + i;
//            info.time = System.currentTimeMillis() - RandomUtils.getRandom(20) * 3600;
//            arrayList.add(info);
//        }
        if (ListUtils.isEmpty(arrayList)) return;
        FeedbackManager.getInstance().cachePush(arrayList);
        BaseApplication.getAppComponent().getSourceManager().handleSystemNotification(arrayList);
    }


    @Override
    public void OnNotifyStorageType(int i) {
        AppLogger.d("OnNotifyStorageType:" + i);
        //此event是全局使用,不需要删除.因为在DataSourceManager需要用到.
        BaseApplication.getAppComponent().getSourceManager().setStorageType(i);
    }

    @Override
    public void OnBindDevRsp(int i, String s) {
        AppLogger.d("onBindDev: " + i + " uuid:" + s);
        RxBus.getCacheInstance().postSticky(new RxEvent.BindDeviceEvent(i, s));
        PreferencesUtils.putString(JConstant.BINDING_DEVICE, "");
    }

    @Override
    public void OnUnBindDevRsp(int i, String s) {
        AppLogger.d(String.format(Locale.getDefault(), "OnUnBindDevRsp:%d,%s", i, s));
    }

    @Override
    public void OnGetVideoShareUrl(String s) {
        AppLogger.d(String.format(Locale.getDefault(), "OnGetVideoShareUrl:%s", s));
        RxBus.getCacheInstance().post(new RxEvent.GetVideoShareUrlEvent(s));
    }

    @Override
    public void OnForwardData(byte[] bytes) {
        try {
            PanoramaEvent.MsgForward rawRspMsg = DpUtils.unpackData(bytes, PanoramaEvent.MsgForward.class);
            RxBus.getCacheInstance().post(rawRspMsg);
            BaseForwardHelper.getInstance().dispatcherForward(rawRspMsg);
        } catch (IOException e) {
            e.printStackTrace();
            AppLogger.e("解析服务器透传消息失败");
        }
    }

    @Override
    public void OnMultiShareDevices(int i, String s, String s1) {
        AppLogger.d(String.format(Locale.getDefault(), "check OnMultiShareDevices:%d,%s,%s", i, s, s1));
        RxBus.getCacheInstance().post(new RxEvent.MultiShareDeviceEvent(i, s, s1));
    }

    @Override
    public void OnCheckClientVersion(int i, String s, int i1) {
        RxBus.getCacheInstance().post(new RxEvent.ClientCheckVersion(i, s, i1));
    }

    @Override
    public void OnRobotCountMultiDataRsp(long l, Object o) {


        AppLogger.d("OnRobotCountMultiDataRsp:" + o.toString());
    }

    @Override
    public void OnRobotGetMultiDataRsp(long l, Object o) {
        AppLogger.d("OnRobotGetMultiDataRsp:" + l + ":" + o);

//        ObjectMapper mapper = CacheHolderKt.getObjectMapper().get();
//
//        try {
//            byte[] asBytes = mapper.writeValueAsBytes(o);
//
//            TypeReference<Map<String, Map<Long, List<JFGDPValue>>>> typeReference = new TypeReference<Map<String, Map<Long, List<JFGDPValue>>>>() {
//            };
//            Object v = mapper.readValue(asBytes, typeReference);
//
//            Log.e("AAAA", "value is" + v);
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        if (o != null && o instanceof HashMap) {
            HashMap<String, HashMap<Long, JFGDPValue[]>> rawMap = (HashMap<String, HashMap<Long, JFGDPValue[]>>) o;
            Set<String> set = rawMap.keySet();
            final int count = set.size();
            for (String uuid : set) {
                RobotoGetDataRsp rsp = new RobotoGetDataRsp();
                rsp.identity = uuid;
                rsp.seq = l;
                rsp.map = new HashMap<>();
                HashMap<Long, JFGDPValue[]> map = rawMap.get(uuid);
                for (Long lll : map.keySet()) {
                    ArrayList<JFGDPMsg> msgList = new ArrayList<>();
                    int msgId = (int) ((long) lll);
                    for (JFGDPValue j : map.get(lll)) {
                        JFGDPMsg msg = new JFGDPMsg();
                        msg.id = msgId;
                        msg.version = j.version;
                        msg.packValue = j.value;
                        msgList.add(msg);
                    }
                    rsp.map.put(msgId, msgList);
                }
                RxBus.getCacheInstance().post(new RxEvent.SerializeCacheGetDataEvent(rsp));
            }

//            CacheHolderKt.saveProperty((Map<String, Map<Long, JFGDPValue[]>>) (Object) rawMap, HashStrategyFactory.INSTANCE::select);
            Log.d("OnRobotGetMultiDataRsp", "size: " + count);
        }
    }


    @Override
    public void OnGetAdPolicyRsp(int i, long l, String picUrl, String tagUrl) {
        AppLogger.d("OnGetAdPolicyRsp:" + l + ":" + picUrl);
//        l = System.currentTimeMillis() + 2 * 60 * 1000;
//        tagUrl = "http://www.baidu.com";
//        picUrl = "http://cdn.duitang.com/uploads/item/201208/19/20120819131358_2KR2S.thumb.600_0.png";
        RxBus.getCacheInstance().postSticky(new RxEvent.AdsRsp().setPicUrl(picUrl).setTagUrl(tagUrl)
                .setRet(i).setTime(l));
    }

    //final boolean hasNew, final String url, final String version,
    // final String tip, final String md5, final String cid
    @Override
    public void OnCheckDevVersionRsp(boolean b, String url, String tagVersion,
                                     String tip, String md5, String cid) {
        AppLogger.d("OnCheckDevVersionRsp :" + b + ":" + url + ":" + tagVersion
                + ":" + tip + ":" + md5 + "," + cid);
//        isFriend = true;
//        account = "http://yf.cylan.com.cn:82/Garfield/JFG2W/3.0.0/3.0.0.1000/201704261515/hi.bin";
//        alias = "3.0.0";
//        s2 = "你好";
//        s3 = "xx";
        if (!b) {
            PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CONTENT + cid);
        }
        ArrayList<DevUpgradeInfo> arrayList = new ArrayList<>();
        DevUpgradeInfo info = new DevUpgradeInfo();
        info.md5 = md5;
        info.tag = 0;
        info.url = url;
        info.version = tagVersion;
        arrayList.add(info);
        PanDeviceVersionChecker.BinVersion version = new PanDeviceVersionChecker.BinVersion();
        version.setCid(cid);
        version.setContent(tip);
        version.setList(arrayList);
        version.setTagVersion(tagVersion);
        RxBus.getCacheInstance().post(new RxEvent.VersionRsp().setUuid(cid).setVersion(version));
    }


    @Override
    public void OnCheckTagDeviceVersionRsp(int ret, String cid,
                                           String tagVersion,
                                           String content,
                                           ArrayList<DevUpgradeInfo> arrayList) {
        AppLogger.d("OnCheckTagDeviceVersionRsp:" + ret + ":" + cid + ",:" + tagVersion + "," + new Gson().toJson(arrayList));
//        arrayList = testList();
//        cid = "290000000065";
//        tagVersion = "1.0.0.009";
//        content = "test";
        if (!TextUtils.isEmpty(cid)) {
            if (ret != 0 || ListUtils.isEmpty(arrayList))
                PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CONTENT + cid);
        } else return;
        PanDeviceVersionChecker.BinVersion version = new PanDeviceVersionChecker.BinVersion();
        version.setCid(cid);
        version.setContent(content);
        version.setList(arrayList);
        version.setTagVersion(tagVersion);
        RxBus.getCacheInstance().post(new RxEvent.VersionRsp().setUuid(cid).setVersion(version));
    }

    @Override
    public void OnUniversalDataRsp(long l, int i, byte[] bytes) {
        try {
            Object value = CacheHolderKt.getObjectMapper().get().readValue(bytes, Object.class);
            Log.i(JConstant.CYLAN_TAG, "OnUniversalDataRsp:" + value);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RxBus.getCacheInstance().post(new RxEvent.UniversalDataRsp(l, i, bytes));
    }

//    private ArrayList<DevUpgradeInfo> testList() {
//        ArrayList<DevUpgradeInfo> list = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            DevUpgradeInfo info = new DevUpgradeInfo();
//            info.md5 = "";
//            info.tag = i;
//            info.url = tmp[i];
//            info.version = "1.0.0.009";
//            list.add(info);
//        }
//        return list;
//    }

//    private static final String[] tmp = new String[]{
//            "http://oss-cn-hangzhou.aliyuncs.com/jiafeigou-yf/package/21/JFG5W-1.0.0.009-Kernel.bin?Expires=1527472979&Signature=m2KroyFfNhOVZi1YmzLWh14NUU4%3D&OSSAccessKeyId=xjBdwD1du8lf2wMI",
//            "http://yf.cylan.com.cn:82/Garfield/Android-New/cylan/201706021000-3.2.0.286/ChangeLog.txt",
//            "http://yf.cylan.com.cn:82/Garfield/JFG2W/3.0.0/3.0.0.1000/201704261515/hi.bin",
//            "http://tse4.mm.bing.net/th?id=OIP.QxZxJAfP-lq-OxYjS3bFLAFNC7&pid=15.1",
//            "http://a2.att.hudong.com/18/04/14300000931600128341040320614.jpg"
//    };
}
