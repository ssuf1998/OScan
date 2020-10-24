package indi.ssuf1998.itempicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

public class ItemPicker extends RelativeLayout {
    private final ItemPickerWheel wheel;
    private final LinearLayoutManager mgr;
    private final RelativeLayout.LayoutParams lp;

    public ItemPicker(Context context) {
        this(context, null, 0, 0);
    }

    public ItemPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public ItemPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ItemPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        wheel = new ItemPickerWheel(context);
        lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mgr = new LinearLayoutManager(context);
        mgr.setOrientation(LinearLayoutManager.HORIZONTAL);

        post(() -> {
            wheel.setLayoutManager(mgr);
            wheel.setLayoutParams(lp);
            wheel.setOverScrollMode(OVER_SCROLL_NEVER);

            addView(wheel);
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

}
