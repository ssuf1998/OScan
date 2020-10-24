package indi.ssuf1998.itempicker;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

class ItemPickerWheel extends RecyclerView {
    private int currentIdx = 0;
    private float scrollBackThreshold = 0.75f;
    private int scrollXOffset = 0;
    private CurrentIdxChangedListener mCurrentIdxChangedListener;

    public ItemPickerWheel(@NonNull Context context) {
        this(context, null, 0);
    }

    public ItemPickerWheel(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemPickerWheel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (getAdapter() == null || getLayoutManager() == null) {
            super.onScrollStateChanged(state);
            return;
        }

        if (state == SCROLL_STATE_IDLE) {
            final ItemPickerAdapter adapter = (ItemPickerAdapter) getAdapter();
            final int wrapWidth = adapter.getItemWidth() + adapter.getAroundMarginPx() * 2;
            final int overLastPosOffset = scrollXOffset % wrapWidth;

            if (overLastPosOffset >= wrapWidth - wrapWidth / 2f * scrollBackThreshold &&
                    overLastPosOffset < wrapWidth) {
                smoothScrollBy(wrapWidth - overLastPosOffset, 0);
            } else if (overLastPosOffset > 0 &&
                    overLastPosOffset <= wrapWidth / 2f * scrollBackThreshold) {
                smoothScrollBy(-overLastPosOffset, 0);
            } else if (overLastPosOffset >= wrapWidth / 2f &&
                    overLastPosOffset < wrapWidth - wrapWidth / 2f * scrollBackThreshold) {
                smoothScrollBy(-overLastPosOffset, 0);
            } else if (overLastPosOffset > wrapWidth / 2f * scrollBackThreshold &&
                    overLastPosOffset <= wrapWidth / 2f) {
                smoothScrollBy(wrapWidth - overLastPosOffset, 0);
            }

            //TODO
            // 有bug，往左滑动但滑动距离不够时（回滚了），会调用两次这东西
            // 往右滑没事
            final int newIdx = scrollXOffset / wrapWidth;
            if (newIdx != currentIdx) {
                mCurrentIdxChangedListener.changed(currentIdx,
                        newIdx);
                currentIdx = newIdx;
            }
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
        if (!(adapter instanceof ItemPickerAdapter))
            throw new IllegalArgumentException(
                    "Adapter must be instance of \"ItemPickerAdapter\"");
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
        final ItemPickerAdapter adapter = (ItemPickerAdapter) getAdapter();
        final int wrapWidth = adapter.getItemWidth() + adapter.getAroundMarginPx() * 2;
        smoothScrollBy(currentIdx * wrapWidth - scrollXOffset, 0);
    }

    public float getScrollBackThreshold() {
        return scrollBackThreshold;
    }

    public void setScrollBackThreshold(float scrollBackThreshold) {
        // 越接近1，则越容易滚回当前值
        this.scrollBackThreshold = Math.max(Math.min(scrollBackThreshold, 1), 0);
    }

    public void setOnCurrentIdxChangedListener(@NonNull CurrentIdxChangedListener listener) {
        mCurrentIdxChangedListener = listener;
    }
}
