package indi.ssuf1998.itempicker;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ItemPicker extends RecyclerView {
    private int currentIdx = 0;
    private float scrollBackThreshold = 0.75f;
    private int scrollXOffset = 0;
    private CurrentIdxChangedListener mCurrentIdxChangedListener;

    public ItemPicker(@NonNull Context context) {
        this(context, null, 0);
    }

    public ItemPicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemPicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (getAdapter() == null || getLayoutManager() == null) {
            super.onScrollStateChanged(state);
            return;
        }

        if (state == SCROLL_STATE_IDLE) {
            final int wrapWidth = getAdapter().getItemWidth() + getAdapter().getAroundMarginPx() * 2;
            final int relativeOffset = scrollXOffset % wrapWidth;


            if (relativeOffset >= wrapWidth - wrapWidth / 2f * scrollBackThreshold &&
                    relativeOffset < wrapWidth) {
                // 右边会自动滑出的部分
                smoothScrollBy(wrapWidth - relativeOffset, 0);
            } else if (relativeOffset > 0 &&
                    relativeOffset <= wrapWidth / 2f * scrollBackThreshold) {
                // 左边会自动滑出的部分
                smoothScrollBy(-relativeOffset, 0);
            } else if (relativeOffset >= wrapWidth / 2f &&
                    relativeOffset < wrapWidth - wrapWidth / 2f * scrollBackThreshold) {
                // 右边回滚的部分
                smoothScrollBy(-relativeOffset, 0);
            } else if (relativeOffset > wrapWidth / 2f * scrollBackThreshold &&
                    relativeOffset <= wrapWidth / 2f) {
                // 左边回滚的部分
                smoothScrollBy(wrapWidth - relativeOffset, 0);
            }

            if (scrollXOffset % wrapWidth == 0) {
                final int newIdx = scrollXOffset / wrapWidth;
                if (newIdx != currentIdx) {
                    if (mCurrentIdxChangedListener!=null)
                        mCurrentIdxChangedListener.changed(currentIdx,
                                newIdx);
                    currentIdx = newIdx;
                }
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

    @Nullable
    @Override
    public ItemPickerAdapter getAdapter() {
        return (ItemPickerAdapter) super.getAdapter();
    }

    public int getCurrentIdx() {
        return currentIdx;
    }

    public void setCurrentIdx(int idx) {
        post(() -> {
            if (getAdapter() == null) {
                this.currentIdx = 0;
                return;
            }

            final int newIdx = Math.max(Math.min(idx, getAdapter().getItemCount() - 1), 0);
            if (this.currentIdx != newIdx) {
                if (mCurrentIdxChangedListener!=null)
                    mCurrentIdxChangedListener.changed(this.currentIdx, newIdx);
                this.currentIdx = newIdx;

                final int wrapWidth = getAdapter().getItemWidth() + getAdapter().getAroundMarginPx() * 2;
                scrollBy(newIdx * wrapWidth - scrollXOffset,
                        0);
            }
        });
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
