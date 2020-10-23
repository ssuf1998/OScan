package indi.ssuf1998.oscan;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import indi.ssuf1998.oscan.adapter.StrItemPickerAdapter;


public class StrItemPicker extends RecyclerView {
    private int currentIdx = 0;
    private float autoScrollThreshold = 0.5f;

    private final ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
    private int scrollXOffset = 0;

    private currentIdxChangedListener mCurrentIdxChangedListener;

    public StrItemPicker(@NonNull Context context) {
        this(context, null, 0);
    }

    public StrItemPicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrItemPicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
//        animator.addUpdateListener(valueAnimator -> {
//            final float v = (float) animator.getAnimatedValue();
//
//
//        });

    }

    @Override
    public void onScrollStateChanged(int state) {
        if (getAdapter() == null || getLayoutManager() == null) {
            super.onScrollStateChanged(state);
            return;
        }

        final StrItemPickerAdapter adapter = (StrItemPickerAdapter) getAdapter();
        final LinearLayoutManager mgr = (LinearLayoutManager) getLayoutManager();
        final int wrapWidth = adapter.getItemWidth() + adapter.getAroundMarginPx() * 2;

        if (state == SCROLL_STATE_IDLE) {
//            final float percent = scrollXOffset / (float) wrapWidth;
//
//
//            if (percent - Math.floor(percent) >= autoScrollThreshold ) {
//                mCurrentIdxChangedListener.changed(
//                        currentIdx += (currentIdx + 1 >= adapter.getItemCount() ? 0 : 1),
//                        currentIdx
//                );
//            } else if (percent - Math.floor(percent) <= 1 - autoScrollThreshold ) {
//                mCurrentIdxChangedListener.changed(
//                        currentIdx -= (currentIdx - 1 < 0 ? 0 : 1),
//                        currentIdx
//                );
//            }
//            mgr.scrollToPositionWithOffset(currentIdx, wrapWidth / 2);

        }
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        scrollXOffset += dx;
    }

    @Override
    public void setLayoutManager(@Nullable LayoutManager mgr) {
        if (!(mgr instanceof LinearLayoutManager))
            throw new IllegalArgumentException("Adapter must be instance of \"LinearLayoutManager\"");
        LinearLayoutManager tmpMgr = (LinearLayoutManager) mgr;

        if (tmpMgr.getOrientation() != LinearLayoutManager.VERTICAL) {
            tmpMgr.setOrientation(LinearLayoutManager.HORIZONTAL);
        }
        super.setLayoutManager(tmpMgr);
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        if (!(adapter instanceof StrItemPickerAdapter))
            throw new IllegalArgumentException(
                    "Adapter must be instance of \"StrItemPickerAdapter\"");
        super.setAdapter(adapter);
    }

    public int getCurrentIdx() {
        return currentIdx;
    }

    public void setCurrentIdx(int currentIdx) {
        if (getAdapter() == null) {
            this.currentIdx = 0;
            return;
        }
        this.currentIdx = Math.max(Math.min(currentIdx, getAdapter().getItemCount() - 1), 0);
    }

    public float getAutoScrollThreshold() {
        return autoScrollThreshold;
    }

    public void setAutoScrollThreshold(float autoScrollThreshold) {
        this.autoScrollThreshold = Math.max(Math.min(autoScrollThreshold, 1), 0);

    }

    public void setOnCurrentIdxChangedListener(@NonNull currentIdxChangedListener listener) {
        mCurrentIdxChangedListener = listener;
    }

    public interface currentIdxChangedListener {
        void changed(int oldIdx, int newIdx);
    }
}
