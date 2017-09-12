/*
 * Copyright (c) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.cylan.jiafeigou.rtmp.youtube.util;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.LiveBroadcasts.Transition;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.CdnSettings;
import com.google.api.services.youtube.model.IngestionInfo;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastContentDetails;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.google.api.services.youtube.model.LiveBroadcastSnippet;
import com.google.api.services.youtube.model.LiveBroadcastStatus;
import com.google.api.services.youtube.model.LiveStream;
import com.google.api.services.youtube.model.LiveStreamListResponse;
import com.google.api.services.youtube.model.LiveStreamSnippet;
import com.google.api.services.youtube.model.MonitorStreamInfo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class YouTubeApi {

    public static final String RTMP_URL_KEY = "rtmpUrl";
    public static final String BROADCAST_ID_KEY = "broadcastId";
    private static final int FUTURE_DATE_OFFSET_MILLIS = 5 * 1000;
    private static YouTube youTube;

    public static EventData createLiveEvent(YouTube youtube, String description,
                                            String title, long startTime, long endTime) throws IOException {
        // We need a date that's in the proper ISO format and is in the future,
        // since the API won't
        // create events that start in the past.
        LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
        broadcastSnippet.setTitle(title);
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        broadcastSnippet.setScheduledStartTime(new DateTime(new Date(startTime), TimeZone.getDefault()));
        if (endTime > startTime) {
            broadcastSnippet.setScheduledEndTime(new DateTime(new Date(endTime), TimeZone.getDefault()));
        }
        broadcastSnippet.setDescription(description);

        LiveBroadcastContentDetails contentDetails = new LiveBroadcastContentDetails();
        MonitorStreamInfo monitorStream = new MonitorStreamInfo();
        monitorStream.setEnableMonitorStream(true);
        contentDetails.setMonitorStream(monitorStream);
        contentDetails.set("projection", "360");//这里控制以360 视角直播

        // Create LiveBroadcastStatus with privacy status.
        LiveBroadcastStatus status = new LiveBroadcastStatus();
        status.setPrivacyStatus("public");

        LiveBroadcast broadcast = new LiveBroadcast();
        broadcast.setKind("youtube#liveBroadcast");
        broadcast.setSnippet(broadcastSnippet);
        broadcast.setStatus(status);
        broadcast.setContentDetails(contentDetails);


        // Create the insert request
        YouTube.LiveBroadcasts.Insert liveBroadcastInsert = youtube
                .liveBroadcasts().insert("snippet,status,contentDetails",
                        broadcast);

        // Request is executed and inserted broadcast is returned
        LiveBroadcast returnedBroadcast = liveBroadcastInsert.execute();

        // Create a snippet with title.
        LiveStreamSnippet streamSnippet = new LiveStreamSnippet();
        streamSnippet.setTitle(title);

        // Create content distribution network with format and ingestion
        // type.
        CdnSettings cdn = new CdnSettings();
        cdn.setFormat("1080p");
        cdn.setIngestionType("rtmp");

        LiveStream stream = new LiveStream();
        stream.setKind("youtube#liveStream");
        stream.setSnippet(streamSnippet);
        stream.setCdn(cdn);

        // Create the insert request
        YouTube.LiveStreams.Insert liveStreamInsert = youtube.liveStreams()
                .insert("snippet,cdn", stream);

        // Request is executed and inserted stream is returned
        LiveStream returnedStream = liveStreamInsert.execute();

        // Create the bind request
        YouTube.LiveBroadcasts.Bind liveBroadcastBind = youtube
                .liveBroadcasts().bind(returnedBroadcast.getId(),
                        "id,snippet,contentDetails,status");

        // Set stream id to bind
        liveBroadcastBind.setStreamId(returnedStream.getId());

        // Request is executed and bound broadcast is returned
        LiveBroadcast liveBroadcast = liveBroadcastBind.execute();

        EventData eventData = new EventData();
        eventData.setEvent(liveBroadcast);
        IngestionInfo ingestionInfo = returnedStream.getCdn().getIngestionInfo();
        eventData.setIngestionAddress(ingestionInfo.getIngestionAddress() + "/" + ingestionInfo.getStreamName());
        return eventData;
    }

    public static EventData createLiveEvent2(YouTube youtube, String description,
                                             String title, long startTime, long endTime) throws IOException {
        // We need a date that's in the proper ISO format and is in the future,
        // since the API won't
        // create events that start in the past.
        LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
        broadcastSnippet.setTitle(title);
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        broadcastSnippet.setScheduledStartTime(new DateTime(new Date(startTime), TimeZone.getDefault()));
        if (endTime > startTime) {
            broadcastSnippet.setScheduledEndTime(new DateTime(new Date(endTime), TimeZone.getDefault()));
        }
        broadcastSnippet.setDescription(description);

        LiveBroadcastContentDetails contentDetails = new LiveBroadcastContentDetails();
        MonitorStreamInfo monitorStream = new MonitorStreamInfo();
        monitorStream.setEnableMonitorStream(true);
        contentDetails.setMonitorStream(monitorStream);
        contentDetails.set("projection", "360");//这里控制以360 视角直播

        // Create LiveBroadcastStatus with privacy status.
        LiveBroadcastStatus status = new LiveBroadcastStatus();
        status.setPrivacyStatus("public");

        LiveBroadcast broadcast = new LiveBroadcast();
        broadcast.setKind("youtube#liveBroadcast");
        broadcast.setSnippet(broadcastSnippet);
        broadcast.setStatus(status);
        broadcast.setContentDetails(contentDetails);


        // Create the insert request
        YouTube.LiveBroadcasts.Insert liveBroadcastInsert = youtube
                .liveBroadcasts().insert("snippet,status,contentDetails",
                        broadcast);

        // Request is executed and inserted broadcast is returned
        LiveBroadcast returnedBroadcast = liveBroadcastInsert.execute();

        // Create a snippet with title.
        LiveStreamSnippet streamSnippet = new LiveStreamSnippet();
        streamSnippet.setTitle(title);

        // Create content distribution network with format and ingestion
        // type.
        CdnSettings cdn = new CdnSettings();
        cdn.setFormat("1080p");
        cdn.setIngestionType("rtmp");

        LiveStream stream = new LiveStream();
        stream.setKind("youtube#liveStream");
        stream.setSnippet(streamSnippet);
        stream.setCdn(cdn);

        // Create the insert request
        YouTube.LiveStreams.Insert liveStreamInsert = youtube.liveStreams()
                .insert("snippet,cdn", stream);

        // Request is executed and inserted stream is returned
        LiveStream returnedStream = liveStreamInsert.execute();

        // Create the bind request
        YouTube.LiveBroadcasts.Bind liveBroadcastBind = youtube
                .liveBroadcasts().bind(returnedBroadcast.getId(),
                        "id,snippet,contentDetails,status");

        // Set stream id to bind
        liveBroadcastBind.setStreamId(returnedStream.getId());

        // Request is executed and bound broadcast is returned
        LiveBroadcast liveBroadcast = liveBroadcastBind.execute();

        EventData eventData = new EventData();
        eventData.setEvent(liveBroadcast);
        IngestionInfo ingestionInfo = returnedStream.getCdn().getIngestionInfo();
        eventData.setIngestionAddress(ingestionInfo.getIngestionAddress() + "/" + ingestionInfo.getStreamName());
        return eventData;
    }

    public static void createLiveEvent(YouTube youtube, String description,
                                       String name) {
        // We need a date that's in the proper ISO format and is in the future,
        // since the API won't
        // create events that start in the past.
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        long futureDateMillis = System.currentTimeMillis()
                + FUTURE_DATE_OFFSET_MILLIS;
        Date futureDate = new Date();
        futureDate.setTime(futureDateMillis);
        String date = dateFormat.format(futureDate);

        AppLogger.w(String.format(
                "Creating event: name='%s', description='%s', date='%s'.",
                name, description, date));

        try {

            LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
            broadcastSnippet.setTitle(name);
            broadcastSnippet.setScheduledStartTime(new DateTime(futureDate));

            LiveBroadcastContentDetails contentDetails = new LiveBroadcastContentDetails();
            MonitorStreamInfo monitorStream = new MonitorStreamInfo();
            monitorStream.setEnableMonitorStream(false);
            contentDetails.setMonitorStream(monitorStream);

            // Create LiveBroadcastStatus with privacy status.
            LiveBroadcastStatus status = new LiveBroadcastStatus();
            status.setPrivacyStatus("unlisted");

            LiveBroadcast broadcast = new LiveBroadcast();
            broadcast.setKind("youtube#liveBroadcast");
            broadcast.setSnippet(broadcastSnippet);
            broadcast.setStatus(status);
            broadcast.setContentDetails(contentDetails);

            // Create the insert request
            YouTube.LiveBroadcasts.Insert liveBroadcastInsert = youtube
                    .liveBroadcasts().insert("snippet,status,contentDetails",
                            broadcast);

            // Request is executed and inserted broadcast is returned
            LiveBroadcast returnedBroadcast = liveBroadcastInsert.execute();

            // Create a snippet with title.
            LiveStreamSnippet streamSnippet = new LiveStreamSnippet();
            streamSnippet.setTitle(name);

            // Create content distribution network with format and ingestion
            // type.
            CdnSettings cdn = new CdnSettings();
            cdn.setFormat("240p");
            cdn.setIngestionType("rtmp");

            LiveStream stream = new LiveStream();
            stream.setKind("youtube#liveStream");
            stream.setSnippet(streamSnippet);
            stream.setCdn(cdn);

            // Create the insert request
            YouTube.LiveStreams.Insert liveStreamInsert = youtube.liveStreams()
                    .insert("snippet,cdn", stream);

            // Request is executed and inserted stream is returned
            LiveStream returnedStream = liveStreamInsert.execute();

            // Create the bind request
            YouTube.LiveBroadcasts.Bind liveBroadcastBind = youtube
                    .liveBroadcasts().bind(returnedBroadcast.getId(),
                            "id,contentDetails");

            // Set stream id to bind
            liveBroadcastBind.setStreamId(returnedStream.getId());

            // Request is executed and bound broadcast is returned
            liveBroadcastBind.execute();

        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleJsonResponseException code: "
                    + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getStackTrace());
            t.printStackTrace();
        }
    }

    public static List<EventData> getLiveEvents(
            YouTube youtube, String liveBroadcastId) throws IOException {
        AppLogger.w("Requesting live events.with id:" + liveBroadcastId);

        YouTube.LiveBroadcasts.List liveBroadcastRequest = youtube
                .liveBroadcasts().list("id,snippet,contentDetails,status");
        if (!TextUtils.isEmpty(liveBroadcastId)) {
            liveBroadcastRequest.setId(liveBroadcastId);
        }
        // liveBroadcastRequest.setMine(true);
        liveBroadcastRequest.setBroadcastStatus("all");

        // List request is executed and list of broadcasts are returned
        LiveBroadcastListResponse returnedListResponse = liveBroadcastRequest.execute();

        // Get the list of broadcasts associated with the user.
        List<LiveBroadcast> returnedList = returnedListResponse.getItems();

        List<EventData> resultList = new ArrayList<>(returnedList.size());
        EventData event;

        for (LiveBroadcast broadcast : returnedList) {
            event = new EventData();
            event.setEvent(broadcast);
            String streamId = broadcast.getContentDetails().getBoundStreamId();
            if (streamId != null) {
                String ingestionAddress = getIngestionAddress(youtube, streamId);
                event.setIngestionAddress(ingestionAddress);
            }
            resultList.add(event);
        }
        return resultList;
    }

    public static List<EventData> getLiveEvents(
            YouTube youtube) throws IOException {
        AppLogger.w("Requesting live events.");

        YouTube.LiveBroadcasts.List liveBroadcastRequest = youtube
                .liveBroadcasts().list("id,snippet,contentDetails,status");
        // liveBroadcastRequest.setMine(true);
        liveBroadcastRequest.setBroadcastStatus("upcoming");

        // List request is executed and list of broadcasts are returned
        LiveBroadcastListResponse returnedListResponse = liveBroadcastRequest.execute();

        // Get the list of broadcasts associated with the user.
        List<LiveBroadcast> returnedList = returnedListResponse.getItems();

        List<EventData> resultList = new ArrayList<>(returnedList.size());
        EventData event;

        for (LiveBroadcast broadcast : returnedList) {
            event = new EventData();
            event.setEvent(broadcast);
            String streamId = broadcast.getContentDetails().getBoundStreamId();
            if (streamId != null) {
                String ingestionAddress = getIngestionAddress(youtube, streamId);
                event.setIngestionAddress(ingestionAddress);
            }
            resultList.add(event);
        }
        return resultList;
    }

    public static boolean checkBroadcast(YouTube youtube, String broadcastId) {
        try {
            LiveBroadcastListResponse response = youtube.liveBroadcasts().list("id,status").setId(broadcastId).execute();
            if (response.getItems().size() > 0) {
                LiveBroadcast broadcast = response.getItems().get(0);
                return "ready".equals(broadcast.getStatus().getLifeCycleStatus());
            }
        } catch (Exception e) {
            AppLogger.w(MiscUtils.getErr(e));
        }
        return false;
    }

    public interface LiveEvent {
        void execute() throws Exception;

        void cancel();
    }

    public static class YoutubeStopEvent implements LiveEvent {

        private YouTube youtube;
        private String broadcastId;

        public YoutubeStopEvent(YouTube youTube, String broadcastId) {
            this.youtube = youTube;
            this.broadcastId = broadcastId;
        }

        @Override
        public void execute() throws Exception {
            Transition transitionRequest = youtube.liveBroadcasts().transition(
                    "completed", broadcastId, "status");
            transitionRequest.execute();
        }

        @Override
        public void cancel() {

        }
    }

    public static class YoutubeStartEvent implements LiveEvent {

        private YouTube youtube;
        private String broadcastId;
        private String boundStreamId;
        private volatile boolean finished = false;

        public YoutubeStartEvent(YouTube youtube, String broadcastId, String boundStreamId) {
            this.youtube = youtube;
            this.broadcastId = broadcastId;
            this.boundStreamId = boundStreamId;
        }

        public void execute() throws Exception {
            AppLogger.w(" YOUTUBE:broadcastId is:" + broadcastId + ",boundStreamId is" + boundStreamId);
            if (finished) return;
            //获取稳定的码率
            boolean hasActiveStream = false;

            while (!hasActiveStream && !finished) {
                YouTube.LiveStreams.List liveStreamRequest = youtube.liveStreams().list("id,status");
                liveStreamRequest.setId(boundStreamId);
                LiveStreamListResponse streamListResponse = liveStreamRequest.execute();
                List<LiveStream> items = streamListResponse.getItems();
                AppLogger.w("YOUTUBE:获取稳定码率" + JacksonFactory.getDefaultInstance().toPrettyString(items));
                if (items != null && items.size() > 0 && items.get(0).getStatus().getStreamStatus().equals("active")) {
                    hasActiveStream = true;//已经有了稳定的码率了
                    AppLogger.w("YOUTUBE:已经收到了稳定的码率,将切换 BroadCast 到 Testing 状态");
                } else {
                    SystemClock.sleep(2000);
                }
            }
            LiveBroadcastListResponse execute1 = youtube.liveBroadcasts().list("id,status").setId(broadcastId).execute();
            LiveBroadcast broadcast = null;
            if (execute1.getItems().size() > 0) {
                broadcast = execute1.getItems().get(0);
                AppLogger.w(broadcast.toPrettyString());
                String status = broadcast.getStatus().getLifeCycleStatus();
                if ("live".equals(status)) {
                    AppLogger.w("当前已经是直播状态了,直接返回就行了");
                    return;
                } else if ("complete".equals(status)) {
                    throw new IllegalArgumentException("当前直播已结束,不可再直播");
                }
            } else {
                throw new IllegalArgumentException("当前频道不存在");
            }
            boolean hasChangeToTesting = false;
            if ("testing".equals(broadcast.getStatus().getLifeCycleStatus()) || "testStarting".equals(broadcast.getStatus().getLifeCycleStatus())) {
                hasChangeToTesting = true;

            }
            if (!finished && !hasChangeToTesting) {
//                if (broadcast!=null&&broadcast.getStatus().getLifeCycleStatus())
                YouTube.LiveBroadcasts.Transition liveTransitionRequest = youtube.liveBroadcasts().transition("testing", broadcastId, "id,status");
                broadcast = liveTransitionRequest.execute();
                AppLogger.w("YOUTUBE:切换 Transition 到 testing" + JacksonFactory.getDefaultInstance().toPrettyString(broadcast));
                if (!broadcast.getStatus().getLifeCycleStatus().equals("testStarting") && !broadcast.getStatus().getLifeCycleStatus().equals("testing")) {
                    AppLogger.w("YOUTUBE:testStarting 失败了");
                    return;
                }
            }

            while (!hasChangeToTesting && !finished) {
                LiveBroadcastListResponse status = youtube.liveBroadcasts().list("status").setId(broadcastId).execute();
                List<LiveBroadcast> list = status.getItems();
                AppLogger.w("YOUTUBE:获取切换结果:" + JacksonFactory.getDefaultInstance().toPrettyString(list));
                if (list != null && list.size() > 0 && list.get(0).getStatus().getLifeCycleStatus().equals("testing")) {
                    hasChangeToTesting = true;
                } else {
                    SystemClock.sleep(2000);
                }
            }

            if (!finished) {
                Transition transition = youtube.liveBroadcasts().transition("live", broadcastId, "status");
                LiveBroadcast execute = transition.execute();
                AppLogger.w("YOUTUBE:切换 Transition 到 Live" + JacksonFactory.getDefaultInstance().toPrettyString(execute));
                if (execute == null || (!execute.getStatus().getLifeCycleStatus().equals("liveStarting") && !execute.getStatus().getLifeCycleStatus().equals("live"))) {
                    AppLogger.w("YOUTUBE:liveStarting 失败");
                    return;
                }
            }

            boolean hasChangeToActive = false;
            while (!hasChangeToActive && !finished) {
                LiveBroadcastListResponse status = youtube.liveBroadcasts().list("status").setId(broadcastId).execute();
                List<LiveBroadcast> list = status.getItems();
                AppLogger.w("YOUTUBE:获取切换到 Live 后的结果:" + JacksonFactory.getDefaultInstance().toPrettyString(status));
                if (list != null && list.size() > 0 && list.get(0).getStatus().getLifeCycleStatus().contains("live")) {
                    hasChangeToActive = true;
                    AppLogger.w("YOUTUBE:直播状态已经切换到激活状态了,可以查看直播了");
                } else {
                    SystemClock.sleep(2000);
                }
            }

        }

        public void cancel() {
            finished = true;
            youtube = null;
            broadcastId = null;
            boundStreamId = null;
        }
    }


    public static void startEvent(YouTube youtube, String broadcastId, String boundStreamId)
            throws IOException {
        AppLogger.w(" YOUTUBE:broadcastId is:" + broadcastId + ",boundStreamId is" + boundStreamId);
        LiveBroadcastListResponse execute1 = youtube.liveBroadcasts().list("id,status").setId(broadcastId).execute();
        if (execute1.getItems().size() > 0) {
            LiveBroadcast broadcast = execute1.getItems().get(0);
            AppLogger.w(broadcast.toPrettyString());
            String status = broadcast.getStatus().getLifeCycleStatus();
            if ("live".equals(status)) {
                AppLogger.w("当前已经是直播状态了,直接返回就行了");
                return;
            }
        }

        //获取稳定的码率
        boolean hasActiveStream = false;

        while (!hasActiveStream) {
            YouTube.LiveStreams.List liveStreamRequest = youtube.liveStreams().list("id,status");
            liveStreamRequest.setId(boundStreamId);
            LiveStreamListResponse streamListResponse = liveStreamRequest.execute();
            List<LiveStream> items = streamListResponse.getItems();
            AppLogger.w("YOUTUBE:获取稳定码率" + JacksonFactory.getDefaultInstance().toPrettyString(items));
            if (items != null && items.size() > 0 && items.get(0).getStatus().getStreamStatus().equals("active")) {
                hasActiveStream = true;//已经有了稳定的码率了
                AppLogger.w("YOUTUBE:已经收到了稳定的码率,将切换 BroadCast 到 Testing 状态");
            } else {
                SystemClock.sleep(3000);
            }
        }

        YouTube.LiveBroadcasts.Transition liveTransitionRequest = youtube.liveBroadcasts().transition("testing", broadcastId, "id,status");
        LiveBroadcast broadcast = liveTransitionRequest.execute();
        AppLogger.w("YOUTUBE:切换 Transition 到 testing" + JacksonFactory.getDefaultInstance().toPrettyString(broadcast));
        if (!broadcast.getStatus().getLifeCycleStatus().equals("testStarting") && !broadcast.getStatus().getLifeCycleStatus().equals("testing")) {
            AppLogger.w("YOUTUBE:testStarting 失败了");
            return;
        }

        boolean hasChangeToTesting = false;
        while (!hasChangeToTesting) {
            LiveBroadcastListResponse status = youtube.liveBroadcasts().list("status").setId(broadcastId).execute();
            List<LiveBroadcast> list = status.getItems();
            AppLogger.w("YOUTUBE:获取切换结果:" + JacksonFactory.getDefaultInstance().toPrettyString(list));
            if (list != null && list.size() > 0 && list.get(0).getStatus().getLifeCycleStatus().equals("testing")) {
                hasChangeToTesting = true;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    AppLogger.e(MiscUtils.getErr(e));
                }
            }
        }

        Transition transition = youtube.liveBroadcasts().transition("live", broadcastId, "status");
        LiveBroadcast execute = transition.execute();
        AppLogger.w("YOUTUBE:切换 Transition 到 Live" + JacksonFactory.getDefaultInstance().toPrettyString(execute));
        if (execute == null || (!execute.getStatus().getLifeCycleStatus().equals("liveStarting") && !execute.getStatus().getLifeCycleStatus().equals("live"))) {
            AppLogger.w("YOUTUBE:liveStarting 失败");
            return;
        }
        boolean hasChangeToActive = false;

        while (!hasChangeToActive) {
            LiveBroadcastListResponse status = youtube.liveBroadcasts().list("status").setId(broadcastId).execute();
            List<LiveBroadcast> list = status.getItems();
            AppLogger.w("YOUTUBE:获取切换到 Live 后的结果:" + JacksonFactory.getDefaultInstance().toPrettyString(execute));
            if (list != null && list.size() > 0 && list.get(0).getStatus().getLifeCycleStatus().contains("live")) {
                hasChangeToActive = true;
                AppLogger.w("YOUTUBE:直播状态已经切换到激活状态了,可以查看直播了");
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    AppLogger.e(MiscUtils.getErr(e));
                }
            }
        }
    }

    public static void endEvent(YouTube youtube, String broadcastId)
            throws IOException {
        Transition transitionRequest = youtube.liveBroadcasts().transition(
                "completed", broadcastId, "status");
        transitionRequest.execute();
    }

    public static String getIngestionAddress(YouTube youtube, String streamId)
            throws IOException {
        YouTube.LiveStreams.List liveStreamRequest = youtube.liveStreams()
                .list("cdn");
        liveStreamRequest.setId(streamId);
        LiveStreamListResponse returnedStream = liveStreamRequest.execute();

        List<LiveStream> streamList = returnedStream.getItems();
        if (streamList.isEmpty()) {
            return "";
        }
        IngestionInfo ingestionInfo = streamList.get(0).getCdn().getIngestionInfo();
        return ingestionInfo.getIngestionAddress() + "/" + ingestionInfo.getStreamName();
    }

    public static YouTube getYoutube(Context context) {
        if (youTube == null) {
            synchronized (YouTubeApi.class) {
                if (youTube == null) {
                    String account = PreferencesUtils.getString(JConstant.YOUTUBE_PREF_ACCOUNT_NAME);
                    if (!TextUtils.isEmpty(account)) {
                        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, YouTubeScopes.all());
                        credential.setSelectedAccountName(account);
                        youTube = new YouTube.Builder(AndroidHttp.newCompatibleTransport(),
                                JacksonFactory.getDefaultInstance(),
                                credential
                        )
                                .setApplicationName(context.getString(R.string.app_name))
                                .build();
                    }
                }
            }
        }
        return youTube;
    }

    public static class YoutubeLiveStartFlow {

        public void setEventListener() {

        }


    }
}
