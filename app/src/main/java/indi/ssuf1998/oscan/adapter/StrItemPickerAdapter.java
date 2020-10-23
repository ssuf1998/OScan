package indi.ssuf1998.oscan.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import indi.ssuf1998.oscan.R;


public class StrItemPickerAdapter extends RecyclerView.Adapter<StrItemPickerAdapter.VH> {
    private final String[] strings;
    private final Context context;

    private int aroundMarginPx = 0;
    private float expand = 0.5f;
    private int itemWidth = -1;
    private int expandWidth = -1;

    public StrItemPickerAdapter(Context context, String[] strings) {
        this.strings = strings;
        this.context = context;
    }

    @NonNull
    @Override
    public StrItemPickerAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout layout = new LinearLayout(parent.getContext());
        TextView textView = new TextView(layout.getContext());
        layout.addView(textView);

        if (itemWidth == -1) {
            expandWidth = Math.round(parent.getWidth() / (2 + 1 / expand));
            itemWidth = Math.round(expandWidth * (1 / expand)) - 2 * aroundMarginPx;
        }

        return new VH(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull StrItemPickerAdapter.VH holder, int position) {

        holder.layout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));


        final TextView textView = (TextView) holder.layout.getChildAt(0);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(
                aroundMarginPx + (position == 0 ? expandWidth : 0),
                aroundMarginPx,
                aroundMarginPx + (position == strings.length - 1 ? expandWidth : 0),
                aroundMarginPx
        );

        textView.setLayoutParams(lp);
        textView.setWidth(itemWidth);
        textView.setBackgroundColor(Color.GRAY);
        textView.setGravity(Gravity.CENTER);
        textView.setText(strings[position]);
    }

    @Override
    public int getItemCount() {
        return strings.length;
    }

    public String[] getStrings() {
        return strings;
    }

    public void setExpand(float expand) {
        this.expand = Math.max(Math.min(expand, 1), 0);
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

    public static class VH extends RecyclerView.ViewHolder {
        public final LinearLayout layout;

        public VH(@NonNull View itemView) {
            super(itemView);
            layout = (LinearLayout) itemView;
        }
    }

    private int dp2Px(@NonNull Context context, @Dimension(unit = Dimension.DP) float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * metrics.density);
    }
}
