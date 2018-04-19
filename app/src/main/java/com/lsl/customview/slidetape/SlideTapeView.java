package com.lsl.customview.slidetape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;

/**
 * Created by lsl on 2018/04/17
 */

public class SlideTapeView extends View{

    private Paint mScalePaint;
    private Paint mTextPaint;
    private Paint mBaseScalePaint;

    private float mWidth;
    private float mHeight;
    private float mScaleSpace;
    private float mTranslocation;
    private float mTouchX;

    public SlideTapeView(Context context) {
        super(context);
        init();
    }

    public SlideTapeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideTapeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBaseScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScalePaint.setARGB(0xff, 0xe4, 0xe4, 0xe4);
        mScalePaint.setStrokeWidth(2);
        mTextPaint.setTextSize(40);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mBaseScalePaint.setARGB(0xff, 0x4f, 0xba, 0x77);
        mBaseScalePaint.setStrokeWidth(10);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mScaleSpace = mWidth / 30;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        ViewGroup parent = getScrollableParent();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                mTouchX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                mTranslocation += (int)(event.getX() - mTouchX);
                scrollBy((int) (mTouchX - event.getX()), 0);
                mTouchX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                if(parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                mTranslocation += event.getX() - mTouchX;
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(0, mHeight / 2);

        canvas.drawLine(-mTranslocation, 0, mWidth - mTranslocation, 0, mScalePaint);

        for(float i = upNumber(mTranslocation, mScaleSpace); i < mWidth + upNumber(mTranslocation, mScaleSpace); i += 30) {
            float scaleNum = i / 30;
            float x = scaleNum * mScaleSpace;      //当前刻度线的横坐标
            if(scaleNum % 10 == 0) {
                mScalePaint.setStrokeWidth(6);
                canvas.drawLine(x, 0, x, 150, mScalePaint);
                canvas.drawText(scaleNum / 10 + "", x, 200, mTextPaint);
            } else {
                mScalePaint.setStrokeWidth(4);
                canvas.drawLine(x, 0, x, 75, mScalePaint);
            }
        }

        canvas.drawLine(mWidth / 2 - mTranslocation, 0, mWidth / 2 - mTranslocation, 170, mBaseScalePaint);

        canvas.restore();
    }

    /**
     * 获得该控件的可滚动的父控件
     */
    private ViewGroup getScrollableParent() {
        View target = this;

        while(true){
            View parent;

            try{
                parent = (View) target.getParent();
            } catch (Exception e) {
                return null;
            }

            if(parent == null)
                return null;
            if(parent instanceof ListView || parent instanceof ScrollView || parent instanceof ViewPager){
                return (ViewGroup) parent;
            }
            target = parent;
        }
    }

    /**
     * 向上取整
     */
    private float upNumber(float x, float y) {
        x = -x;
        if(x % y == 0) {
            return (x / y) * 30;
        } else {
            return (int)(x / y + 1) * 30;
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }
}
