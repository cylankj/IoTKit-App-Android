package com.cylan.jiafeigou.cache.video;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.wheel.ex.DataExt;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.utils.TimeUtils.getSpecificDayEndTime;

/**
 * 历史录像数据管理中心
 *
 * @author cylan-hunt
 * @date 16-12-6
 */

public class History {
    private static final String TAG = "History";
    private final Object lock = new Object();
    private static final Gson GSON = new Gson();

    private static final ThreadLocal<SimpleDateFormat> SAFE_FORMAT__ = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
            TimeZone zone = TimeZone.getTimeZone("Europe/London");
            format.setTimeZone(zone);
            return format;
        }
    };

    private static final ThreadLocal<SimpleDateFormat> SAFE_FORMAT_ = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.UK);
            TimeZone zone = TimeZone.getTimeZone("Europe/London");
            format.setTimeZone(zone);
            return format;
        }
    };
    private static final ThreadLocal<SimpleDateFormat> SAFE_FORMAT_LIVE_TIME = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm", Locale.UK);
            TimeZone zone = TimeZone.getTimeZone("Europe/London");
            format.setTimeZone(zone);
            return format;
        }
    };

    private volatile static History history;
    private HashMap<String, ArrayList<Long>> dateListMap = new HashMap<>();
    private ConcurrentHashMap<String, ArrayList<HistoryFile>> historyMap;

    private HashMap<String, Boolean> rspIndexMap = new HashMap<>();

    /**
     * 数据集,不实现Lru逻辑
     */

    public static History getHistory() {
        if (history == null) {
            history = new History();
        }
        return history;
    }

    private void ensureMap() {
        if (historyMap == null) {
            historyMap = new ConcurrentHashMap<>();
        }
        if (dateListMap == null) {
            dateListMap = new HashMap<>();
        }
    }

    public synchronized static String parseTime2Date(long time) {
        return SAFE_FORMAT__.get().format(new Date(time));
    }

    public synchronized static String parseLiveTime(long time) {
        return SAFE_FORMAT_LIVE_TIME.get().format(new Date(time));
    }

    public synchronized static String date2String(long time) {
        return SAFE_FORMAT_.get().format(new Date(time));
    }

    private History() {
        RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class)
                .subscribeOn(Schedulers.io())
                .map(setDataRsp -> {
                    int size = ListUtils.getSize(setDataRsp.rets);
                    for (int i = 0; i < size; i++) {
                        JFGDPMsgRet ret = setDataRsp.rets.get(i);
                        if (ret.id == 218 && ret.ret == 0) {
                            //格式化了sd卡.
                            clearHistoryFile(setDataRsp.uuid);
                        }
                    }
                    return null;
                })
                .retry()
                .subscribe();
    }

    public void queryHistory(String uuid, int endTime, int way, int count) {
        try {
            int ret = Command.getInstance().getVideoListV2(uuid,
                    endTime, way, count);
            rspIndexMap.put(uuid, ret == 0);
            AppLogger.d(String.format("ret:%s,uuid:%s,startTime:%s,way:%s,count:%s", ret, uuid, endTime, way, count));
        } catch (JfgException e) {
        }
    }

    /**
     * 非分享账号
     * 可以利用上 218这个消息,{@link com.cylan.jiafeigou.dp.DpMsgMap#ID_218_DEVICE_FORMAT_SDCARD}
     *
     * @param device
     * @return
     */
    public boolean queryHistory(Device device) {
        if (device == null || TextUtils.isEmpty(device.uuid)
                || !TextUtils.isEmpty(device.shareAccount)) {
            AppLogger.w("go back:" + device);
            return false;
        }
        cleanCache();
        try {
            //日期:1 分钟:0   getVideoList(device.uuid); 不用了
            queryHistory(device.uuid, (int) (System.currentTimeMillis() / 1000), 1, 365);
            AppLogger.w("getVideoList");
            return true;
        } catch (Exception e) {
            AppLogger.e("uuid is null: " + e.getLocalizedMessage());
            return false;
        }
    }

    public ArrayList<HistoryFile> getHistoryFile(String date) {
        ensureMap();
        return historyMap.get(date);
    }

    public HistoryFile getHistoryFile(long time) {
        final String date = parseTime2Date(TimeUtils.wrapToLong(time));
        AppLogger.d("historyFile:timeEnd?" + date);
        ArrayList<HistoryFile> list = getHistoryFile(date);
        final int timeInt = (int) (TimeUtils.wrapToLong(time) / 1000);
        if (list == null) return null;
        for (HistoryFile file : list) {
            if (file.getTime() <= timeInt && (file.getTime() + file.getDuration()) >= timeInt) {
                return file;
            }
        }
        return null;
    }

    public ArrayList<HistoryFile> getAllHistoryFile() {
        ensureMap();
        ArrayList<HistoryFile> files = new ArrayList<>();
        Iterator<String> iterator = historyMap.keySet().iterator();
        while (iterator.hasNext()) {
            String date = iterator.next();
            files.addAll(historyMap.get(date));
        }
        return files;
    }

    public ArrayList<Long> getDateList(String uuid) {
        synchronized (lock) {
            if (TextUtils.isEmpty(uuid)) {
                return null;
            }
            ensureMap();
            return dateListMap.get(uuid);
        }
    }

    private void fillDataList(String uuid, ArrayList<Integer> list) {
        if (ListUtils.isEmpty(list)) {
            return;
        }
        ensureMap();
        ArrayList<Long> tmp = new ArrayList<>(list.size());
        for (Integer integer : list) {
            tmp.add(integer * 1000L);
        }
        dateListMap.put(uuid, tmp);
    }

    public static String reverse(String input) {
        char[] in = input.toCharArray();
        int begin = 0;
        int end = in.length - 1;
        char temp;
        while (end > begin) {
            temp = in[begin];
            in[begin] = in[end];
            in[end] = temp;
            end--;
            begin++;
        }
        return new String(in);
    }

    /**
     * 转化出 180*8个字符的字符串。为了提取连续的1,从而压缩历史录像。
     *
     * @param unitList
     * @return
     */
    private static String flatBitList(List<DpMsgDefine.Unit> unitList) {
        PerformanceUtils.startTrace("flatBitList");
        LinkedList<DpMsgDefine.Unit> list = new LinkedList<>(unitList);
//        Collections.reverse(list);
        //
        StringBuilder builder = new StringBuilder();
        for (DpMsgDefine.Unit unit : list) {
            builder.append(reverse(flatIntTo8bitStr(unit.video)));
        }
        PerformanceUtils.stopTrace("flatBitList");
        return builder.toString();
    }

    private static String flatIntTo8bitStr(int data) {
        return String.format("%8s", Integer.toBinaryString(data))
                .replace(' ', '0');
    }

    private void fillListInDate(String uuid, Map<Integer, List<DpMsgDefine.Unit>> rspMap) {
        final int cnt = rspMap == null ? 0 : rspMap.size();
        if (cnt > 0) {
            AppLogger.d("日期不为空:" + GSON.toJson(rspMap));
        } else {
            AppLogger.d("日期为空，需要告诉UI");
            RxBus.getCacheInstance().post(new RxEvent.HistoryBack(true));
            return;
        }
        ArrayList<Integer> tmList = new ArrayList<>(rspMap.keySet());
        //来个降序吧
        Collections.sort(tmList, (o1, o2) -> (o2 - o1));
        fillDataList(uuid, tmList);

        //注意啊，有一个落后设备只能 一次查2天。
        final int total = 2;
        final int qNum = cnt / total + (cnt % total == 0 ? 0 : 1);//查询次数
        //        for (int i = 0; i < qNum; i++) {
//            final int startTime = tmList.get(i * total);
//            queryHistory(uuid, (int) (getSpecificDayEndTime(startTime * 1000L) / 1000),
//                    0, total);
//        }
        AppLogger.d("不能循环，把所有的录像分几次查回来，设备内存不足，需要等待一次响应再去查下一天的");
        queryHistory(uuid, (int) (getSpecificDayEndTime(tmList.get(0) * 1000L) / 1000),
                0, total);
    }

    /**
     * 历史数据从dataSource传过来.
     * 1.新的数据列表过来后,先清空db中这段时间的记录,然后存db
     *
     * @return
     */

    public void cacheHistoryDataList(byte[] rawV2Data) {
        if (rawV2Data == null || rawV2Data.length == 0) {
            RxBus.getCacheInstance().post(new RxEvent.HistoryBack(true));
            return;
        }
        DpMsgDefine.UniversalDataBaseRsp rsp = DpUtils.unpackDataWithoutThrow(rawV2Data,
                DpMsgDefine.UniversalDataBaseRsp.class, null);
        if (rsp == null) {
            AppLogger.e("bytes is null");
        } else {
            if (rsp.dataMap == null || rsp.dataMap.size() == 0) {
                RxBus.getCacheInstance().post(new RxEvent.HistoryBack(true));
                return;
            }
            if (rsp.way == 1) {
                //日期参数回来,有些设备坑了，一次只能返回两天。所以只能动态加载了。
                fillListInDate(rsp.caller, rsp.dataMap);
            } else {
                //按照分钟的查询回来
                parseBit(rsp.caller, rsp.dataMap);
            }
        }
        AppLogger.w("save hisFile tx:" + Arrays.toString(rawV2Data));
    }

    /**
     * 已经默认设备 对数据做好了按天分类
     *
     * @param uuid
     * @param dataMap
     */
    public void parseBit(String uuid, Map<Integer, List<DpMsgDefine.Unit>> dataMap) {
        synchronized (lock) {
            Set<Integer> dateSet = dataMap == null ? null : dataMap.keySet();
            PerformanceUtils.startTrace("parseBit");
            if (dateSet != null) {
                long maxTime = -1;
                for (Integer dateInInt : dateSet) {
                    if (dateInInt > maxTime) {
                        maxTime = dateInInt;
                    }
                    List<DpMsgDefine.Unit> units = dataMap.get(dateInInt);
                    //把这些Units,展开成最多180*8个字符串，然后提取连续的1凑成一个HistoryFile
                    final String longLongBits = flatBitList(units);
                    //一天的list
                    ArrayList<HistoryFile> list = squeeze(dateInInt, longLongBits);
                    final String dateInStr = parseTime2Date(dateInInt * 1000L);
                    AppLogger.d("list Size:" + ListUtils.getSize(units) + ",squeezeSize:" + ListUtils.getSize(list));
                    if (ListUtils.getSize(list) != 0) {
                        addDateList(dateInStr, list);
                    } else {
                        AppLogger.e("设备端出锅 发来空列表");
                    }
                }
                Boolean sent = rspIndexMap.get(uuid);
                if (sent != null && sent && maxTime != -1) {
                    rspIndexMap.put(uuid, false);
                    RxBus.getCacheInstance().post(new RxEvent.JFGHistoryVideoParseRsp(uuid)
                            .setTimeStart(maxTime));
                }
            }
            PerformanceUtils.stopTrace("parseBit");
        }
    }

    public void addDateList(final String date, ArrayList<HistoryFile> dateList) {
        if (historyMap == null) {
            historyMap = new ConcurrentHashMap<>();
        }
        ArrayList<HistoryFile> arrayList = historyMap.get(date);
        if (arrayList == null) {
            arrayList = new ArrayList<>();
        }
        arrayList.addAll(dateList);
        arrayList = new ArrayList<>(new TreeSet<>(dateList));
        Collections.sort(arrayList);
        historyMap.put(date, arrayList);
    }

    public void cacheHistoryDataList(JFGHistoryVideo historyVideo) {
        Observable.just(historyVideo)
                .subscribeOn(Schedulers.io())
                .filter(ret -> {
                    if (ListUtils.isEmpty(ret.list)) {
                        RxBus.getCacheInstance().post(new RxEvent.HistoryBack(true));
                        //清空
                        return false;
                    }
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .flatMap(historyVideo1 -> {
                    //设备端已经排好序了.
                    long time = System.currentTimeMillis();
                    String uuid = historyVideo.list.get(0).peer;
                    ArrayList<Long> timeList = new ArrayList<>();
                    ArrayList<JFGVideo> list = historyVideo1.list;
                    for (JFGVideo video : list) {
                        ensureMap();
                        final String dateInStr = parseTime2Date(video.beginTime * 1000L);
                        ArrayList<HistoryFile> historyFiles = historyMap.get(dateInStr);
                        if (historyFiles == null) {
                            historyFiles = new ArrayList<>();
                            historyMap.put(dateInStr, historyFiles);
                        }
                        timeList.add(video.beginTime);
                        HistoryFile file = new HistoryFile();
                        file.duration = video.duration;
                        file.time = video.beginTime;
                        file.uuid = video.peer;
                        historyFiles.add(file);
//                        Collections.reverse(historyFiles);//来个降序
                    }
                    list.clear();//需要清空
                    Collections.reverse(timeList);
                    try {
                        long timeStart = timeList.get(0), timeEnd = timeList.get(ListUtils.getSize(timeList) - 1);
                        AppLogger.w(String.format(Locale.getDefault(), "before insert uuid:%s,timeStart:%s,timeEnd:%s,performance:%s",
                                uuid, timeStart, timeEnd, (System.currentTimeMillis() - time)));
                        return Observable.just(new Helper(uuid, timeStart));
                    } catch (Exception e) {
                        AppLogger.e("err: " + MiscUtils.getErr(e));
                        return Observable.just(new Helper());
                    }
                })
                .subscribeOn(Schedulers.io())
                .filter(helper -> historyMap != null && historyMap.size() > 0)
                .subscribe(helper -> {
                    AppLogger.d("录像回来了");
                    RxBus.getCacheInstance().post(new RxEvent.JFGHistoryVideoParseRsp(helper.uuid)
                            .setTimeStart(helper.timeStart));
                }, throwable -> AppLogger.e("err: " + MiscUtils.getErr(throwable)));
    }

    public void cleanCache() {
        if (historyMap != null) {
            historyMap.clear();
        }
        if (dateListMap != null) {
            dateListMap.clear();
        }
    }

    public void clear() {
        history = null;
    }


    public void clearHistoryFile(String uuid) {
        synchronized (lock) {
            DataExt.getInstance().clean();
            cleanCache();
//            BaseApplication.getAppComponent().getDBHelper().deleteHistoryFile(uuid, 0, Integer.MAX_VALUE)
//                    .subscribeOn(Schedulers.io())
//                    .subscribe(ret -> {
//                    }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
        }
    }

    private static class Helper {
        private String uuid;
        private long timeStart;

        public Helper() {

        }

        public Helper(String uuid, long timeStart) {
            this.uuid = uuid;
            this.timeStart = timeStart;
        }
    }

    /**
     * 提取连续的 bit 1
     * 连个for 复杂度  O(n)
     *
     * @param startTime:设备保证这是一天的 凌晨时间戳，单位：秒
     * @param result
     */
    private ArrayList<HistoryFile> squeeze(long startTime, final String result) {
        ArrayList<HistoryFile> list = new ArrayList<>();
        final int len = result.length();
        Log.d(TAG, "squeeze,len: " + len);
        for (int i = 0; i < len; i++) {
            if (result.charAt(i) == '1') {
                int end = i;
                for (int j = i; j < len; j++) {
                    if (result.charAt(j) == '1') {
                        end = j;
                    } else {
                        break;
                    }
                }
                HistoryFile file = new HistoryFile();
                file.time = startTime + i * 60;
                file.duration = (end - i + 1) * 60;
                list.add(file);
                i = end;
            }
        }
        return list;
    }
}
