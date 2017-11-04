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
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
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

    private static final SimpleDateFormat SAFE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
    private volatile static History history;
    private HashMap<String, ArrayList<Long>> dateListMap = new HashMap<>();
    private ConcurrentHashMap<String, ArrayList<HistoryFile>> historyMap;

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

    private void queryHistory(String uuid, int startTime, int way, int count) {
        try {
            int ret = BaseApplication.getAppComponent().getCmd().getVideoListV2(uuid,
                    startTime, way, count);
            AppLogger.d("ret:" + ret);
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
//            BaseApplication.getAppComponent().getCmd().getVideoList(device.uuid);
            //日期:1 分钟:0
            queryHistory(device.uuid, (int) (System.currentTimeMillis() / 1000), 1, 365);
            AppLogger.w("getVideoList");
            return true;
        } catch (Exception e) {
            AppLogger.e("uuid is null: " + e.getLocalizedMessage());
            return false;
        }
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

    /**
     * 转化出 180*8个字符的字符串。为了提取连续的1,从而压缩历史录像。
     *
     * @param unitList
     * @return
     */
    private static String flatBitList(List<DpMsgDefine.Unit> unitList) {
        LinkedList<DpMsgDefine.Unit> list = new LinkedList<>(unitList);
        Collections.reverse(list);
        //
        StringBuilder builder = new StringBuilder();
        for (DpMsgDefine.Unit unit : list) {
            builder.append(flatIntTo8bitStr(unit.video));
        }
        return builder.toString();
    }

    private static String flatIntTo8bitStr(int data) {
        return String.format("%8s", Integer.toBinaryString(data))
                .replace(' ', '0');
    }

    private void queryListInDate(String uuid, Map<Integer, List<DpMsgDefine.Unit>> rsp) {
        Set<Integer> dateSet = rsp == null ? null : rsp.keySet();
        if (dateSet != null && dateSet.size() > 0) {
            AppLogger.d("日期不为空:" + GSON.toJson(dateSet));
        } else {
            AppLogger.d("日期为空，需要告诉UI");
            RxBus.getCacheInstance().post(new RxEvent.HistoryBack(true));
            return;
        }
        ArrayList<Integer> dateList = new ArrayList<>(dateSet);
        final int cnt = ListUtils.getSize(dateList);
        if (cnt == 0) {
            //没有录像
            return;
        }
        ArrayList<Integer> tmList = new ArrayList<>(dateList);
        //来个降序吧
        Collections.sort(tmList, (o1, o2) -> (o2 - o1));
        fillDataList(uuid, tmList);

        //注意啊，有一个落后设备只能 一次查3天。
        final int total = 10;
        final int qNum = cnt / total + (cnt % total == 0 ? 0 : 1);//查询次数
        for (int i = 0; i < qNum; i++) {
            final int startTime = tmList.get(i * total);
            queryHistory(uuid, (int) (getSpecificDayEndTime(startTime * 1000L) / 1000),
                    0, total);
        }
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
        }
        DpMsgDefine.UniversalDataBaseRsp rsp = DpUtils.unpackDataWithoutThrow(rawV2Data,
                DpMsgDefine.UniversalDataBaseRsp.class, null);
        if (rsp == null) {
            AppLogger.e("bytes is null");
        } else {
            if (rsp.way == 1) {
                //日期参数回来
                queryListInDate(rsp.caller, rsp.dataMap);
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
        Set<Integer> dateSet = dataMap == null ? null : dataMap.keySet();
        PerformanceUtils.startTrace("parseBit");
        if (dateSet != null) {
            Iterator<Integer> integerIterator = dateSet.iterator();
            while (integerIterator.hasNext()) {
                final Integer dateInInt = integerIterator.next();
                List<DpMsgDefine.Unit> units = dataMap.get(dateInInt);
                //把这些Units,展开成最多180*8个字符串，然后提取连续的1凑成一个HistoryFile
                final String longLongBits = flatBitList(units);
                //一天的list
                ArrayList<HistoryFile> list = squeeze(dateInInt, longLongBits);
                final String dateInStr = SAFE_FORMAT.format(new Date(dateInInt * 1000L));
                Log.d(TAG, "list Size:" + ListUtils.getSize(units));
                addDateList(dateInStr, list);
            }
            RxBus.getCacheInstance().post(new RxEvent.JFGHistoryVideoParseRsp(uuid)
                    .setTimeList(dateListMap.get(uuid)));
        }
        PerformanceUtils.stopTrace("parseBit");
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
                    ArrayList<HistoryFile> historyFiles = new ArrayList<>(ListUtils.getSize(list));
                    for (JFGVideo video : list) {
                        timeList.add(video.beginTime);
                        HistoryFile file = new HistoryFile();
                        file.duration = video.duration;
                        file.time = video.beginTime;
                        file.uuid = video.peer;
                        historyFiles.add(file);
                    }
                    list.clear();//需要清空
                    Collections.reverse(historyFiles);//来个降序
                    Collections.reverse(timeList);
                    try {
                        long timeStart = timeList.get(0), timeEnd = timeList.get(ListUtils.getSize(timeList) - 1);
                        AppLogger.w(String.format(Locale.getDefault(), "before insert uuid:%s,timeStart:%s,timeEnd:%s,performance:%s",
                                uuid, timeStart, timeEnd, (System.currentTimeMillis() - time)));
                        return Observable.just(new Helper(uuid, timeList, historyFiles));
                    } catch (Exception e) {
                        AppLogger.e("err: " + MiscUtils.getErr(e));
                        return Observable.just(new Helper());
                    }
                })
                .subscribeOn(Schedulers.io())
                .filter(helper -> !TextUtils.isEmpty(helper.uuid) && ListUtils.getSize(helper.files) > 0)
                .map(helper -> {
                    long timeStart = helper.timeList.get(0);
                    long timeEnd = helper.timeList.get(ListUtils.getSize(helper.timeList) - 1);
                    BaseApplication.getAppComponent().getDBHelper().deleteHistoryFile(helper.uuid, Math.min(timeStart, timeEnd),
                            Math.max(timeStart, timeEnd))
                            .subscribeOn(Schedulers.io())
                            .map(aBoolean -> {
                                AppLogger.w("delete hisFile:" + aBoolean + ",hisFile:" + ListUtils.getSize(helper.files));
                                BaseApplication.getAppComponent().getDBHelper().saveHistoryFile(helper.files)
                                        .subscribe(ret -> {
                                            AppLogger.w("save hisFile tx");
                                            RxBus.getCacheInstance().post(new RxEvent.JFGHistoryVideoParseRsp(helper.uuid)
                                                    .setTimeList(helper.timeList));
                                        }, AppLogger::e);
                                return null;
                            })
                            .subscribe(ret -> AppLogger.w("save history good?"),
                                    throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
                    return null;
                })
                .subscribe(ret -> {
                }, throwable -> AppLogger.e("err: " + MiscUtils.getErr(throwable)));
    }

    public void cleanCache() {
        if (historyMap != null) {
            historyMap.clear();
        }
    }

    public void clear() {
        history = null;
    }


    public boolean clearHistoryFile(String uuid) {
        synchronized (lock) {
            DataExt.getInstance().clean();
            cleanCache();
            BaseApplication.getAppComponent().getDBHelper().deleteHistoryFile(uuid, 0, Integer.MAX_VALUE)
                    .subscribeOn(Schedulers.io())
                    .subscribe(ret -> {
                    }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
        }
        return true;
    }

    private static class Helper {
        private String uuid;
        private ArrayList<Long> timeList;
        private ArrayList<HistoryFile> files;

        public Helper() {

        }

        public Helper(String uuid, ArrayList<Long> timeList, ArrayList<HistoryFile> files) {
            this.uuid = uuid;
            this.files = files;
            this.timeList = timeList;
        }
    }

    /**
     * 提取连续的 bit 1
     * 连个for 复杂度  O(n)
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
