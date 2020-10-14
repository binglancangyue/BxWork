package com.bixin.bxfm.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class ScaleView extends View {

    private static final int ITEM_MIN_HEIGHT = 18;
    private float mDensity;
    private int mWidth, mHeight, mLineDivider = 10;

    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDensity = getContext().getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        mWidth = getWidth();
        mHeight = getHeight();
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawScaleLine(canvas);
    }

    private void drawScaleLine(Canvas canvas) {
        canvas.save();
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(2);
        linePaint.setColor(Color.rgb(0xe0, 0xe0, 0xe0));

        int width = mWidth, drawCount = 0;
        float xPosition = 0;
        float xOver_a, xOver_b;

        xPosition = width / 2;
        linePaint.setStrokeWidth(2);
        canvas.drawLine(xPosition, getPaddingTop(), xPosition,
                mHeight-getPaddingBottom(), linePaint);

        int i = 0;
        while(true)
        {
            i++;
            xOver_a = width / 2 + i * mLineDivider * mDensity;
            xOver_b = width / 2 - i * mLineDivider * mDensity;
            if((xOver_a + getPaddingRight() > mWidth) && (xOver_b - getPaddingLeft() < 0))
                break;
            xPosition = width / 2 + i * mLineDivider * mDensity;
            if (xPosition + getPaddingRight() <= mWidth) {
                if(i%5 == 0){
                    linePaint.setStrokeWidth(2);
                    canvas.drawLine(xPosition, getPaddingTop(), xPosition,
                            mHeight-getPaddingBottom(), linePaint);
                }
                else{
                    linePaint.setStrokeWidth(1);
                    canvas.drawLine(xPosition, getPaddingTop()+mDensity * ITEM_MIN_HEIGHT, xPosition,
                            mHeight-getPaddingBottom()-mDensity * ITEM_MIN_HEIGHT, linePaint);
                }
            }
            xPosition = width / 2 - i * mLineDivider * mDensity;
            if(xOver_b - getPaddingLeft() >= 0){
                if(i%5 == 0){
                    linePaint.setStrokeWidth(2);
                    canvas.drawLine(xPosition, getPaddingTop(), xPosition,
                            mHeight-getPaddingBottom(), linePaint);
                }
                else{
                    linePaint.setStrokeWidth(1);
                    canvas.drawLine(xPosition, getPaddingTop()+mDensity * ITEM_MIN_HEIGHT, xPosition,
                            mHeight-getPaddingBottom()-mDensity * ITEM_MIN_HEIGHT, linePaint);
                }
            }
        }
        canvas.restore();
    }
}
