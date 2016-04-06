package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.DswLog;
import com.cylan.jiafeigou.entity.msg.MsgTimeData;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by HeBin on 2015/5/6.
 */
public class CustomProgressView extends View {

    private Paint mPaint;

    private int yuliu;

    private List<MsgTimeData> list = new ArrayList<>();

    private int wHeight;

    private int wWidth;

    public CustomProgressView(Context context) {
        this(context, null);
    }

    public CustomProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        wHeight = DensityUtil.dip2px(getContext(), 55);
        wWidth = DensityUtil.getScreenWidth(getContext()) + DensityUtil.dip2px(getContext(), 24 * 100);
        yuliu = DensityUtil.getScreenWidth(this.getContext()) / 2;
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.history_video_lan));
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        DswLog.i("onDraw");
        for (int i = 0; i < list.size(); i++) {
            int width = (DensityUtil.dip2px(getContext(), list.get(i).time / 60 == 0 ? 1 : list.get(i).time / 60) * 5 / 3);
            float a = getMaginLeft(list.get(i).begin) + yuliu;
            int top = DensityUtil.dip2px(getContext(), 8);
            canvas.drawRect(a, top, a + width, wHeight - top, mPaint);
        }
    }


    private float getMaginLeft(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.UK);

        String str = dateFormat.format(new Date(time * 1000));
        System.out.println("time----->" + time + "----------------str---->" + str);
        String[] s = str.split(":");
        int size = StringUtils.toInt(s[0]) * 60 + StringUtils.toInt(s[1]);
        int smoothSize = size * 60 / 36;
        return DensityUtil.dip2px(this.getContext(), smoothSize);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        setMeasuredDimension(wWidth, wHeight);
    }

    public void setList(List<MsgTimeData> mList) {
        this.list = mList;
        invalidate();
    }

    public int getCount() {
        return list.size();
    }
}
