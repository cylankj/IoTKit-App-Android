package com.cylan.jiafeigou.n.view.panorama;

/**
 * Created by yanzhendong on 2017/3/13.
 */

//public class PanoramaAlbumPresenter extends BasePresenter<PanoramaAlbumContact.View> implements
//        PanoramaAlbumContact.Presenter {
//
//    private boolean hasConnected;
//
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        fetch(false);//下拉刷新
//        Observable.just("make")
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(Schedulers.newThread())
//                .delay(2, TimeUnit.SECONDS)
//                .subscribe(account -> {
//                    makeTCPBridge();
//                }, AppLogger::e);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//    }
//
//    @Override
//    public void OnConnected() {
//        AppLogger.d("socked connected");
//        startGetFirstItem();
//        Observable.just("connected")
//                .subscribeOn(AndroidSchedulers.mainThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .filter(account -> mView != null)
//                .subscribe(account -> mView.onConnected(), AppLogger::e);
//    }
//
//    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
//
//    public void startSyncAlbumList(int time, int limit) {
////        PanoramaEvent.MsgFileListReq req = new PanoramaEvent.MsgFileListReq();
////        req.beginTime = time;
////        req.endTime = req.beginTime + 24 * 3600;
////        req.limit = limit;
////        byte[] data = fill(new PanoramaEvent.RawReqMsg(), MIDRobotForwardDataV2, TYPE_FILE_LIST_REQ, DpUtils.pack(req));
////        boolean send = JFGSocket.SendMsgpackBuff(socketPointer, data);
////        AppLogger.d("send ret:" + send + " time:" + dateFormat.format(new Date(time * 1000L)));
//    }
//
//    /**
//     * 得到文件
//     */
//    private void startGetFirstItem() {
////        Observable.just("go")
////                .subscribeOn(Schedulers.newThread())
////                .subscribe(account -> {
////                    PanoramaEvent.MsgFirstFileInListReq req = new PanoramaEvent.MsgFirstFileInListReq();
////                    byte[] data = fill(new PanoramaEvent.RawReqMsg(), MIDRobotForwardDataV2,
////                            TYPE_FIRST_FILE_REQ, DpUtils.pack(req));
////                    boolean send = JFGSocket.SendMsgpackBuff(socketPointer, data);
////                    AppLogger.d("send ret:" + send + "请求第一条文件时间");
////                }, AppLogger::e);
//    }
//
////    protected byte[] fill(PanoramaEvent.RawReqMsg rawReqMsg, int msgId, int type, byte[] msg) {
//////        rawReqMsg.dst = Collections.singletonList(mUUID);
//////        rawReqMsg.mCaller = "";
//////        rawReqMsg.mCallee = "";
//////        rawReqMsg.mId = msgId;
//////        rawReqMsg.isAck = 1;
//////        rawReqMsg.type = type;
//////        rawReqMsg.msg = msg;
//////        return DpUtils.pack(rawReqMsg);
////        return null
////    }
//
//    @Override
//    public void OnDisconnected() {
//        hasConnected = false;
//        HandlerThreadUtils.postAtFrontOfQueue(() -> {
//            if (socketPointer != -1) {
//                JFGSocket.Release(socketPointer);
//                socketPointer = -1;
//                AppLogger.d("onDestroy socket good");
//            }
//        });
//        AppLogger.d("OnDisconnected ");
//        Observable.just("disconnect")
//                .subscribeOn(AndroidSchedulers.mainThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .filter(account -> mView != null)
//                .subscribe(account -> mView.onDisconnected(), AppLogger::e);
//    }
//
//    private static final String KEY_FIRST_FILE_TIME = "firstFileTime";
//
//    private void handleFirstFileTime(int time) {
//        if (time <= 0) {
//            AppLogger.d("设备没有文件:" + time);
////            notifyNoFile();
//            return;
//        }
//        int firstTime = PreferencesUtils.getInt(KEY_FIRST_FILE_TIME + mUUID, 0);
//        AppLogger.d("firstTime in cache: " + firstTime + " " + time);
//
//        if (time > firstTime) {
//            if (firstTime != 0)
//                AppLogger.d("设备刷新了，或者更换sd卡了。:" + dateFormat.format(new Date(firstTime * 1000L)));
//            PreferencesUtils.putInt(KEY_FIRST_FILE_TIME + mUUID, time);
//        } else if (time == firstTime) {
//            AppLogger.d("文件列表开始时间不变");
//        } else {
//            AppLogger.d("设备刷新了，或者更换sd卡了。:" + dateFormat.format(new Date(firstTime * 1000L)));
//        }
//        Observable.just("o")
//                .subscribeOn(Schedulers.newThread())
//                .subscribe(account -> startSyncAlbumList(time, 20), AppLogger::e);
//    }
//
//
//    private static final class DownFileRsp {
//        PanoramaEvent.RawRspMsg data;
//
//        public DownFileRsp(PanoramaEvent.RawRspMsg data) {
//            this.data = data;
//        }
//    }
//
//    public static final class FileListRspEvent {
//        public PanoramaEvent.MsgFile[] array;
//
//        public FileListRspEvent(PanoramaEvent.MsgFile[] array) {
//            this.array = array;
//        }
//    }
//
////    private Observable<PanoramaEvent.MsgFileDownloadRsp> writeFile(PanoramaEvent.MsgFileDownloadRsp rsp) {
////        return Observable.just(rsp)
////                .subscribeOn(Schedulers.io())
////                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
////                .flatMap(new Func1<PanoramaEvent.MsgFileDownloadRsp, Observable<PanoramaEvent.MsgFileDownloadRsp>>() {
////                    @Override
////                    public Observable<PanoramaEvent.MsgFileDownloadRsp> call(PanoramaEvent.MsgFileDownloadRsp rsp) {
////                        try {
////                            String filePath = JConstant.PAN_PATH + File.separator + mUUID + File.separator + rsp.fileName;
////                            File file = new File(filePath);
////                            if (!file.getParentFile().exists()) {
////                                file.getParentFile().mkdir();
////                            }
////                            if (!file.exists()) file.createNewFile();
////                            RandomAccessFile aFile = new RandomAccessFile(filePath, "rw");
////                            aFile.seek(rsp.begin);
////                            aFile.write(rsp.buffer);
////                            aFile.close();
////                            Log.d(this.getClass().getSimpleName(), "write: good");
////                        } catch (IOException e) {
////                            Log.e(this.getClass().getSimpleName(), "err: " + e.getLocalizedMessage());
////                            return null;
////                        }
////                        return Observable.just(rsp);
////                    }
////                });
////    }
//
//    /**
//     * 开始下载
//     */
//    private void goonDownload() {
//        PanAlbumDataManager.getInstance().getNextPreparedDownloadFile(mUUID)
//                .subscribeOn(Schedulers.newThread())
//                .flatMap(file -> {
//                    if (file == null) {
//                        AppLogger.e("err: no file need to be downloaded");
//                        return Observable.just(new Pair<Long, byte[]>(-1L, null));
//                    }
//                    AppLogger.d(" need download:" + file);
//                    PanoramaEvent.MsgFileDownloadReq req = new PanoramaEvent.MsgFileDownloadReq();
//                    req.begin = file.offset;
//                    req.offset = file.offset + 64;
//                    req.fileName = file.fileName;
//                    req.md5 = file.md5;
//                    PanoramaEvent.RawReqMsg raw = new PanoramaEvent.RawReqMsg();
//                    raw.mSeq = System.currentTimeMillis();
//                    byte[] data = fill(raw,
//                            MIDRobotForwardDataV2,
//                            TYPE_FILE_DOWNLOAD_REQ,
//                            DpUtils.pack(req));
//                    boolean ret = JFGSocket.SendMsgpackBuff(socketPointer, data);
//                    AppLogger.d("开始下载:" + ret + " " + req + " " + raw.mSeq);
//                    return Observable.just(new Pair<>(raw.mSeq, req.md5));
//                })
//                .filter(longPair -> longPair != null && longPair.first > 0)
//                .flatMap(longPair -> {
//                    final Subscription subscription = RxBus.getCacheInstance().toObservable(DownFileRsp.class)
//                            .filter(downFileRsp -> downFileRsp.data != null && downFileRsp.data.mSeq == longPair.first)
//                            .flatMap(downFileRsp -> {
//                                try {
//                                    PanoramaEvent.MsgFileDownloadRsp rsp = DpUtils.unpackData(downFileRsp.data.msg, PanoramaEvent.MsgFileDownloadRsp.class);
//                                    if (rsp == null || rsp.ret == -1) {
//                                        AppLogger.e("file download err: " + rsp);
//                                        return Observable.just(1);
//                                    }
//                                    int fileSize = PanAlbumDataManager.getInstance().getFileSize(rsp.fileName);
//                                    if (rsp.begin + rsp.offset >= fileSize) {
//                                        AppLogger.e("file is finish: " + fileSize + " " + rsp);
//                                    }
//                                    //写文件吧。
//                                    return writeFile(rsp).filter(rsp1 -> rsp1 != null)
//                                            .subscribeOn(Schedulers.io())
//                                            .flatMap(new Func1<PanoramaEvent.MsgFileDownloadRsp, Observable<Integer>>() {
//                                                @Override
//                                                public Observable<Integer> call(PanoramaEvent.MsgFileDownloadRsp rsp) {
//                                                    int updateRet = PanAlbumDataManager.getInstance().updateFile(mUUID, rsp.fileName, rsp.begin + rsp.offset);
//                                                    if (updateRet == 0) {
//                                                        AppLogger.d("下载完成？:" + rsp + " " + (System.currentTimeMillis() - longPair.first) + "ms," + MiscUtils.getFlowResult((rsp.begin + rsp.offset) / 1024));
//                                                        notifyItemFinish(rsp);
//                                                        return Observable.just(1);
//                                                    } else if (updateRet == 1) {
//                                                        PanoramaEvent.MsgFileDownloadReq req = new PanoramaEvent.MsgFileDownloadReq();
//                                                        req.begin = rsp.begin + rsp.offset;
//                                                        req.offset = req.begin + 30 * 1024;
//                                                        req.fileName = rsp.fileName;
//                                                        req.md5 = longPair.second;
//                                                        PanoramaEvent.RawReqMsg raw = new PanoramaEvent.RawReqMsg();
//                                                        raw.mSeq = longPair.first;
//                                                        byte[] data = fill(raw,
//                                                                MIDRobotForwardDataV2,
//                                                                TYPE_FILE_DOWNLOAD_REQ,
//                                                                DpUtils.pack(req));
//                                                        boolean ret = JFGSocket.SendMsgpackBuff(socketPointer, data);
//                                                        AppLogger.d("继续下载:" + ret + " " + req + " " + raw.mSeq);
//                                                        return Observable.just(2);
//                                                    } else {
//                                                        AppLogger.d("文件下载状态：" + updateRet);
//                                                        return Observable.just(-1);
//                                                    }
//                                                }
//                                            });
//                                } catch (IOException e) {
//                                    AppLogger.e("err: 下载出错" + e.getLocalizedMessage());
//                                    return Observable.just(-1);
//                                }
//                            }).subscribe(ret -> {
//                                AppLogger.d("downloadRet:" + ret);
//                                if (ret == 1) goonDownload();
//                            }, throwable -> AppLogger.d("download err: " + throwable.getLocalizedMessage()));
//                    registerSubscription(subscription);
//                    return Observable.just(longPair);
//                })
//                .subscribe(o -> AppLogger.d(o == null ? "没有文件下载" : "文件下载中...."),
//                        throwable -> AppLogger.e("download err: " + throwable.getLocalizedMessage()),
//                        () -> AppLogger.d("流程走通...."));
//
//    }
//
//    private void notifyItemFinish(PanoramaEvent.MsgFileDownloadRsp rsp) {
//        Observable.just(rsp)
//                .subscribeOn(Schedulers.newThread())
//                .flatMap(new Func1<PanoramaEvent.MsgFileDownloadRsp, Observable<Integer>>() {
//                    @Override
//                    public Observable<Integer> call(PanoramaEvent.MsgFileDownloadRsp rsp) {
//                        ArrayList<PAlbumBean> allList = mView.getList();
//                        if (allList == null) return null;
//                        PAlbumBean bean = new PAlbumBean();
//                        DownloadFile file = new DownloadFile();
//                        file.fileName = rsp.fileName;
//                        file.time = MiscUtils.getValueFrom(file.fileName);
//                        bean.setDownloadFile(file);
//                        int index = allList.indexOf(bean);
//                        AppLogger.d("file index finish: to update ui: " + index);
//                        if (index < 0) return null;
//                        return Observable.just(index);
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .filter(account -> mView != null)
//                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
//                .subscribe(integer -> mView.onUpdate(null, integer), AppLogger::e);
//    }
//
//    private Subscription fileListRspSubscription() {
//        return RxBus.getCacheInstance().toObservable(FileListRspEvent.class)
//                .subscribeOn(Schedulers.io())
//                .map(fileListRspEvent -> {
//                    if (fileListRspEvent.array == null || fileListRspEvent.array.length == 0)
//                        return null;
//                    for (PanoramaEvent.MsgFile file : fileListRspEvent.array) {
//                        if (TextUtils.isEmpty(file.fileName)) {
//                            AppLogger.e("fileName is empty: " + file);
//                            continue;
//                        }
//                        PanAlbumDataManager.getInstance().insertFile(mUUID, file.fileName, file.md5, file.fileSize)
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(Schedulers.io())
//                                .subscribe(ret -> {
//                                }, AppLogger::e);
//                        AppLogger.d("insert file?: " + file);
//                    }
//                    goonDownload();
//                    judgeLoadMore();
//                    return fileListRspEvent.array;
//                })
//                .subscribe(list -> {
//                }, throwable -> {
//                    AppLogger.e("err: " + throwable.getLocalizedMessage());
//                    registerSubscription(fileListRspSubscription());
//                });
//    }
//
//    private void judgeLoadMore() {
//        if (mView != null && ListUtils.getSize(mView.getList()) > 0)
//            fetch(true);//刷列表,loadMore形式
//    }
//
//    private ArrayList<PAlbumBean> convert(List<DownloadFile> list, boolean assembleDate) {
//        if (ListUtils.getSize(list) == 0) return new ArrayList<>();
//        ArrayList<PAlbumBean> arrayList = new ArrayList<>(list.size());
//        Collections.sort(list);
//        ArrayList<String> dateList = new ArrayList<>();
//        for (DownloadFile file : list) {
//            String date = TimeUtils.getDayString(file.getTime() * 1000L);
//            Log.d(this.getClass().getSimpleName(), "convert: " + date);
//            PAlbumBean bean = new PAlbumBean();
//            if (!dateList.contains(date) && assembleDate) {
//                bean.setDownloadFile(file);
//                dateList.add(date);
//                bean.isDate = true;
//                arrayList.add(bean);
//            }
//            bean = new PAlbumBean();
//            bean.setDownloadFile(file);
//            bean.isDate = false;
//            arrayList.add(bean);
//        }
//        Log.d(this.getClass().getSimpleName(), "List: " + arrayList);
//        return arrayList;
//    }
//
//    @Override
//    protected void onRegisterSubscription() {
//        super.onRegisterSubscription();
//        registerSubscription(getNetWorkChangedSub());
//        registerSubscription(fileListRspSubscription());
//    }
//
//    private Observable<Boolean> checkConnection() {
//        return Observable.just("checkConnection")
//                .observeOn(Schedulers.io())
//                .subscribeOn(Schedulers.io())
//                .map(account -> {
//                    final WifiInfo info = NetUtils.getWifiManager(ContextUtils.getContext()).getConnectionInfo();
//                    if (info == null || !JFGRules.isCylanDevice(info.getSSID())) {
//                        AppLogger.i("checkConnection: " + info);
//                        return false;
//                    }
//                    return true;
//                });
//    }
//
//    private void makeTCPBridge() {
//        checkConnection()
//                .filter(aBoolean -> aBoolean)
//                .map(account -> {
//                    try {
//                        AppLogger.d("正在发送 FPing 消息");
//                        appCmd.sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
//                        appCmd.sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
//                    } catch (JfgException e) {
//                        e.printStackTrace();
//                        AppLogger.d("连接 socket 出现错误");
//                    }
//                    return null;
//                })
//                .timeout(3, TimeUnit.SECONDS)
//                .zipWith(RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(Schedulers.io())
//                        .map(new Func1<RxEvent.LocalUdpMsg, Boolean>() {
//                            @Override
//                            public Boolean call(RxEvent.LocalUdpMsg msg) {
//                                try {
//                                    JfgUdpMsg.UdpHeader header = DpUtils.unpackData(msg.data, JfgUdpMsg.UdpHeader.class);
//                                    AppLogger.d("header: " + new Gson().toJson(header));
//                                    if (header == null || !TextUtils.equals(header.cmd, "f_ping_ack"))
//                                        return false;
//                                    JfgUdpMsg.FPingAck pingAck = DpUtils.unpackData(msg.data, JfgUdpMsg.FPingAck.class);
//                                    AppLogger.d("pingAck: " + new Gson().toJson(pingAck));
//                                    if (pingAck == null || !TextUtils.equals(pingAck.cid, mUUID))
//                                        return false;
//                                    //得到 fping响应
//                                    synchronized (this) {
//                                        if (!hasConnected) {
//                                            hasConnected = true;
//                                            AppLogger.d("获取到设备 IP 地址:" + msg.ip + ",port:" + msg.port);
//                                            JFGSocket.Connect(socketPointer, msg.ip, msg.port, true);
//                                            return true;
//                                        }
//                                    }
//                                } catch (Exception e) {
//                                    AppLogger.e("err: " + e.getLocalizedMessage());
//                                    return false;
//                                }
//                                return false;
//                            }
//                        }), (Object o, Boolean aBoolean) -> aBoolean)
//                .filter(aBoolean -> aBoolean)
//                .doOnError(throwable -> {
//                    if (throwable instanceof TimeoutException) {
//                        AppLogger.e("f_ping timeout :");
//                    }
//                })
//                .subscribe(o -> AppLogger.d("good"),
//                        throwable -> AppLogger.e("f_ping err :" + throwable.getLocalizedMessage()),
//                        () -> AppLogger.d("make tcp finished"));
//    }
//
//    private Subscription getNetWorkChangedSub() {
//        return RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(event -> {
//                    if (event.mobile != null && event.mobile.isConnected()) {
//                        //移动网络,提醒用户注意流量
//                        AppLogger.d("mobile network connected");
//                    } else if (event.wifi != null && event.wifi.isConnected()) {
//                        //wifi 网络,关闭流量提醒
//                        AppLogger.d("wifi network connected");
//                    }
//                }, AppLogger::e);
//
//    }
//
//    /**
//     * @param lt     less than
//     * @param goNext
//     */
//    private void fetch(boolean lt, boolean goNext) {
//        ArrayList<PAlbumBean> list = mView.getList();
//        int startTime = 0;
//        if (list == null || list.size() == 0) {
//        } else {
//            startTime = (int) list.get(0).getDownloadFile().getTime();
//        }
//        PanFileDownloader.getDownloader().getFileFrom(mUUID, startTime, lt, 20)
//                .subscribeOn(Schedulers.io())
//                .flatMap(downloadFiles -> {
//                    ArrayList<PAlbumBean> uiList = mView.getList();
//                    if (ListUtils.getSize(uiList) == 0) {
//                        AppLogger.d("back 0");
//                        return Observable.just(convert(downloadFiles, true));
//                    }
//                    if (ListUtils.getSize(downloadFiles) == 0) {
//                        AppLogger.d("back 1");
//                        return null;
//                    }
//                    int sizeInUi = ListUtils.getSize(uiList);
//                    ArrayList<PAlbumBean> convertList = convert(downloadFiles, false);
//                    convertList.removeAll(uiList);
//                    if (ListUtils.getSize(convertList) == 0) {
//                        AppLogger.d("back 2");
//                        return null;
//                    }
//                    AppLogger.d("size: " + ListUtils.getSize(convertList) + " account:" + sizeInUi);
//                    long timeLastItem = uiList.get(uiList.size() - 1).getDownloadFile().getTime() * 1000L;
//                    long timeFirst = convertList.get(0).getDownloadFile().getTime() * 1000L;
//                    AppLogger.d("timeLastItem: " + timeLastItem + " timeFirst:" + timeFirst);
//                    if (!TimeUtils.isSameDay(timeFirst, timeLastItem)) {
//                        //需要组装
//                        PAlbumBean bean = new PAlbumBean();
//                        bean.isDate = true;
//                        convertList.add(0, bean);
//                        AppLogger.d("assemble date timeLastItem: " + timeLastItem + " timeFirst:" + timeFirst);
//                    }
//                    return Observable.just(convertList);
//                })
//                .filter(finalList -> ListUtils.getSize(finalList) > 0)
//                .observeOn(AndroidSchedulers.mainThread())
//                .filter(account -> mView != null)
//                .subscribe(beanArrayList -> {
//                            mView.onAppend(beanArrayList);
//                            Log.d(this.getClass().getSimpleName(), "beanArrayList: " + ListUtils.getSize(beanArrayList));
//                        },
//                        throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()),
//                        () -> AppLogger.d("good load finish"));
//    }
//
//    @Override
//    public void fetch(boolean loadMore) {
//        fetch(loadMore, false);
//    }
//
//    @Override
//    public void downloadFile(String fileName) {
//        PanAlbumDataManager.getInstance().getDownloadFile(mUUID, fileName)
//                .flatMap(downloadFile -> {
//                    if (downloadFile == null) {
//                        AppLogger.e("文件损坏");
//                        Observable.just("fileNotFound")
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .filter(account -> mView != null)
//                                .subscribe(account -> mView.onFileState(-1), AppLogger::e);
//                    }
//                    return null;
//                });
//    }
//}
