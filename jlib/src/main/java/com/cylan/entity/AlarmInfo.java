package com.cylan.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AlarmInfo implements Parcelable {

    public String cid;
    public boolean isEnabled = false;
    public boolean isPush = false;
    public String startTime = "0:00", endTime = "23:59";
    public int days = 0;
    public boolean isLedOpen = true;
    public int sound = 0;
    public int direction = 0;
    public String timezonestr;
    public int timezone = 0;//for dog
    public int sound_long = 0;
    public int auto_record = 0;
    public int vid = 0;
    public List<CidList> mCidList;
    public int sensitivity;
    public boolean isNTSC;
    public boolean isMobile;

    @SuppressWarnings("unchecked")
    public AlarmInfo(Parcel source) {
        isEnabled = source.readInt() == 1;
        isPush = source.readInt() == 1;
        days = source.readInt();
        startTime = source.readString();
        endTime = source.readString();
        cid = source.readString();
        isLedOpen = source.readInt() == 1;
        sound = source.readInt();
        direction = source.readInt();
        timezonestr = source.readString();
        timezone = source.readInt();
        sound_long = source.readInt();
        auto_record = source.readInt();

        vid = source.readInt();
        isNTSC = source.readInt() == 1;
        isMobile = source.readInt() == 1;
        sensitivity = source.readInt();

    }

    public AlarmInfo() {
    }

    public static class CidList {
        public String cid;
        public int enable;
        public String alias;
        public int os;
    }

    public static int parseTime(String time) {
        String[] times = time.split(":");
        final String h = Integer.toBinaryString(times[0].length() > 1 ? Integer.parseInt(times[0].substring(times[0].length() - 2, times[0].length())) : Integer.parseInt(times[0].substring(times[0].length() - 1, times[0].length())));
        final String m = String.format("%08d",
                Integer.parseInt(Integer.toBinaryString(Integer.parseInt(times[1]))));
        return Integer.parseInt(h + m, 2);
    }

    public static String parse2Time(int value) {
        return String.format("%02d", value >> 8) + String.format(":%02d", (((byte) value << 8) >> 8));
    }

    public static boolean isSelectedDay(int values, int day) {
        try {
            return String.format("%07d", Integer.parseInt(Integer.toBinaryString(values), 10))
                    .charAt(day) != '0';
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAllDay() {
        return "00:00".equals(startTime) && "23:59".equals(endTime);
    }

    public boolean isInDay(int day, int hour, int minute) {
        String[] starts = startTime.split(":");
        String[] ends = endTime.split(":");
        int start_h = Integer.parseInt(starts[0]);
        int start_m = Integer.parseInt(starts[1]);
        int end_h = Integer.parseInt(ends[0]);
        int end_m = Integer.parseInt(ends[1]);
        boolean dayFlag = AlarmInfo.isSelectedDay(days, day);
        boolean beginTimeFlag = false;
        boolean endTimeFlag = false;
        boolean crossDayFlag = true;

        if (start_h < end_h || (start_h == end_h && start_m <= end_m)) crossDayFlag = false;
        if (start_h < hour || (hour == start_h && start_m < minute)) beginTimeFlag = true;
        if (hour < end_h || (hour == end_h && minute < end_m)) endTimeFlag = true;

        if (crossDayFlag) {
            int yesterday = (day + 6) % 7;
            boolean yesterdayFlag = AlarmInfo.isSelectedDay(days, yesterday);
            return beginTimeFlag && dayFlag || endTimeFlag && yesterdayFlag;
        } else {
            if (!dayFlag) return false;
            return beginTimeFlag && endTimeFlag;
        }
    }

    public boolean isAlarm(long date) {
        if (!isEnabled) {
            return false;
        }

        Calendar aCalendar = Calendar.getInstance();
        aCalendar.setTime(new Date(date));
        int day = aCalendar.get(Calendar.DAY_OF_WEEK);
        int hour = aCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = aCalendar.get(Calendar.MINUTE);
        if (day == Calendar.SUNDAY)
            day = 6;
        else
            day = day - 2;
        return isInDay(day, hour, minute);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(isPush ? 1 : 0);
        dest.writeInt(isEnabled ? 1 : 0);
        dest.writeInt(days);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(cid);
        dest.writeInt(isLedOpen ? 1 : 0);
        dest.writeInt(sound);
        dest.writeInt(direction);
        dest.writeString(timezonestr);
        dest.writeInt(timezone);
        dest.writeInt(sound_long);
        dest.writeInt(auto_record);
        dest.writeInt(vid);
        dest.writeInt(isNTSC ? 1 : 0);
        dest.writeInt(isMobile ? 1 : 0);
        dest.writeInt(sensitivity);
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);

    }

    public static final Parcelable.Creator<AlarmInfo> CREATOR = new Creator<AlarmInfo>() {

        public AlarmInfo[] newArray(int size) {
            return new AlarmInfo[size];
        }

        public AlarmInfo createFromParcel(Parcel source) {
            return new AlarmInfo(source);
        }
    };
}
