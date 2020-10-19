package indi.ssuf1998.oscan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import org.opencv.core.Point;

public class CropImageView extends AppCompatImageView {
    private Point[] cornerPts;

    private float globalStrokeWidth;
    private int draggingPtIdx;
    private final Point lastTimePt = new Point();
    private Rect drawableRect;

    private int cornerPtsColor = Color.RED;
    private int cornerPtsRadius = 45;

    private int edgeColor = Color.RED;

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

        initPaint();
    }

    public void setCornerPts(Point[] cornerPts) {
        this.cornerPts = cornerPts;
        invalidate();
    }

    private void initPaint() {
        globalStrokeWidth = cornerPtsRadius / 3f;

        cornerPtsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerPtsPaint.setColor(cornerPtsColor);
        cornerPtsPaint.setStrokeWidth(globalStrokeWidth);
        cornerPtsPaint.setStyle(Paint.Style.STROKE);

        edgesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgesPaint.setColor(edgeColor);
        edgesPaint.setStrokeWidth(globalStrokeWidth);
        edgesPaint.setStyle(Paint.Style.STROKE);

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(Color.BLACK);
        maskPaint.setStyle(Paint.Style.FILL);
    }

    private void initDrawableRect() {
        if (drawableRect == null)
            drawableRect = new Rect(
                    0, 0, getWidth(), getHeight()
            );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isCornerPtsInvalid())
            return;

        initDrawableRect();

        drawCropRect(canvas);
        drawCornerPts(canvas);
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
    }

    private void drawMask(Canvas canvas, Path cropPath) {

        int layerId = canvas.saveLayer(drawableRect.left, drawableRect.top, drawableRect.right, drawableRect.bottom,
                null, Canvas.ALL_SAVE_FLAG);

        maskPaint.setAlpha(127);
        canvas.drawRect(drawableRect, maskPaint);
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
        } else if (action == MotionEvent.ACTION_MOVE &&
                draggingPtIdx != -1) {
            //TODO
            // 关于在拖动阶段就控制选区必须为凸包
            // 可以先判断完了再去复制画，如果不符合就返回上一次滑动的状态
            // 缺点和明显，会使得这个点在两个状态反复横跳，导致界面有时候会狂闪
            // Microsoft的Office移动端用的就是这种方法
            // 而扫描全能王用的是选择完之后再来判断（也很麻烦呀）
            cornerPts[draggingPtIdx].x = event.getX();
            cornerPts[draggingPtIdx].y = event.getY();
        } else if (action == MotionEvent.ACTION_UP) {
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


}
