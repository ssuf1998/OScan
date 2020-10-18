package indi.ssuf1998.oscan;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RectItemDecoration extends RecyclerView.ItemDecoration {
    private final Rect rect;

    @Override
    public void getItemOffsets(@NonNull Rect outRect,
                               @NonNull View view,
                               @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.left = rect.left;
        outRect.right = rect.right;
        outRect.bottom = rect.bottom;
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = rect.top;
        }

    }

    public RectItemDecoration(Rect rect) {
        this.rect = rect;
    }
}
