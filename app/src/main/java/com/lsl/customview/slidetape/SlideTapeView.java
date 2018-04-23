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

    //惯性滑动相关
    private static final int FLING_MIN_SPEED = 500;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private Paint mScalePaint;
    private Paint mTextPaint;
    private Paint mBaseScalePaint;

    private float mWidth;
    private float mHeight;
    private float mScaleSpace;
    private float mLastTouchX;

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
                scrollBy((int) (mLastTouchX - event.getX()), 0);
                mLastTouchX = event.getX();
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
        mScroller.fling(getScrollX(), 0, x, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(0, mHeight / 2);

        //横向的位移，向左为正，向右为负
        float translocation = getScrollX();

        //横向的刻度线
        canvas.drawLine(translocation, 0, mWidth + translocation, 0, mScalePaint);
        //纵向刻度线
        for(float i = upNumber(translocation, mScaleSpace); i < mWidth + upNumber(translocation, mScaleSpace); i += 30) {
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

        //基线
        canvas.drawLine(mWidth / 2 + translocation, 0, mWidth / 2 + translocation, 170, mBaseScalePaint);

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
     * 根据x的范围返回值：
     * (-72,-36] -> -30
     * (-36,0]   -> 0
     * (0,36]    -> 30
     * (36,72]   -> 60
     */
    private float upNumber(float x, float y) {
        if(x % y == 0) {
            return (x / y) * 30;
        } else if(x > 0){
            return (int) (x / y + 1) * 30;
        } else {
            return (int) (x / y) * 30;
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
