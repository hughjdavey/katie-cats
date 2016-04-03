package com.hugh.katiecats.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DoubleButton extends View {
    private boolean upPressed = false;
    private boolean downPressed = false;
    private int centerX, centerY;

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    RectF viewRect = new RectF();

    private OnClickListener onClickListener;

    public interface OnClickListener {
        void clickUp();

        void clickDown();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public DoubleButton(Context context) {
        super(context);
        init();
    }

    public DoubleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DoubleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setFocusable(true);
        setClickable(true);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (y < centerY) {
                upPressed = true;
                this.invalidate();
            }
            else {
                downPressed = true;
                this.invalidate();
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (upPressed) {
                if (null != onClickListener) {
                    onClickListener.clickUp();
                }
                upPressed = false;
                this.invalidate();
            }
            if (downPressed) {
                if (null != onClickListener) {
                    onClickListener.clickDown();
                }
                downPressed = false;
                this.invalidate();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        centerX = w / 2;
        centerY = h / 2;
        viewRect = new RectF(0, 0, centerY * 2, centerX * 2);

        paint.setColor(Color.parseColor("#338FC0"));
        canvas.drawArc(viewRect, 0, 180, true, paint);

        paint.setColor(Color.parseColor("#CD5C5C"));
        canvas.drawArc(viewRect, 0, -180, true, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);

        canvas.drawText("GET NEW CATS", centerX, centerY / 1.5f, paint);
        canvas.drawText("VIEW MY CATS", centerX, centerY * 1.5f, paint);
    }
}