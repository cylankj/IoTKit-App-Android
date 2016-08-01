package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cylan.jiafeigou.R;

/**
 * 创建者     谢坤
 * 创建时间   2016/7/27 16:09
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */


public class SwitchButton extends View{

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    boolean isOn ;
    float curX = 0;
    float centerY; //y固定
    float viewWidth;
    float radius;
    float lineStart; //直线段开始的位置（横坐标)
    float lineEnd; //直线段结束的位置（纵坐标)
    float lineWidth;
    final int SCALE = 4; // 控件长度为滑动的圆的半径的倍数

    /**
     * 对开关进行接口回调的监听，当开关状态发生改变的时候，做相应的操作
     */
    OnStateChangedListener onStateChangedListener;

    //设置开关状态改变监听器
    public void setOnStateChangedListener(OnStateChangedListener o) {
        this.onStateChangedListener = o;
    }

    //内部接口，开关状态改变监听器
    public interface OnStateChangedListener {
        public void onStateChanged(boolean state);
    }


    public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SwitchButton(Context context) {
        this(context,null);
    }



        @Override
        public boolean onTouchEvent(MotionEvent event) {
            curX = event.getX();
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(curX > viewWidth / 2) {
                    curX = lineEnd;
                    if(false == isOn) {
                        //只有状态发生改变才调用回调函数， 下同
                        if(null != onStateChangedListener) {
                            onStateChangedListener.onStateChanged(true);
                        }
                        isOn = true;
                    }
                }else{
                    curX = lineStart;
                    if(true == isOn) {
                        if(null != onStateChangedListener) {
                            onStateChangedListener.onStateChanged(false);
                        }
                        isOn = false;
                    }
                }
            }

            //通过刷新调用onDraw
            postInvalidate();
            return true;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            //保持宽是高的SCALE/2倍,即圆的直径
            setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth() * 2 / SCALE);
            viewWidth = getMeasuredWidth();
            radius = viewWidth / SCALE;
            lineWidth = radius ; //直线宽度等于滑块直径
            curX = radius;
            centerY = getMeasuredWidth() / SCALE; //centerY为高度的一半
            lineStart = radius;
            lineEnd = (SCALE - 1) * radius;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //限制滑动范围
            curX = curX > lineEnd?lineEnd:curX;
            curX = curX < lineStart?lineStart:curX;

            //划线
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(lineWidth);
            //左边部分的线
            mPaint.setColor(getResources().getColor(R.color.color_a6e4ff));
            canvas.drawLine(lineStart,centerY, curX, centerY, mPaint);
            //右边部分的线
            mPaint.setColor(getResources().getColor(R.color.color_b9b8b8));
            canvas.drawLine(curX, centerY, lineEnd, centerY, mPaint);

            //画圆
            //画最左和最右的圆，直径为直线段宽度，即在直线段两边分别再加上一个半圆
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(getResources().getColor(R.color.color_b9b8b8));
            canvas.drawCircle(lineEnd, centerY, lineWidth /2, mPaint);
            mPaint.setColor(getResources().getColor(R.color.color_a6e4ff));
            canvas.drawCircle(lineStart, centerY, lineWidth / 2, mPaint);

            //圆形滑块
            if(false == isOn){
                mPaint.setColor(getResources().getColor(R.color.color_f1f1f1));
                canvas.drawCircle(curX, centerY, radius , mPaint);
            }else {
                mPaint.setColor(getResources().getColor(R.color.color_36bdff));
                canvas.drawCircle(curX, centerY, radius, mPaint);
            }
        }

    /**
     * 传入开关的状态，用来进行开关的  开关切换
     * @param isToggle
     */
    /*public void setToggle(boolean isToggle){
        isOn = isToggle;

        //根据传递过来的boolean来设定开关状态

        if(isOn){ //true
            curX = lineEnd;
            postInvalidate();
        }else{
            curX = lineStart;
            postInvalidate();
        }
    }*/

}
