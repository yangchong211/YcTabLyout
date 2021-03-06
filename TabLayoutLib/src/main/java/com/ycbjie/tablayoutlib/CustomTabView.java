/*
Copyright 2017 yangchong211（github.com/yangchong211）

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


package com.ycbjie.tablayoutlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * @author yc
 */
public class CustomTabView extends View {

    private int mTextStartX;
    private int mTextStartY;

    private int mDirection = DIRECTION_LEFT;
    private static final int DIRECTION_LEFT = 0;
    private static final int DIRECTION_RIGHT = 1;
    private static final int DIRECTION_TOP = 2;
    private static final int DIRECTION_BOTTOM = 3;
    private String mText = "";
    private Paint mPaint;
    private int mTextSize = sp2px(30);
    private int mTextOriginColor = 0xff000000;
    private int mTextChangeColor = 0xffff0000;
    private Rect mTextBound = new Rect();
    private int mTextWidth;
    private int mTextHeight;
    private float mProgress;
    private boolean debug = false;
    private static final String KEY_STATE_PROGRESS = "key_progress";
    private static final String KEY_DEFAULT_STATE = "key_default_state";

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putFloat(KEY_STATE_PROGRESS, mProgress);
        bundle.putParcelable(KEY_DEFAULT_STATE, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mProgress = bundle.getFloat(KEY_STATE_PROGRESS);
            super.onRestoreInstanceState(bundle.getParcelable(KEY_DEFAULT_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public CustomTabView(Context context) {
        this(context, null);
    }

    public CustomTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomTabView);
        mText = ta.getString(R.styleable.CustomTabView_ctvText);
        mTextSize = ta.getDimensionPixelSize(R.styleable.CustomTabView_ctvTextSize, mTextSize);
        mTextOriginColor = ta.getColor(R.styleable.CustomTabView_ctvTextOriginColor, mTextOriginColor);
        mTextChangeColor = ta.getColor(R.styleable.CustomTabView_ctvTextChangeColor, mTextChangeColor);
        mProgress = ta.getFloat(R.styleable.CustomTabView_ctvProgress, 0);
        mDirection = ta.getInt(R.styleable.CustomTabView_ctvDirection, mDirection);
        ta.recycle();
        mPaint.setTextSize(mTextSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureText();
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
        mTextStartX = getMeasuredWidth() / 2 - mTextWidth / 2;
        mTextStartY = getMeasuredHeight() / 2 - mTextHeight / 2;
    }

    private int measureHeight(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int val = MeasureSpec.getSize(measureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                result = val;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                result = mTextBound.height();
                result += getPaddingTop() + getPaddingBottom();
                break;
            default:
                break;
        }
        result = mode == MeasureSpec.AT_MOST ? Math.min(result, val) : result;
        return result;
    }

    private int measureWidth(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int val = MeasureSpec.getSize(measureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                result = val;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                result = mTextWidth;
                result += getPaddingLeft() + getPaddingRight();
                break;
            default:
                break;
        }
        result = mode == MeasureSpec.AT_MOST ? Math.min(result, val) : result;
        return result;
    }

    private void measureText() {
        mTextWidth = (int) mPaint.measureText(mText);
        FontMetrics fm = mPaint.getFontMetrics();
        mTextHeight = (int) Math.ceil(fm.descent - fm.top);
        mPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
        mTextHeight = mTextBound.height();
    }

    public void reverseColor() {
        int tmp = mTextOriginColor;
        mTextOriginColor = mTextChangeColor;
        mTextChangeColor = tmp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDirection == DIRECTION_LEFT) {
            drawChangeLeft(canvas);
            drawOriginLeft(canvas);
        } else if (mDirection == DIRECTION_RIGHT) {
            drawOriginRight(canvas);
            drawChangeRight(canvas);
        } else if (mDirection == DIRECTION_TOP) {
            drawOriginTop(canvas);
            drawChangeTop(canvas);
        } else if (mDirection == DIRECTION_BOTTOM){
            drawOriginBottom(canvas);
            drawChangeBottom(canvas);
        }
    }

    /**
     * 横向
     * @param canvas                    画板
     * @param color                     颜色
     * @param startX                    开始x
     * @param endX                      结束x
     */
    private void drawTextHor(Canvas canvas, int color, int startX, int endX) {
        mPaint.setColor(color);
        if (debug) {
            mPaint.setStyle(Style.STROKE);
            canvas.drawRect(startX, 0, endX, getMeasuredHeight(), mPaint);
        }
        canvas.save();
        canvas.clipRect(startX, 0, endX, getMeasuredHeight());
        // right, bottom
        canvas.drawText(mText, mTextStartX, getMeasuredHeight() / 2
                        - ((mPaint.descent() + mPaint.ascent()) / 2), mPaint);
        canvas.restore();
    }


    /**
     * 横向
     * @param canvas                    画板
     * @param color                     颜色
     * @param startY                    开始y
     * @param endY                      结束y
     */
    private void drawTextVer(Canvas canvas, int color, int startY, int endY) {
        mPaint.setColor(color);
        if (debug) {
            mPaint.setStyle(Style.STROKE);
            canvas.drawRect(0, startY, getMeasuredWidth(), endY, mPaint);
        }
        canvas.save();
        canvas.clipRect(0, startY, getMeasuredWidth(), endY);
        canvas.drawText(mText, mTextStartX, getMeasuredHeight() / 2
                        - ((mPaint.descent() + mPaint.ascent()) / 2), mPaint);
        canvas.restore();
    }


    public void setDirection(int direction) {
        mDirection = direction;
    }

    private void drawChangeLeft(Canvas canvas) {
        drawTextHor(canvas, mTextChangeColor, mTextStartX,
                (int) (mTextStartX + mProgress * mTextWidth));
    }

    private void drawOriginLeft(Canvas canvas) {
        drawTextHor(canvas, mTextOriginColor, (int) (mTextStartX + mProgress
                * mTextWidth), mTextStartX + mTextWidth);
    }

    private void drawChangeRight(Canvas canvas) {
        drawTextHor(canvas, mTextChangeColor,
                (int) (mTextStartX + (1 - mProgress) * mTextWidth), mTextStartX + mTextWidth);
    }

    private void drawOriginRight(Canvas canvas) {
        drawTextHor(canvas, mTextOriginColor, mTextStartX,
                (int) (mTextStartX + (1 - mProgress) * mTextWidth));
    }

    private void drawChangeTop(Canvas canvas) {
        drawTextVer(canvas, mTextChangeColor, mTextStartY,
                (int) (mTextStartY + mProgress * mTextHeight));
    }

    private void drawOriginTop(Canvas canvas) {
        drawTextVer(canvas, mTextOriginColor, (int) (mTextStartY + mProgress
                * mTextHeight), mTextStartY + mTextHeight);
    }

    private void drawChangeBottom(Canvas canvas) {
        drawTextVer(canvas, mTextChangeColor,
                (int) (mTextStartY + (1 - mProgress) * mTextHeight), mTextStartY + mTextHeight);
    }

    private void drawOriginBottom(Canvas canvas) {
        drawTextVer(canvas, mTextOriginColor, mTextStartY,
                (int) (mTextStartY + (1 - mProgress) * mTextHeight));
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        invalidate();
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int mTextSize) {
        this.mTextSize = mTextSize;
        mPaint.setTextSize(mTextSize);
        requestLayout();
        invalidate();
    }

    public void setText(String text) {
        this.mText = text;
        requestLayout();
        invalidate();
    }
    
    public void setTextOriginColor(int mTextOriginColor) {
        this.mTextOriginColor = mTextOriginColor;
        invalidate();
    }
    
    public void setTextChangeColor(int mTextChangeColor) {
        this.mTextChangeColor = mTextChangeColor;
        invalidate();
    }

    private int sp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                dpVal, getResources().getDisplayMetrics());
    }

}
