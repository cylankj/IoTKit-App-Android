//package com.cylan.jiafeigou.base.module;
//
//import com.bumptech.glide.Priority;
//import com.bumptech.glide.load.data.DataFetcher;
//import com.bumptech.glide.load.model.stream.StreamModelLoader;
//import com.cylan.jiafeigou.support.log.AppLogger;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.HashMap;
//import java.util.Map;
//
//import okhttp3.Call;
//import okhttp3.HttpUrl;
//import okhttp3.Interceptor;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import okhttp3.ResponseBody;
//import okio.Buffer;
//import okio.BufferedSource;
//import okio.ForwardingSource;
//import okio.Okio;
//import okio.Source;
//
///**
// * Created by yanzhendong on 2017/5/13.
// */
//
//public class GlideDownloadPercentLoader implements StreamModelLoader<DownloadPercent> {
//    private OkHttpClient okHttpClient;
//    private static GlideDownloadPercentLoader instance;
//    private Map<String, DownloadPercent> percentMap = new HashMap<>();
//
//    @Override
//    public DataFetcher<InputStream> getResourceFetcher(DownloadPercent model, int width, int height) {
//        return new GlideDownloadPercentDataFetcher(model);
//    }
//
//    public static GlideDownloadPercentLoader getInstance() {
//        if (instance == null) {
//            synchronized (GlideDownloadPercentLoader.class) {
//                if (instance == null) {
//                    instance = new GlideDownloadPercentLoader();
//                }
//            }
//        }
//        return instance;
//    }
//
//    private GlideDownloadPercentLoader() {
//        okHttpClient = new OkHttpClient.Builder().addInterceptor(interceptor).build();
//    }
//
//    private Interceptor interceptor = chain -> {
//        Request request = chain.request();
//        Response response = chain.proceed(request);
//        return response.newBuilder().body(new DownloadPercentResponseBody(response.body(), percentMap.get(request.url().encodedPath()))).build();
//    };
//
//    private class DownloadPercentResponseBody extends ResponseBody {
//        private final ResponseBody responseBody;
//        private DownloadPercent percent;
//        private BufferedSource bufferedSource;
//        private int currentPercent = 0;
//        private long contentLength = 0;
//
//        public DownloadPercentResponseBody(ResponseBody responseBody, DownloadPercent percent) {
//            this.responseBody = responseBody;
//            this.percent = percent;
//            this.contentLength = responseBody.contentLength();
//        }
//
//        @Override
//        public MediaType contentType() {
//            return responseBody.contentType();
//        }
//
//        @Override
//        public long contentLength() {
//            return responseBody.contentLength();
//        }
//
//        @Override
//        public BufferedSource source() {
//            if (bufferedSource == null) {
//                try {
//                    bufferedSource = Okio.buffer(source(responseBody.source()));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            return bufferedSource;
//        }
//
//        private Source source(Source source) {
//            return new ForwardingSource(source) {
//                long totalBytesRead = 0L;
//
//                @Override
//                public long read(Buffer sink, long byteCount) throws IOException {
//                    long bytesRead = super.read(sink, byteCount);
//                    // read() returns the number of bytes read, or -1 if this source is exhausted.
//                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
//                    int current = (int) (((float) totalBytesRead / contentLength) * 100);
//                    AppLogger.d("当前百分比" + current);
////                    if (percent != null && percent.listener != null && current - currentPercent >= 1) {
////                        percent.listener.update(totalBytesRead, contentLength, current, bytesRead == -1);
////                    }
//                    currentPercent = current;
//                    return bytesRead;
//                }
//            };
//        }
//    }
//
//    private class GlideDownloadPercentDataFetcher implements DataFetcher<InputStream> {
//        private HttpUrl httpUrl;
//        private DownloadPercent downloadPercent;
//        private Call newCall;
//
//        public GlideDownloadPercentDataFetcher(DownloadPercent downloadPercent) {
//            DeviceInformation deviceInformation = BaseDeviceInformationFetcher.getInstance().getDeviceInformation();
//            if (deviceInformation != null) {
//                this.downloadPercent = downloadPercent;
////                this.httpUrl = HttpUrl.parse("http://" + deviceInformation.ip + "/images/" + downloadPercent.url);
//                percentMap.put(httpUrl.encodedPath(), downloadPercent);
//            }
//        }
//
//        @Override
//        public InputStream loadData(Priority priority) throws Exception {
//            if (this.httpUrl == null) {
//                throw new IllegalAccessException("当前网络环境下无法加载");
//            }
//            newCall = okHttpClient.newCall(new Request.Builder().url(httpUrl).build());
//            Response response = newCall.execute();
//            return response.body().byteStream();
//        }
//
//        @Override
//        public void cleanup() {
//            AppLogger.e("cleanup");
//        }
//
//        @Override
//        public String getId() {
//            return "";
//        }
//
//        @Override
//        public void cancel() {
//            AppLogger.e("cancel");
//            if (newCall != null && !newCall.isExecuted()) {
//                newCall.cancel();
//                newCall = null;
//            }
//        }
//    }
//
//}
