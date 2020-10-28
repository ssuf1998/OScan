package indi.ssuf1998.itempicker;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemPickerAdapter extends RecyclerView.Adapter<ItemPickerViewHolder> {
    private final List<? extends View> viewList;
    private final Context context;

    private int aroundMarginPx = 0;
    private float expand = 0.5f;
    private int itemWidth = -1;
    private int expandWidth = -1;

    public ItemPickerAdapter(Context context, List<? extends View> views) {
        this.viewList = views;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemPickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout boxLayout = new LinearLayout(parent.getContext());

        if (itemWidth == -1) {
            expandWidth = Math.round(parent.getWidth() / (2 + 1 / expand));
            itemWidth = Math.round(expandWidth * (1 / expand)) - 2 * aroundMarginPx;
        }

        return new ItemPickerViewHolder(boxLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemPickerViewHolder holder, int position) {
        holder.boxLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        final View inner = viewList.get(position);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                itemWidth,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(
                aroundMarginPx + (position == 0 ? expandWidth : 0),
                aroundMarginPx,
                aroundMarginPx + (position == viewList.size() - 1 ? expandWidth : 0),
                aroundMarginPx
        );
        inner.setLayoutParams(lp);

        holder.boxLayout.addView(inner);
    }

    @Override
    public int getItemCount() {
        return viewList.size();
    }

    public void setExpand(float expand) {
        this.expand = Math.max(Math.min(expand, 1), 0);
    }

    public float getExpand() {
        return expand;
    }

    public void setAroundMarginDp(float dp) {
        this.aroundMarginPx = dp2Px(context, dp);
    }

    public int getAroundMarginPx() {
        return aroundMarginPx;
    }

    public int getItemWidth() {
        return itemWidth;
    }

    public List<? extends View> getViewList() {
        return viewList;
    }

    private int dp2Px(@NonNull Context context, @Dimension(unit = Dimension.DP) float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * metrics.density);
    }

}
