/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import com.huawei.hms.audioeditor.demo.R;

import androidx.annotation.Nullable;

/**
 * View showing spatial orientation
 *
 * @since 2021/7/6
 */
public class SpaceRenderPositionView extends View {

    public static final float MAX_DEST = 5.0f;
    private static final int AREA_COUNT = 4;
    private static final int NO_RES_ID = -1;
    private static final int FRONT_TYPE = 1;
    private static final int TOP_TYPE = 2;
    private static final int DEFAULT_ACTIVE_COLOR = 0xFF6555E6;
    private static final int DEFAULT_IN_ACTIVE_COLOR = 0x80FFFFFF;
    private final int lineWidth = dpToPx(1.5f);
    private int mCenterBitmapId = NO_RES_ID;
    private int mActiveColor = DEFAULT_ACTIVE_COLOR;
    private Paint mPaint;
    private int mBgLineColor = 0x26FFFFFF;
    private boolean isActive = false;
    private boolean actionable = true;
    private int mViewType = FRONT_TYPE;
    private Bitmap mCenterBitmap;
    private int mCenterBitmapWidth;
    private int mCenterBitmapHeight;
    private int mPointRadius = dpToPx(8);

    private float currentX;
    private float currentY;
    private int viewSize = 0;
    private SpaceRenderPositionView brother;
    private float destZoom = 1.0f;

    private ViewParent parent;

    public SpaceRenderPositionView(Context context) {
        this(context, null);
    }

