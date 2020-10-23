package indi.ssuf1998.cropimageview;

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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import org.opencv.core.Point;

import java.util.ArrayList;

public class CropImageView extends AppCompatImageView {
    // 属性
    private int mStrokeWidth;
    private int cornerPtsColor;
    private int cornerPtsRadius;
    private int edgeColor;
    private int maskAlpha;
    private int cropShadowRadius;
    private int cropShadowColor;
    private int magnifierRadius;
    private float magnifierScale;
    private int magnifierPosition;
    private int magnifierCrossWidth;
    private int magnifierCrossSize;
    private int magnifierCrossColor;
    private int magnifierShadowRadius;
    private int magnifierShadowColor;

    // 内部
    private Point[] cornerPts;
    private int draggingPtIdx = -1;
    private final Point lastTimePt = new Point();
    private Rect workRect;
    private float scaleX, scaleY;
    private Bitmap magnifierOrigBmp;
    private Bitmap magnifierBmp;
    private Canvas magnifierCanvas;
    private int magnifierCenter;

    // 绘画相关
    private Paint cornerPtsPaint;
    private Paint edgesPaint;
    private Paint maskPaint;
    private Paint magnifierPaint;
    private Paint magnifierOutPaint;

    // 事件
    private BadCornerPtsListener mBadCornerPtsListener;

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
        final Point[] ret = new Point[cornerPts.length];
        for (int i = 0; i < cornerPts.length; i++) {
            ret[i] = new Point((cornerPts[i].x - workRect.left) / scaleX,
                    (cornerPts[i].y - workRect.top) / scaleY);
        }
        return ret;
    }

    private void initPaint() {
        cornerPtsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerPtsPaint.setColor(cornerPtsColor);
        cornerPtsPaint.setStrokeWidth(mStrokeWidth);
        cornerPtsPaint.setStyle(Paint.Style.STROKE);
        cornerPtsPaint.setShadowLayer(cropShadowRadius, 0, 0, cropShadowColor);

        edgesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgesPaint.setColor(edgeColor);
        edgesPaint.setStrokeWidth(mStrokeWidth);
        edgesPaint.setStyle(Paint.Style.STROKE);
        edgesPaint.setShadowLayer(cropShadowRadius, 0, 0, cropShadowColor);

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(Color.BLACK);
        maskPaint.setStyle(Paint.Style.FILL);

        magnifierPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        magnifierPaint.setStyle(Paint.Style.FILL);

        magnifierOutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        magnifierOutPaint.setColor(Color.TRANSPARENT);
        magnifierOutPaint.setStyle(Paint.Style.FILL);
        magnifierOutPaint.setShadowLayer(magnifierShadowRadius, 0, 0, magnifierShadowColor);
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

            if (cornerPtsAnyNull() || notConvex()) {
                cornerPts = new Point[]{
                        new Point(origW / 4f, origH / 4f),
                        new Point(origW / 4f, origH / 4f * 3),
                        new Point(origW / 4f * 3, origH / 4f * 3),
                        new Point(origW / 4f * 3, origH / 4f),
                };
                mBadCornerPtsListener.haveBadCornerPts();
            }

            for (Point p : cornerPts) {
                p.x = (p.x * scaleX) + workRect.left;
                p.y = (p.y * scaleY) + workRect.top;
            }

            magnifierOrigBmp = ((BitmapDrawable) getDrawable()).getBitmap();
            magnifierOrigBmp = Bitmap.createScaledBitmap(magnifierOrigBmp,
                    (int) (scaleW * magnifierScale), (int) (scaleH * magnifierScale),
                    false);

            magnifierBmp = Bitmap.createBitmap(magnifierRadius, magnifierRadius, Bitmap.Config.ARGB_8888);
            magnifierCanvas = new Canvas(magnifierBmp);
        }

    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView);

        mStrokeWidth = ta.getDimensionPixelSize(R.styleable.CropImageView_strokeWidth,
                Const.D_STROKE_WIDTH);
        cornerPtsColor = ta.getColor(R.styleable.CropImageView_cornerPtsColor, Const.D_CORNER_PTS_COLOR);
        cornerPtsRadius = ta.getDimensionPixelSize(R.styleable.CropImageView_cornerPtsRadius,
                Const.D_CORNER_PTS_RADIUS);
        edgeColor = ta.getColor(R.styleable.CropImageView_edgeColor, Const.D_EDGE_COLOR);
        maskAlpha = ta.getInteger(R.styleable.CropImageView_maskAlpha, Const.D_MASK_ALPHA);
        maskAlpha = Math.max(Math.min(maskAlpha, 255), 0);
        cropShadowRadius = ta.getDimensionPixelSize(R.styleable.CropImageView_cropShadowRadius,
                Const.D_CROP_SHADOW_RADIUS);
        cropShadowRadius = Math.min(20, cropShadowRadius);
        cropShadowColor = ta.getColor(R.styleable.CropImageView_cropShadowColor,
                Const.D_CROP_SHADOW_COLOR);
        magnifierRadius = ta.getDimensionPixelSize(R.styleable.CropImageView_magnifierRadius,
                Const.D_MAGNIFIER_RADIUS);
        magnifierScale = ta.getFloat(R.styleable.CropImageView_magnifierScale, Const.D_MAGNIFIER_SCALE);
        magnifierPosition = ta.getInteger(R.styleable.CropImageView_magnifierPosition, Const.D_MAGNIFIER_POSITION);
        magnifierCrossWidth = ta.getInteger(R.styleable.CropImageView_magnifierCrossWidth, Const.D_MAGNIFIER_CROSS_WIDTH);
        magnifierCrossSize = ta.getInteger(R.styleable.CropImageView_magnifierCrossSize, Const.D_MAGNIFIER_CROSS_SIZE);
        magnifierCrossColor = ta.getColor(R.styleable.CropImageView_magnifierCrossColor, Const.D_MAGNIFIER_CROSS_COLOR);
        magnifierShadowRadius = ta.getDimensionPixelSize(R.styleable.CropImageView_magnifierShadowRadius,
                Const.D_MAGNIFIER_SHADOW_RADIUS);
        magnifierShadowRadius = Math.min(10, magnifierShadowRadius);
        magnifierShadowColor = ta.getColor(R.styleable.CropImageView_magnifierShadowColor,
                Const.D_MAGNIFIER_SHADOW_COLOR);

        magnifierCenter = magnifierRadius / 2;

        ta.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isCornerPtsInvalid())
            return;

        initWorkRect();

        drawCropRect(canvas);
        drawMagnifier(canvas);
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

            drawMagnifierContent();

        } else if (action == MotionEvent.ACTION_UP) {
            if (notConvex()) {
                cornerPts[draggingPtIdx].x = lastTimePt.x;
                cornerPts[draggingPtIdx].y = lastTimePt.y;
                lastTimePt.x = 0;
                lastTimePt.y = 0;
            }
            draggingPtIdx = -1;
            magnifierCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
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

    private void drawMagnifier(Canvas canvas) {
        if (draggingPtIdx == -1)
            return;

        final int shadowOffset = magnifierShadowRadius / 2;
        int displayX = magnifierCenter + shadowOffset;
        int displayY = magnifierCenter + shadowOffset;

        if (magnifierPosition == Const.MAGNIFIER_CENTER_TOP) {
            displayX = getWidth() / 2;
        } else if (magnifierPosition == Const.MAGNIFIER_RIGHT_TOP) {
            displayX = getWidth() - magnifierCenter - shadowOffset;
        } else if (magnifierPosition == Const.MAGNIFIER_CENTER) {
            displayX = getWidth() / 2;
            displayY = getHeight() / 2;
        }

        canvas.drawCircle(displayX,
                displayY,
                magnifierCenter,
                magnifierOutPaint);
        canvas.drawBitmap(magnifierBmp,
                displayX - magnifierCenter, displayY - magnifierCenter,
                null);
    }

    private void drawMagnifierContent() {
        final int magnifierX = (int) ((cornerPts[draggingPtIdx].x - workRect.left) * magnifierScale);
        final int magnifierY = (int) ((cornerPts[draggingPtIdx].y - workRect.top) * magnifierScale);
        final int magnifierCrossOffset = magnifierCrossSize / 2;

        magnifierPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        magnifierPaint.setColor(Color.BLACK);
        magnifierPaint.setStrokeWidth(0);
        magnifierCanvas.drawCircle(magnifierCenter, magnifierCenter,
                magnifierCenter, magnifierPaint);

        magnifierPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        magnifierCanvas.drawBitmap(magnifierOrigBmp,
                new Rect(
                        magnifierX - magnifierCenter,
                        magnifierY - magnifierCenter,
                        magnifierX + magnifierCenter,
                        magnifierY + magnifierCenter
                ),
                new Rect(0, 0, magnifierRadius, magnifierRadius), magnifierPaint);

        magnifierPaint.setXfermode(null);
        magnifierPaint.setColor(magnifierCrossColor);
        magnifierPaint.setStrokeWidth(magnifierCrossWidth);
        magnifierCanvas.drawLine(magnifierCenter - magnifierCrossOffset,
                magnifierCenter,
                magnifierCenter + magnifierCrossOffset,
                magnifierCenter,
                magnifierPaint);
        magnifierCanvas.drawLine(magnifierCenter,
                magnifierCenter - magnifierCrossOffset,
                magnifierCenter,
                magnifierCenter + magnifierCrossOffset,
                magnifierPaint);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        workRect = null;
    }

    private boolean isCornerPtsInvalid() {
        return cornerPts == null || cornerPts.length != 4;
    }

    private boolean cornerPtsAnyNull() {
        boolean anyNull = false;
        for (Point p : cornerPts) {
            if (p == null) {
                anyNull = true;
                break;
            }
        }

        return anyNull;
    }

    // https://github.com/pqpo/SmartCropper/blob/master/smartcropperlib/src/main/java/me/pqpo/smartcropperlib/view/CropImageView.java#L348
    private boolean notConvex() {
        return (!(pointSideLine(cornerPts[0], cornerPts[2], cornerPts[3]) *
                pointSideLine(cornerPts[0], cornerPts[2], cornerPts[1]) < 0) ||
                !(pointSideLine(cornerPts[3], cornerPts[1], cornerPts[0]) *
                        pointSideLine(cornerPts[3], cornerPts[1], cornerPts[2]) < 0));
    }

    // https://github.com/pqpo/SmartCropper/blob/master/smartcropperlib/src/main/java/me/pqpo/smartcropperlib/view/CropImageView.java#L368
    private double pointSideLine(Point linePt1, Point linePt2, Point Ptx) {
        return (Ptx.x - linePt1.x) * (linePt2.y - linePt1.y) -
                (Ptx.y - linePt1.y) * (linePt2.x - linePt1.x);
    }

    private int getTouchedPtIdx(MotionEvent event) {
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

    public void setOnBadCornerPtsListener(@NonNull BadCornerPtsListener listener) {
        mBadCornerPtsListener = listener;
    }

    public interface BadCornerPtsListener {
        void haveBadCornerPts();
    }

}
