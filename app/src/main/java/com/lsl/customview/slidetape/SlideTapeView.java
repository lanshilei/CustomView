package com.lsl.customview.slidetape;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
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

import com.lsl.customview.R;

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

    private float mBaseScaleLength;
    private float mLongScaleLength;
    private float mShortScaleLength;
    private float mBaseScaleTextSize;
    private float mUnitTextSize;
    private float mScaleTextSize;
    private float mPaddingTop;
    private float mPaddingBottom;
    private int mScaleColor;
    private int mBaseScaleColor;

    private float mWidth;
    private float mHeight;
    /**
     * mScaleNum * mScaleSpace = mWidth
     * 这两个属性赋值一个即可
     */
    private int mScaleNum;          //刻度线个数
    private float mScaleSpace;      //刻度线间距
    private int mInitScale;         //初始刻度
    private int mMinScale;          //最小刻度
    private int mMaxScale;          //最大刻度
    private float mScaleGranularity;//大刻度粒度
    private float mFinerGranularity;//小刻度粒度
    private int mScaleCount;        //两条大刻度线之间的刻度数
    private String mUnit = "";      //单位

    private float mLastTouchX;  //上一次的触摸点

    public SlideTapeView(Context context) {
        super(context);
        initAttributes();
        init();
    }

    public SlideTapeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttributes(attrs);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        mScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBaseScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScalePaint.setColor(mScaleColor);
        mScalePaint.setStrokeWidth(2);
        mTextPaint.setTextSize(mScaleTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mBaseScalePaint.setColor(mBaseScaleColor);
        mBaseScalePaint.setStrokeWidth(10);
        mBaseScalePaint.setTextAlign(Paint.Align.CENTER);
    }

    private void initAttributes() {
        mScaleNum = 30;
        mScaleSpace = 0;
        mInitScale = 0;
        mMinScale = -100;
        mMaxScale = 100;
        mScaleGranularity = 1;
        mFinerGranularity = 0.1f;
        mBaseScaleLength = dp2px(56);
        mLongScaleLength = dp2px(50);
        mShortScaleLength = dp2px(25);
        mBaseScaleTextSize = dp2px(32);
        mUnitTextSize = dp2px(16);
        mScaleTextSize = dp2px(12);
        mPaddingTop = dp2px(24);
        mPaddingBottom = dp2px(20);
        mScaleColor = Color.argb(0xff, 0xe4, 0xe4, 0xe4);
        mBaseScaleColor = Color.argb(0xff, 0x4f, 0xba, 0x77);
        adjustAttrs();
    }

    private void initAttributes(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.SlideTapeView);
        mScaleNum = array.getInteger(R.styleable.SlideTapeView_scale_num, 30);
        mScaleSpace = array.getDimension(R.styleable.SlideTapeView_scale_space, 0);
        mInitScale = array.getInteger(R.styleable.SlideTapeView_init_scale, 0);
        mMinScale = array.getInteger(R.styleable.SlideTapeView_min_scale, -100);
        mMaxScale = array.getInteger(R.styleable.SlideTapeView_max_scale, 100);
        mScaleGranularity = array.getFloat(R.styleable.SlideTapeView_long_granularity, 1);
        mFinerGranularity = array.getFloat(R.styleable.SlideTapeView_short_granularity, 0.1f);
        if(array.getString(R.styleable.SlideTapeView_unit) != null) {
            mUnit = array.getString(R.styleable.SlideTapeView_unit);
        }
        mBaseScaleLength = array.getDimension(R.styleable.SlideTapeView_base_scale_length, dp2px(56));
        mLongScaleLength = array.getDimension(R.styleable.SlideTapeView_long_scale_length, dp2px(50));
        mShortScaleLength = array.getDimension(R.styleable.SlideTapeView_short_scale_length, dp2px(25));
        mBaseScaleTextSize = array.getDimension(R.styleable.SlideTapeView_base_text_size, dp2px(32));
        mUnitTextSize = array.getDimension(R.styleable.SlideTapeView_unit_text_size, dp2px(16));
        mScaleTextSize = array.getDimension(R.styleable.SlideTapeView_scale_text_size, dp2px(12));
        mPaddingTop = array.getDimension(R.styleable.SlideTapeView_padding_top, dp2px(24));
        mPaddingBottom = array.getDimension(R.styleable.SlideTapeView_padding_bottom, dp2px(20));
        mScaleColor = array.getColor(R.styleable.SlideTapeView_scale_color, Color.argb(0xff, 0xe4, 0xe4, 0xe4));
        mBaseScaleColor = array.getColor(R.styleable.SlideTapeView_base_scale_color, Color.argb(0xff, 0x4f, 0xba, 0x77));
        array.recycle();
        adjustAttrs();
    }

    public int dp2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 调整属性值到合法值
     */
    private void adjustAttrs() {
        if(mMinScale > mMaxScale) {
            mMinScale = -100;
            mMaxScale = 100;
        }
        if(mInitScale < mMinScale) {
            mInitScale = mMinScale;
        }
        if(mInitScale > mMaxScale) {
            mInitScale = mMaxScale;
        }
        mScaleCount = (int) (mScaleGranularity / mFinerGranularity);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        if(mScaleSpace == 0) {
            mScaleSpace = mWidth / mScaleNum;
        } else {
            mScaleNum = (int) (mWidth / mScaleSpace);
        }
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
                    canvas.drawLine(scaleX, 0, scaleX, mLongScaleLength, mScalePaint);
                    canvas.drawText(scaleIndex / reciprocal + "", scaleX, mLongScaleLength + mPaddingBottom, mTextPaint);
                } else {
                    //短刻度线
                    mScalePaint.setStrokeWidth(4);
                    canvas.drawLine(scaleX, 0, scaleX, mShortScaleLength, mScalePaint);
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
        canvas.drawLine(mWidth / 2 + translocation, 0, mWidth / 2 + translocation, mBaseScaleLength, mBaseScalePaint);
        //选中文字
        Rect rect = new Rect();
        float reciprocal = 1 / mFinerGranularity;
        String scaleText = (mInitScale + (int) (translocation / mScaleSpace)) / reciprocal + "";
        mBaseScalePaint.setTextSize(mBaseScaleTextSize);
        mBaseScalePaint.setTextAlign(Paint.Align.CENTER);
        mBaseScalePaint.getTextBounds(scaleText, 0, scaleText.length(), rect);
        canvas.drawText(scaleText, mWidth / 2 + translocation, -mPaddingTop, mBaseScalePaint);
        //单位
        mBaseScalePaint.setTextSize(mUnitTextSize);
        mBaseScalePaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(mUnit, (mWidth + rect.right + rect.left) / 2 + translocation, rect.top - mPaddingTop, mBaseScalePaint);
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
