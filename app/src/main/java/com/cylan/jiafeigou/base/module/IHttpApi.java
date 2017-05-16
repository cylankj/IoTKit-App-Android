package com.cylan.jiafeigou.base.module;


import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import rx.Observable;

/**
 * Created by yanzhendong on 2017/5/8.
 */

public interface IHttpApi {
    String CGI = "cgi/ctrl.cgi";

    @GET("images/{fileName}")
    @Streaming
    Observable<ResponseBody> download(@Path("fileName") String fileName, @Header("Range") String range);

//    @GET("thumb/{thumb}.thumb")
//    Observable<File> getThumbPicture(@Path("thumb") String thumb);

    /**
     * @param deleteType -1:全部删除;0:反向删除,即选中的不删除;1:正向删除,即选中的删除;
     */

    @GET(CGI + "?Msg=fileDelete")
    Observable<PanoramaEvent.MsgFileRsp> delete(@Query("deltype") int deleteType, @Query("filename") List<String> files);

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
    Observable<PanoramaEvent.MsgRsp> setLogo(@Query("req") int logType);

    @GET(CGI + "?Msg=setResolution")
    Observable<PanoramaEvent.MsgRsp> setResolution(@Query("videoStandard") int videoStandard);

    @GET(CGI + "?Msg=getLog")
    Observable<PanoramaEvent.MsgLogoRsp> getLogo();

    @GET(CGI + "?Msg=getResolution")
    Observable<PanoramaEvent.MsgResolutionRsp> getResolution();

}
