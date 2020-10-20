package indi.ssuf1998.oscan;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import org.opencv.core.Point;

import java.util.Arrays;

public class CropImageView extends AppCompatImageView {
    private Point[] cornerPts;

    private int draggingPtIdx;
    private final Point lastTimePt = new Point();
    private Rect workRect;
    private float scaleX, scaleY;

    private int mStrokeWidth;
    private int cornerPtsColor;
    private int cornerPtsRadius;
    private int edgeColor;
    private int maskAlpha;

    private Paint cornerPtsPaint;
    private Paint edgesPaint;
    private Paint maskPaint;

    public CropImageView(@NonNull Context context) {
        this(context, null);
    }

    public CropImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttrs(context, attrs);

        final int padding = cornerPtsRadius + (int) Math.ceil(mStrokeWidth / 2f);
        this.setPadding(padding,
                padding,
                padding,
                padding);

        initPaint();
    }

    public void setCornerPts(Point[] cornerPts) {
        this.cornerPts = cornerPts;
        invalidate();
    }

    public Point[] getCornerPts() {
        final Point[] ret = Arrays.copyOf(cornerPts, cornerPts.length);
        for (Point p : ret) {
            p.x = (p.x - workRect.left) / scaleX;
            p.y = (p.y - workRect.top) / scaleY;
        }
        return ret;
    }

    private void initPaint() {
        cornerPtsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerPtsPaint.setColor(cornerPtsColor);
        cornerPtsPaint.setStrokeWidth(mStrokeWidth);
        cornerPtsPaint.setStyle(Paint.Style.STROKE);
        cornerPtsPaint.setShadowLayer(5f, 0, 0,
                Color.parseColor("#80000000"));

        edgesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgesPaint.setColor(edgeColor);
        edgesPaint.setStrokeWidth(mStrokeWidth);
        edgesPaint.setStyle(Paint.Style.STROKE);
        edgesPaint.setShadowLayer(5f, 0, 0,
                Color.parseColor("#80000000"));

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(Color.BLACK);
        maskPaint.setStyle(Paint.Style.FILL);
    }

    private void initWorkRect() {
        final Drawable d = getDrawable();
        if (workRect == null && d != null) {
            final float[] matrix = new float[9];
            getImageMatrix().getValues(matrix);
            scaleX = matrix[Matrix.MSCALE_X];
            scaleY = matrix[Matrix.MSCALE_Y];
            final int origW = d.getIntrinsicWidth();
            final int origH = d.getIntrinsicHeight();

            final int scaleW = Math.round(origW * scaleX);
            final int scaleH = Math.round(origH * scaleY);

            workRect = new Rect(
                    (getWidth() - scaleW) / 2,
                    (getHeight() - scaleH) / 2,
                    (getWidth() + scaleW) / 2,
                    (getHeight() + scaleH) / 2
            );

            for (Point p : cornerPts) {
                p.x = (p.x * scaleX) + workRect.left;
                p.y = (p.y * scaleY) + workRect.top;
            }
        }
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView);

        mStrokeWidth = ta.getDimensionPixelSize(R.styleable.CropImageView_strokeWidth, 8);
        cornerPtsColor = ta.getColor(R.styleable.CropImageView_cornerPtsColor, Color.WHITE);
        cornerPtsRadius = ta.getDimensionPixelSize(R.styleable.CropImageView_cornerPtsRadius, 36);
        edgeColor = ta.getColor(R.styleable.CropImageView_edgeColor, Color.WHITE);
        maskAlpha = ta.getInteger(R.styleable.CropImageView_maskAlpha, 96);
        maskAlpha = Math.max(Math.min(maskAlpha, 255), 0);

        ta.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isCornerPtsInvalid())
            return;

        initWorkRect();

        drawCropRect(canvas);
    }

    private void drawCornerPts(Canvas canvas) {
        for (Point p : cornerPts) {
            canvas.drawCircle((float) p.x, (float) p.y,
                    cornerPtsRadius, cornerPtsPaint);
        }
    }

    private void drawCropRect(Canvas canvas) {
        final Path cropPath = new Path();
        cropPath.moveTo((float) cornerPts[0].x, (float) cornerPts[0].y);
        cropPath.lineTo((float) cornerPts[1].x, (float) cornerPts[1].y);
        cropPath.lineTo((float) cornerPts[2].x, (float) cornerPts[2].y);
        cropPath.lineTo((float) cornerPts[3].x, (float) cornerPts[3].y);
        cropPath.close();

        drawMask(canvas, cropPath);
        canvas.drawPath(cropPath, edgesPaint);
        drawCornerPts(canvas);
    }

    private void drawMask(Canvas canvas, Path cropPath) {

        int layerId = canvas.saveLayer(workRect.left, workRect.top, workRect.right, workRect.bottom,
                null, Canvas.ALL_SAVE_FLAG);

        maskPaint.setAlpha(maskAlpha);
        canvas.drawRect(workRect, maskPaint);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        maskPaint.setAlpha(255);
        canvas.drawPath(cropPath, maskPaint);
        maskPaint.setXfermode(null);

        canvas.restoreToCount(layerId);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            draggingPtIdx = getTouchedPtIdx(event);
            if (draggingPtIdx != -1) {
                lastTimePt.x = cornerPts[draggingPtIdx].x;
                lastTimePt.y = cornerPts[draggingPtIdx].y;
            }
        } else if (action == MotionEvent.ACTION_MOVE &&
                draggingPtIdx != -1) {
            final float[] canMoveXY = getMoveValue(event.getX(), event.getY());
            cornerPts[draggingPtIdx].x = canMoveXY[0];
            cornerPts[draggingPtIdx].y = canMoveXY[1];
        } else if (action == MotionEvent.ACTION_UP) {
            if (!isConvex()) {
                cornerPts[draggingPtIdx].x = lastTimePt.x;
                cornerPts[draggingPtIdx].y = lastTimePt.y;
                lastTimePt.x = 0;
                lastTimePt.y = 0;
            }
            performClick();
        }
        invalidate();

        // 如果当前处于有拖动点的状态（draggingPtIdx != -1）
        // 则只关注于本控件，不做事件分发
        // 如果没有拖动点，则这个分发交给父类来决定
        return draggingPtIdx != -1 || super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }


    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        workRect = null;
    }

    private boolean isCornerPtsInvalid() {
        return cornerPts == null ||
                cornerPts.length != 4;
    }

    // https://github.com/pqpo/SmartCropper/blob/master/smartcropperlib/src/main/java/me/pqpo/smartcropperlib/view/CropImageView.java#L348
    private boolean isConvex() {
        return (pointSideLine(cornerPts[0], cornerPts[2], cornerPts[3]) *
                pointSideLine(cornerPts[0], cornerPts[2], cornerPts[1]) < 0 &&
                pointSideLine(cornerPts[3], cornerPts[1], cornerPts[0]) *
                        pointSideLine(cornerPts[3], cornerPts[1], cornerPts[2]) < 0);
    }

    // https://github.com/pqpo/SmartCropper/blob/master/smartcropperlib/src/main/java/me/pqpo/smartcropperlib/view/CropImageView.java#L368
    private double pointSideLine(Point linePt1, Point linePt2, Point Ptx) {
        return (Ptx.x - linePt1.x) * (linePt2.y - linePt1.y) -
                (Ptx.y - linePt1.y) * (linePt2.x - linePt1.x);
    }

    private int getTouchedPtIdx(MotionEvent event) {
        if (isCornerPtsInvalid()) {
            return -1;
        }

        final float x = event.getX();
        final float y = event.getY();

        for (int i = 0; i < cornerPts.length; i++) {

            final double d = Math.sqrt(Math.pow(x - cornerPts[i].x, 2) +
                    Math.pow(y - cornerPts[i].y, 2));
            if (d < cornerPtsRadius) {
                return i;
            }
        }
        return -1;
    }

    private float[] getMoveValue(float x, float y) {

        final int offset = (int) Math.ceil(mStrokeWidth / 2f);
        final float maxX = Math.max(
                Math.max(workRect.left + offset, x),
                workRect.right - offset);
        final float minX = Math.min(
                Math.min(workRect.left + offset, x),
                workRect.right - offset);

        final float maxY = Math.max(Math.max(
                workRect.top + offset, y),
                workRect.bottom - offset);
        final float minY = Math.min(
                Math.min(workRect.top + offset, y),
                workRect.bottom - offset);

        return new float[]{
                workRect.left + x + workRect.right - maxX - minX,
                workRect.top + y + workRect.bottom - maxY - minY,
        };
    }


}
