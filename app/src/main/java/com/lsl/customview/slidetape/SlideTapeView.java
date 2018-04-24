package com.lsl.customview.slidetape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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

import java.math.BigDecimal;

/**
 * Created by lsl on 2018/04/17
 */

public class SlideTapeView extends View{

    //惯性滑动相关
    private static final int FLING_MIN_SPEED = 500;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private Paint mScalePaint;
    private Paint mTextPaint;
    private Paint mBaseScalePaint;

    private float mWidth;
    private float mHeight;
    private int mScaleNum = 30;     //刻度线个数
    private float mScaleSpace;      //刻度线间距
    private float mScaleGranularity = 0.1f; //刻度粒度
    private int mInitScale = 0;     //初始刻度值
    private int mMinScale = 0;      //最小刻度值
    private int mMaxScale = 100;    //最大刻度值

    private float mLastTouchX;  //上一次的触摸点

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
        mScroller = new Scroller(getContext());
        mScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBaseScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScalePaint.setARGB(0xff, 0xe4, 0xe4, 0xe4);
        mScalePaint.setStrokeWidth(2);
        mTextPaint.setTextSize(40);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mBaseScalePaint.setARGB(0xff, 0x4f, 0xba, 0x77);
        mBaseScalePaint.setStrokeWidth(10);
        mBaseScalePaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mScaleSpace = mWidth / mScaleNum;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        if(mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        ViewGroup parent = getScrollableParent();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                mLastTouchX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float minX = - (mInitScale - mMinScale) * mScaleSpace / mScaleGranularity;   //左边界坐标
                float maxX = (mMaxScale - mInitScale) * mScaleSpace / mScaleGranularity;   //右边界坐标
                //右划未超过左边界，左划未超过右边界
                if((event.getX() > mLastTouchX && getScrollX() > minX) ||
                        (event.getX() < mLastTouchX && getScrollX() < maxX)) {
                    scrollBy((int) (mLastTouchX - event.getX()), 0);
                    mLastTouchX = event.getX();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                //抬手时速度大于阈值，执行fling动作
                mVelocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) mVelocityTracker.getXVelocity();
                if(Math.abs(velocityX) > FLING_MIN_SPEED) {
                    fling(-velocityX);
                } else {
                    scrollToScaleLine();
                }
                if(mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return true;
    }

    /**
     *  滑动到最近的刻度线上
     */
    private void scrollToScaleLine() {
        float translocation = getScrollX();
        if(translocation % mScaleSpace == 0) {
            return;
        }

        int left, right;    //左右两边最近的刻度线
        if(translocation > 0) {
            left = (int) (translocation / mScaleSpace);
            right = left + 1;
        } else {
            right = (int) (translocation / mScaleSpace);
            left = right - 1;
        }

        float mid = (mScaleSpace * left + mScaleSpace * right) / 2;
        int transX;
        if(translocation < mid) {
            transX = (int) (mScaleSpace * left - translocation);
        } else {
            transX = (int) (mScaleSpace * right - translocation);
        }
        mScroller.startScroll(getScrollX(), 0, transX, 0, 500);
        invalidate();
    }

    private void fling(int x) {
        float minX = - (mInitScale - mMinScale) * mScaleSpace / mScaleGranularity;   //左边界坐标
        float maxX = (mMaxScale - mInitScale) * mScaleSpace / mScaleGranularity;   //右边界坐标
        mScroller.fling(getScrollX(), 0, x, 0, (int) minX, (int) maxX, 0, 0);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(0, mHeight / 2);

        drawHorizontalLine(canvas);
        drawScaleLine(canvas);
        drawBaseScale(canvas);

        canvas.restore();
    }

    /**
     * 绘制水平的刻度线
     */
    private void drawHorizontalLine(Canvas canvas) {
        float translocation = getScrollX();
        float minX = mWidth / 2 - (mInitScale - mMinScale) * mScaleSpace / mScaleGranularity;   //左边界坐标
        float maxX = mWidth / 2 + (mMaxScale - mInitScale) * mScaleSpace / mScaleGranularity;   //右边界坐标
        canvas.drawLine(Math.max(translocation, minX), 0, Math.min(mWidth + translocation, maxX), 0, mScalePaint);
    }

    /**
     * 绘制刻度线以及刻度值
     */
    private void drawScaleLine(Canvas canvas) {
        float translocation = getScrollX();
        float startX = mInitScale - mScaleGranularity * mScaleNum / 2;     //第一条刻度线的值
        for(float i = upNumber(translocation, mScaleSpace); i < mWidth + upNumber(translocation, mScaleSpace); i += mScaleNum) {
            float scaleIndex = i / mScaleNum;
            float scaleX = scaleIndex * mScaleSpace;      //当前刻度线的横坐标
            float scaleValue = startX + scaleIndex * mScaleGranularity;   //当前刻度线的值
            if(scaleValue >= mMinScale && scaleValue <= mMaxScale) {
                if (scaleValue % 1 == 0) {
                    mScalePaint.setStrokeWidth(6);
                    canvas.drawLine(scaleX, 0, scaleX, 150, mScalePaint);
                    canvas.drawText((int) scaleValue + "", scaleX, 220, mTextPaint);
                } else {
                    mScalePaint.setStrokeWidth(4);
                    canvas.drawLine(scaleX, 0, scaleX, 75, mScalePaint);
                }
            }
        }
    }

    /**
     * 绘制基准线以及选中的的刻度值
     */
    private void drawBaseScale(Canvas canvas) {
        float translocation = getScrollX();
        //基准线
        canvas.drawLine(mWidth / 2 + translocation, 0, mWidth / 2 + translocation, 170, mBaseScalePaint);
        //选中文字
        Rect rect = new Rect();
        float reciprocal = 1 / mScaleGranularity;
        String scaleText = mInitScale + ((int) (translocation / mScaleSpace)) / reciprocal + "";
        mBaseScalePaint.setTextSize(100);
        mBaseScalePaint.getTextBounds(scaleText, 0, scaleText.length(), rect);
        canvas.drawText(scaleText + "", mWidth / 2 + translocation, -80, mBaseScalePaint);
        //单位
        mBaseScalePaint.setTextSize(50);
        canvas.drawText("kg", mWidth / 2 + rect.right + translocation, rect.top - 80, mBaseScalePaint);
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
     * 根据x的范围返回值：
     * (-72,-36] -> -mScaleNum
     * (-36,0]   -> 0
     * (0,36]    -> mScaleNum
     * (36,72]   -> 2*mScaleNum
     */
    private float upNumber(float x, float y) {
        if(x % y == 0) {
            return (x / y) * mScaleNum;
        } else if(x > 0){
            return (int) (x / y + 1) * mScaleNum;
        } else {
            return (int) (x / y) * mScaleNum;
        }
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            if(!mScroller.computeScrollOffset()) {
                scrollToScaleLine();
            }
            invalidate();
        }
    }
}
