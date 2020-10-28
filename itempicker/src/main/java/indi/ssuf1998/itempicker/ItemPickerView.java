package indi.ssuf1998.itempicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

public class ItemPickerView extends RelativeLayout {
    // 不支持大小不一样的item
    // 或者说大小不一样，就会很难看……
    // 这就很suck了
    private final ItemPicker wheel;
    private final ImageView mask;
    private int maskColor = 0xFFFFFFFF;
    private float maskRatio;

    public ItemPickerView(Context context) {
        this(context, null, 0, 0);
    }

    public ItemPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public ItemPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ItemPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.item_picker_view_layout, this, true);

        wheel = findViewById(R.id.wheel);
        final LinearLayoutManager mgr = new LinearLayoutManager(context);
        mgr.setOrientation(LinearLayoutManager.HORIZONTAL);
        wheel.setLayoutManager(mgr);

        mask = findViewById(R.id.mask);
        mask.setClickable(false);

        post(() -> {
            final ItemPickerAdapter adapter = wheel.getAdapter();
            maskRatio = adapter == null ? 0.5f : adapter.getExpand() / 2f;
            drawMask(maskColor, maskRatio);
        });
    }

    public void setAdapter(ItemPickerAdapter adapter) {
        wheel.setAdapter(adapter);
    }

    public void setOnCurrentIdxChangedListener(CurrentIdxChangedListener mCurrentIdxChangedListener) {
        wheel.setOnCurrentIdxChangedListener(mCurrentIdxChangedListener);
    }

    public void setScrollBackThreshold(float scrollBackThreshold) {
        wheel.setScrollBackThreshold(scrollBackThreshold);
    }

    public int getCurrentIdx() {
        return wheel.getCurrentIdx();
    }

    public void setCurrentIdx(int currentIdx) {
        wheel.setCurrentIdx(currentIdx);
    }

    public float getScrollBackThreshold() {
        return wheel.getScrollBackThreshold();
    }

    public int getMaskColor() {
        return maskColor;
    }

    public void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    public float getMaskRatio() {
        return maskRatio;
    }

    public void setMaskRatio(float maskRatio) {
        this.maskRatio = maskRatio;
    }

    private void drawMask(int color, float ratio) {
        if (getWidth() == 0 || getHeight() == 0)
            return;

        final LinearGradient gd = new LinearGradient(0, 0, getWidth(), 0,
                new int[]{color, Color.TRANSPARENT, Color.TRANSPARENT, color},
                new float[]{0, ratio, 1 - ratio, 1},
                Shader.TileMode.REPEAT);

        final Bitmap bmp = Bitmap.createBitmap(
                getMeasuredWidth(),
                getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setShader(gd);
        canvas.drawRect(0, 0, getWidth(), getHeight(), p);
        mask.setImageBitmap(bmp);
    }

}
