package com.lsl.customview.favorite;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.lsl.customview.R;

/**
 * Created by lsl on 2018/05/02
 */

public class FavoriteView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap likeBitmap;
    private Bitmap dislikeBitmap;
    private Bitmap shiningBitmap;

    private int mWidth;
    private int mHeight;

    private ObjectAnimator animator1;
    private ObjectAnimator animator2;
    private ObjectAnimator animator3;
    private ObjectAnimator animator4;
    private ObjectAnimator animator5;
    private ObjectAnimator animator6;
    private AnimatorSet animatorSet = new AnimatorSet();
    private float mDislikeScale = 1;
    private float mLikeScale;
    private float mShiningScale;
    private int mDislikeAlpha = 1;
    private int mLikeAlpha;
    private int mShiningAlpha;

    public FavoriteView(Context context) {
        super(context);
        init();
    }

    public FavoriteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setARGB(0xff, 0xe0, 0x4d, 0x36);
        paint.setStyle(Paint.Style.STROKE);
        likeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_like);
        dislikeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_dislike);
        shiningBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_shining);

        animator1 = ObjectAnimator.ofFloat(this, "dislikeScale", 1, 0.5f, 1);
        animator2 = ObjectAnimator.ofFloat(this, "likeScale", 1, 0.5f, 1);
        animator5 = ObjectAnimator.ofFloat(this, "shiningScale", 1, 0, 1);
        animator3 = ObjectAnimator.ofInt(this, "dislikeAlpha", 1, 0, 0);
        animator4 = ObjectAnimator.ofInt(this, "likeAlpha", 0, 1, 1);
        animator6 = ObjectAnimator.ofInt(this, "shiningAlpha", 0, 1, 1);
        animatorSet.playTogether(animator1, animator2, animator3, animator4, animator5, animator6);
        animatorSet.setInterpolator(new OvershootInterpolator());
    }

    @SuppressWarnings("unused")
    public void setDislikeScale(float scale) {
        this.mDislikeScale = scale;
        invalidate();
    }

    @SuppressWarnings("unused")
    public void setLikeScale(float scale) {
        this.mLikeScale = scale;
        invalidate();
    }

    @SuppressWarnings("unused")
    public void setShiningScale(float scale) {
        this.mShiningScale = scale;
        invalidate();
    }

    @SuppressWarnings("unused")
    public void setDislikeAlpha(int alpha) {
        this.mDislikeAlpha = alpha;
        invalidate();
    }

    @SuppressWarnings("unused")
    public void setLikeAlpha(int alpha) {
        this.mLikeAlpha = alpha;
        invalidate();
    }

    @SuppressWarnings("unused")
    public void setShiningAlpha(int alpha) {
        this.mShiningAlpha = alpha;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int a = dislikeBitmap.getWidth();
        int b = dislikeBitmap.getHeight();
        int c = likeBitmap.getWidth();
        int d = likeBitmap.getHeight();
        int e = shiningBitmap.getWidth();
        int f = shiningBitmap.getHeight();

        canvas.save();
        canvas.translate(mWidth / 2, mHeight / 2);

        if(mDislikeAlpha == 1) {
            canvas.save();
            canvas.scale(mDislikeScale, mDislikeScale);
            canvas.drawBitmap(dislikeBitmap, -dislikeBitmap.getWidth() / 2, -dislikeBitmap.getHeight() / 2, paint);
            canvas.restore();
        }
        if(mLikeAlpha == 1) {
            canvas.save();
            canvas.scale(mLikeScale, mLikeScale);
            canvas.drawBitmap(likeBitmap, -likeBitmap.getWidth() / 2, -likeBitmap.getHeight() / 2, paint);
            canvas.drawBitmap(shiningBitmap, -shiningBitmap.getWidth() / 2, -(likeBitmap.getHeight() + shiningBitmap.getHeight()) / 2, paint);
            canvas.restore();
        }
        if(mShiningAlpha > 0) {
            canvas.save();
            canvas.scale(mShiningScale, mShiningScale);
            canvas.drawCircle(3, -6, 60, paint);
            canvas.restore();
        }
//        canvas.drawRect(-dislikeBitmap.getWidth() / 2, -dislikeBitmap.getHeight() / 2, dislikeBitmap.getWidth() / 2, dislikeBitmap.getHeight() / 2, paint);
//        canvas.drawRect(-shiningBitmap.getWidth() / 2, -(shiningBitmap.getHeight() + likeBitmap.getHeight()) / 2, shiningBitmap.getWidth() / 2, (shiningBitmap.getHeight() - likeBitmap.getHeight()) / 2, paint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                animatorSet.start();
                break;
        }
        return true;
    }
}
