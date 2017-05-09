package com.cylan.jiafeigou.base.module;


import java.io.File;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by yanzhendong on 2017/5/8.
 */

public interface IHttpApi {
    String CGI = "cgi/ctrl.cgi";

    @GET("images/{fileName}")
    Observable<File> download(@Path("fileName") String fileName);

    @GET("thumb/{thumb}.thumb")
    Observable<File> getThumbPicture(@Path("thumb") String thumb);

    @GET(CGI + "?Msg=fileDelete")
    Observable<PanoramaEvent.MsgFileRsp> delete(@Query("deltype") int deleteType, @Query("filename") String fileName);

    @GET(CGI + "?Msg=getFileList")
    Observable<PanoramaEvent.MsgFileListRsp> getFileList(@Query("begintime") int beginTime, @Query("endtime") int endTime, @Query("count") int count);

    @GET(CGI + "?Msg=snapShot")
    Observable<PanoramaEvent.MsgFileRsp> snapShot();

    @GET(CGI + "?Msg=startRec")
    Observable<PanoramaEvent.MsgRsp> startRec(@Query("videoType") int videoType);

    @GET(CGI + "?Msg=stopRec")
    Observable<PanoramaEvent.MsgFileRsp> stopRec(@Query("videoType") int videoType);

    @GET(CGI + "?Msg=getRecStatus")
    Observable<PanoramaEvent.MsgVideoStatusRsp> getRecStatus();

    @GET(CGI + "?Msg=sdFormat")
    Observable<PanoramaEvent.MsgSdInfoRsp> sdFormat();

    @GET(CGI + "?Msg=getSdInfo")
    Observable<PanoramaEvent.MsgSdInfoRsp> getSdInfo();

    @GET(CGI + "?Msg=getPowerLine")
    Observable<PanoramaEvent.MsgPowerLineRsp> getPowerLine();

    @GET(CGI + "?Msg=getBattery")
    Observable<PanoramaEvent.MsgBatteryRsp> getBattery();

    @GET(CGI + "?Msg=setLog")
    Observable<PanoramaEvent.MsgRsp> setLogo(@Query("logType") int logType);

    @GET(CGI + "?Msg=setResolution")
    Observable<PanoramaEvent.MsgRsp> setResolution(@Query("videoStandard") int videoStandard);

    @GET(CGI + "?Msg=getLog")
    Observable<PanoramaEvent.MsgLogoRsp> getLogo();

    @GET(CGI + "?Msg=getResolution")
    Observable<PanoramaEvent.MsgResolutionRsp> getResolution();

}
