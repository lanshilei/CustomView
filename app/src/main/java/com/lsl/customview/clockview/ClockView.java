package com.lsl.customview.clockview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;

/**
 * Created by lsl on 2018/04/13
 */

public class ClockView extends View {

    private float mDialRadius;          //表盘半径
    private float mActualRadius;        //减去padding后的实际绘制半径
    private float mQuarterLength;       //一刻钟刻度线长度
    private float mFiveMultipleLength;  //5的整数倍刻度线长度
    private float mOtherScaleLength;    //其他刻度线长度
    private float mHourHandWidth;       //时针宽度
    private float mMinuteHandWidth;     //分针宽度
    private float mSecondHandWidth;     //秒针宽度
    private float mHourHandLength;      //时针长度
    private float mMinuteHandLength;    //分针长度
    private float mSecondHandLength;    //秒针长度
    private float mHandEndLength;       //指针末尾长度
    private float mTextSize;            //刻度文字字体

    private Paint paint;

    public ClockView(Context context) {
        super(context);
        init();
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    }

    private void resize(float radius) {
        this.mDialRadius = radius;
        this.mActualRadius = radius * 24 / 25;
        this.mQuarterLength = radius / 10;
        this.mFiveMultipleLength = radius / 13;
        this.mOtherScaleLength = radius / 16;
        this.mHourHandWidth = radius / 25;
        this.mMinuteHandWidth = radius / 40;
        this.mSecondHandWidth = radius / 80;
        this.mHourHandLength = radius / 2;
        this.mMinuteHandLength = radius / 1.4f;
        this.mSecondHandLength = radius * 24 / 25;
        this.mHandEndLength = radius / 7;
        this.mTextSize = radius * 2 / 25;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resize(Math.min(w, h) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //原点移动到画布中心方便计算
        canvas.save();
        canvas.translate(getWidth() / 2, getHeight() / 2);
        //绘制表盘
        drawDial(canvas);
        //绘制刻度线
        drawScaleLine(canvas);
        //绘制刻度文字
        drawScaleText(canvas);
        //绘制指针
        drawPointer(canvas);
        canvas.restore();

        postInvalidateDelayed(100);
    }

    private void drawDial(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(0, 0, mDialRadius, paint);
        paint.setColor(Color.GREEN);
    }

    private void drawScaleLine(Canvas canvas) {
        for(int i = 0; i < 60; i++) {
            float scaleLength;
            if(i % 15 == 0) {
                paint.setStrokeWidth(1.8f);
                paint.setARGB(255, 0, 0, 0);
                scaleLength = mQuarterLength;
            } else if(i % 5 == 0) {
                paint.setStrokeWidth(1.4f);
                paint.setARGB(127, 0, 0, 0);
                scaleLength = mFiveMultipleLength;
            } else {
                paint.setStrokeWidth(1.0f);
                paint.setARGB(63, 0, 0, 0);
                scaleLength = mOtherScaleLength;
            }
            canvas.drawLine(0, -mActualRadius, 0, -mActualRadius + scaleLength, paint);
            canvas.rotate(6);
        }
    }

    private void drawScaleText(Canvas canvas) {
        float angle = (float)(6 * Math.PI / 180);   //刻度线之间的弧度
        float x = 0, y;             //12点刻度线的文字坐标

        paint.setColor(Color.BLACK);
        paint.setTextSize(mTextSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        for(int i = 0; i < 60; i += 5) {

            float scaleLength;
            if(i % 15 == 0) {
                scaleLength = mQuarterLength;
            } else {
                scaleLength = mFiveMultipleLength;
            }

            y = -mActualRadius + scaleLength + fontMetrics.descent - fontMetrics.ascent;
            String text = (i == 0 ? 12 : i / 5) + "";

            //各个文字的坐标
            float xi = (float) (x * Math.cos(angle * i) - y * Math.sin(angle * i));
            float yi = (float) (x * Math.sin(angle * i) + y * Math.cos(angle * i));

            canvas.drawText(text, xi, yi - (fontMetrics.bottom + fontMetrics.top) / 2, paint);
        }
    }

    private void drawPointer(Canvas canvas){
        Calendar calendar = getTime();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        float hourAngle = hour * 360 / 12;
        float minuteAngle = minute * 360 / 60;
        float secondAngle = second * 360 / 60;

        canvas.save();
        canvas.rotate(hourAngle);
        canvas.drawRoundRect(-mHourHandWidth / 2, -mHourHandLength, mHourHandWidth / 2, mHandEndLength, mHourHandWidth / 2, mHourHandWidth / 2, paint);
        canvas.restore();

        canvas.save();
        canvas.rotate(minuteAngle);
        canvas.drawRoundRect(-mMinuteHandWidth / 2, -mMinuteHandLength, mMinuteHandWidth / 2, mHandEndLength, mMinuteHandWidth / 2, mMinuteHandWidth / 2, paint);
        canvas.restore();

        paint.setColor(Color.RED);
        canvas.save();
        canvas.rotate(secondAngle);
        canvas.drawRoundRect(-mSecondHandWidth / 2, -mSecondHandLength, mSecondHandWidth / 2, mHandEndLength, mSecondHandWidth / 2, mSecondHandWidth / 2, paint);
        canvas.restore();

        canvas.drawCircle(0, 0, mSecondHandWidth * 3, paint);
    }

    private Calendar getTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar;
    }
}