    public SpaceRenderPositionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpaceRenderPositionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SpaceRenderPositionView);
        mCenterBitmapId = typedArray.getResourceId(R.styleable.SpaceRenderPositionView_srp_center_bitmap, NO_RES_ID);
        mViewType = typedArray.getInt(R.styleable.SpaceRenderPositionView_srp_view_type, FRONT_TYPE);
        mActiveColor = typedArray.getColor(R.styleable.SpaceRenderPositionView_srp_active_color, DEFAULT_ACTIVE_COLOR);
        mPointRadius = typedArray.getDimensionPixelSize(R.styleable.SpaceRenderPositionView_srp_point_radius, dpToPx(8));
        typedArray.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(lineWidth);

        if (mCenterBitmapId > 0) {
            mCenterBitmap = BitmapFactory.decodeResource(getResources(), mCenterBitmapId);
            if (mCenterBitmap != null) {
                mCenterBitmapWidth = mCenterBitmap.getWidth();
                mCenterBitmapHeight = mCenterBitmap.getHeight();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewSize = w;
        float radius = viewSize / 2.0f - mPointRadius - lineWidth;
        destZoom = MAX_DEST / radius;
        if (currentX <= 0 && currentY <= 0) {
            currentX = (float) w / 2;
            currentY = (float) w / 2;
        }
        parent = getParent();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int resolveWidth = resolveSize(widthSize, widthMeasureSpec);
        int resolveHeight = resolveSize(heightSize, heightMeasureSpec);
        int realSize = Math.min(resolveWidth, resolveHeight);
        setMeasuredDimension(realSize, realSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int everySpace = viewSize / AREA_COUNT;
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mBgLineColor);
        for (int i = 1; i < AREA_COUNT; i++) {
            canvas.drawLine(i * everySpace, 0, i * everySpace, viewSize, mPaint);
            canvas.drawLine(0, i * everySpace, viewSize, i * everySpace, mPaint);
        }
        if (isActive) {
            mPaint.setColor(mActiveColor);
        } else {
            mPaint.setColor(DEFAULT_IN_ACTIVE_COLOR);
        }
        int offset = lineWidth / 2;
        canvas.drawLine(0, offset, viewSize, offset, mPaint);
        canvas.drawLine(viewSize - offset, 0, viewSize - offset, viewSize, mPaint);
        canvas.drawLine(viewSize, viewSize - offset, 0, viewSize - offset, mPaint);
        canvas.drawLine(offset, viewSize, offset, 0, mPaint);
        canvas.drawLine(0, AREA_COUNT / 2 * everySpace, viewSize, AREA_COUNT / 2 * everySpace, mPaint);
        canvas.drawLine(AREA_COUNT / 2 * everySpace, 0, AREA_COUNT / 2 * everySpace, viewSize, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        if (mCenterBitmapId > 0 && mCenterBitmap != null) {
            mPaint.setColor(Color.WHITE);
            canvas.drawBitmap(
                    mCenterBitmap,
                    (float) (viewSize - mCenterBitmapWidth) / 2,
                    (float) (viewSize - mCenterBitmapHeight) / 2,
                    mPaint);
        }

        mPaint.setColor(mActiveColor);
        canvas.drawCircle(currentX, currentY, mPointRadius, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (actionable) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (brother != null) {
                        brother.setActionable(false);
                    }
                    isActive = true;
                    currentX = event.getX();
                    currentY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // Working with Nested Slides
                    if(parent != null){
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    currentX = event.getX();
                    currentY = event.getY();
                    limitPointPosition();
                    if (brother != null) {
                        brother.notifyBrotherActionMove(currentX, currentY);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isActive = false;
                    if (brother != null) {
                        brother.setActionable(true);
                    }
                    // Handle whether the position needs to be returned to the origin.
                    currentX = event.getX();
                    currentY = event.getY();
                    handleActionUp();
                    notifyBrotherActionUp();
                    break;
                default:
                    break;
            }
            invalidate();
        }
        return true;
    }

    public void notifyBrotherActionMove(float brotherX, float brotherY) {
        if (!actionable) {
            currentX = brotherX;
            invalidate();
        }
    }

    public void notifyBrotherActionUp() {
        if (brother != null) {
            brother.handleActionUp();
        }
    }

    public void handleActionUp() {
        if (Math.abs(currentX - viewSize / 2.0f) < mPointRadius
                && Math.abs(currentY - viewSize / 2.0f) < mPointRadius) {
            currentX = viewSize / 2.0f;
            currentY = viewSize / 2.0f;
            invalidate();
        } else {
            limitPointPosition();
        }
    }

    private void limitPointPosition() {
        int min = mPointRadius + lineWidth;
        int max = viewSize - mPointRadius - lineWidth;
        if (currentX <= min) {
            currentX = min;
        }
        if (currentX >= max) {
            currentX = max;
        }
        if (currentY <= min) {
            currentY = min;
        }
        if (currentY >= max) {
            currentY = max;
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public void setActionable(boolean actionable) {
        this.actionable = actionable;
    }

    public SpaceRenderPositionView getBrother() {
        return brother;
    }

    public void setBrother(SpaceRenderPositionView brother) {
        this.brother = brother;
    }

    public float getCurrentX() {
        return currentX;
    }

    public float getCurrentY() {
        return currentY;
    }

    public float getOrientationX() {
        return (currentX - viewSize / 2.0f) * destZoom;
    }

    public float getOrientationY() {
        if (mViewType == FRONT_TYPE) {
            return (viewSize / 2.0f - currentY) * destZoom;
        } else {
            if (brother != null) {
                return (viewSize / 2.0f - brother.getCurrentY()) * destZoom;
            } else {
                return 0f;
            }
        }
    }

    public float getOrientationZ() {
        if (mViewType == FRONT_TYPE) {
            if (brother != null) {
                return (viewSize / 2.0f - brother.getCurrentY()) * destZoom;
            } else {
                return 0f;
            }
        } else {
            return (viewSize / 2.0f - currentY) * destZoom;
        }
    }

    public void setPosition(float x, float y, float z) {
        if (destZoom <= 0) {
            destZoom = 1;
        }
        currentX = x / destZoom;
        if (mViewType == FRONT_TYPE) {
            currentY = y / destZoom;
        } else {
            currentY = z / destZoom;
        }
        currentX += viewSize / 2.0f;
        currentY = viewSize / 2.0f - currentY;

        invalidate();
    }
}
