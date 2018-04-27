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
    /**
     * mScaleNum * mScaleSpace = mWidth
     * 这两个属性赋值一个即可
     */
    private int mScaleNum = 30;     //刻度线个数
    private float mScaleSpace;      //刻度线间距
    private int mInitScale = 0;     //初始刻度
    private int mMinScale = -100;   //最小刻度
    private int mMaxScale = 100;    //最大刻度
    private float mScaleGranularity = 1f;    //大刻度粒度
    private float mFinerGranularity = 0.1f;   //小刻度粒度
    private int mScaleCount = (int) (mScaleGranularity / mFinerGranularity);    //两条大刻度线之间的刻度数
    private String mUnit = "kg";       //单位

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
                //停止上一次还未完成的滑动
                if(!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastTouchX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float minX = - (mInitScale - mMinScale) * mScaleSpace;   //左边界坐标
                float maxX = (mMaxScale - mInitScale) * mScaleSpace;   //右边界坐标
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
            //离左边近
            transX = (int) (mScaleSpace * left - translocation);
        } else {
            //离右边近
            transX = (int) (mScaleSpace * right - translocation);
        }
        mScroller.startScroll(getScrollX(), 0, transX, 0, 500);
        invalidate();
    }

    private void fling(int x) {
        float minX = - (mInitScale - mMinScale) * mScaleSpace;   //左边界坐标
        float maxX = (mMaxScale - mInitScale) * mScaleSpace;   //右边界坐标
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
        float minX = mWidth / 2 - (mInitScale - mMinScale) * mScaleSpace;   //左边界坐标
        float maxX = mWidth / 2 + (mMaxScale - mInitScale) * mScaleSpace;   //右边界坐标
        canvas.drawLine(Math.max(translocation, minX), 0, Math.min(mWidth + translocation, maxX), 0, mScalePaint);
    }

    /**
     * 绘制刻度线以及刻度值
     */
    private void drawScaleLine(Canvas canvas) {
        //只绘制屏幕内的刻度
        for(int i = 0; i <= mScaleNum; i++) {
            int index = i + getScaleTransNumber();

            int scaleIndex = getIndexOfScale(index);          //当前刻度线的index
            float scaleX = getXCoordinateOfScale(index);      //当前刻度线的横坐标

            if(scaleIndex >= mMinScale && scaleIndex <= mMaxScale) {
                if (scaleIndex % mScaleCount == 0) {
                    //长刻度线
                    float reciprocal = 1 / mFinerGranularity;
                    mScalePaint.setStrokeWidth(6);
                    canvas.drawLine(scaleX, 0, scaleX, 150, mScalePaint);
                    canvas.drawText(scaleIndex / reciprocal + "", scaleX, 220, mTextPaint);
                } else {
                    //短刻度线
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
        float reciprocal = 1 / mFinerGranularity;
        String scaleText = (mInitScale + (int) (translocation / mScaleSpace)) / reciprocal + "";
        mBaseScalePaint.setTextSize(100);
        mBaseScalePaint.getTextBounds(scaleText, 0, scaleText.length(), rect);
        canvas.drawText(scaleText + "", mWidth / 2 + translocation, -80, mBaseScalePaint);
        //单位
        mBaseScalePaint.setTextSize(50);
        canvas.drawText(mUnit, mWidth / 2 + rect.right + translocation, rect.top - 80, mBaseScalePaint);
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
     * 获取基准线左边的刻度线个数
     */
    private int getScaleNumLeft() {
        if(mScaleNum % 2 == 0) {
            return mScaleNum / 2;
        } else {
            return (mScaleNum - 1) / 2;
        }
    }

    /**
     * 返回当前刻度线的index
     * 以刻度0位原点，向左为负，向右为正
     * @param i 屏幕中的第几条刻度线
     */
    private int getIndexOfScale(int i) {
        return mInitScale - getScaleNumLeft() + i;
    }

    /**
     * 返回当前刻度线的横坐标
     * @param i 屏幕中的第几条刻度线
     */
    private float getXCoordinateOfScale(int i) {
        //第一条刻度线的横坐标，刻度线个数为偶数从0开始，为奇数从一半scaleSpace开始
        float startX = mScaleNum % 2 * mScaleSpace / 2;
        return startX + i * mScaleSpace;
    }

    /**
     * 将横向的坐标位移转换为刻度的位移
     * @return  当前已经移动了几条刻度线，向右为正，向左为负
     */
    private int getScaleTransNumber() {
        float x = getScrollX();     //横向的坐标位移
        if(x % mScaleSpace == 0) {
            return (int) (x / mScaleSpace);
        } else if(x > 0){
            return (int) (x / mScaleSpace + 1);
        } else {
            return (int) (x / mScaleSpace);
        }
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //滑动结束后停在最近的刻度线上
            if(!mScroller.computeScrollOffset()) {
                scrollToScaleLine();
            }
            invalidate();
        }
    }
}
