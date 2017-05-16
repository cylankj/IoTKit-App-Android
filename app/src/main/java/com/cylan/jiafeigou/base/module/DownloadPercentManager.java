package com.cylan.jiafeigou.base.module;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Sink;
import okio.Source;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/5/15.
 */

public class DownloadPercentManager {
    private static DownloadPercentManager instance;
    private Map<String, DownloadPercent> downloadPercentMap;
    private Map<String, DownloadPercent.DownloadListener> downloadListenerMap = new HashMap<>();
    private int maxCount = 10;
    private IHttpApi httpApi;

    public static DownloadPercentManager getInstance() {
        if (instance == null) {
            synchronized (DownloadPercentManager.class) {
                if (instance == null) {
                    instance = new DownloadPercentManager();
                }
            }
        }
        return instance;
    }

    public DownloadPercentManager() {
    }

    public DownloadPercent fetch(String uuid, String fileName) {
        String identifier = DownloadPercent.getIdentifier(uuid, fileName);
        DownloadPercent downloadPercent = downloadPercentMap.get(identifier);
        if (downloadPercent == null) {
            downloadPercent = new DownloadPercent(fileName, uuid);
            downloadPercentMap.put(identifier, downloadPercent);
        }
        return downloadPercent;
    }


    public void init(IHttpApi httpApi) {
        try {
            this.httpApi = httpApi;
            FileReader reader = new FileReader(new File(JConstant.PAN_PATH, "DownloadPercent.json"));
            Type type = new TypeToken<Map<String, DownloadPercent>>() {
            }.getType();
            downloadPercentMap = new Gson().fromJson(reader, type);
        } catch (FileNotFoundException e) {
            AppLogger.e(e.getMessage());
        }
        if (downloadPercentMap == null) {
            downloadPercentMap = new HashMap<>();
        }
    }

    public void download(String uuid, String fileName, DownloadPercent.DownloadListener listener) {
        DownloadPercent downloadPercent = fetch(uuid, fileName);
        downloadListenerMap.put(downloadPercent.getIdentifier(), listener);
        httpApi.download(fileName, downloadPercent.progress + " -")
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(responseBody -> {
                    DownloadPercentResponseBody body = new DownloadPercentResponseBody(responseBody, downloadPercent);
                    try {
                        File file = new File(JConstant.PANORAMA_MEDIA_PATH + File.separator + downloadPercent.getIdentifier());
                        file.getParentFile().mkdirs();
                        Sink sink = Okio.sink(file);
                        BufferedSink buffer = Okio.buffer(sink);
                        buffer.writeAll(body.source());
                        buffer.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }, e -> {
                    AppLogger.e(e.getMessage());
                });
    }

    private class DownloadPercentResponseBody extends ResponseBody {
        private final ResponseBody responseBody;
        private DownloadPercent percent;
        private BufferedSource bufferedSource;
        private final Scheduler.Worker worker;
        private DownloadPercent.DownloadListener listener;

        public DownloadPercentResponseBody(ResponseBody responseBody, DownloadPercent percent) {
            this.responseBody = responseBody;
            this.percent = percent;
            this.percent.total = responseBody.contentLength();
            worker = AndroidSchedulers.mainThread().createWorker();
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                try {
                    bufferedSource = Okio.buffer(source(responseBody.source()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {


                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    percent.progress += bytesRead != -1 ? bytesRead : 0;
                    percent.percent = (int) (((float) percent.progress / percent.total) * 100);
                    AppLogger.d("当前百分比" + percent.percent);
                    listener = downloadListenerMap.get(percent.getIdentifier());
                    if (listener != null) {
                        worker.schedule(() -> listener.update(percent.progress, percent.total, percent.percent, bytesRead == -1));
                    }
                    return bytesRead;
                }
            };
        }
    }

}
