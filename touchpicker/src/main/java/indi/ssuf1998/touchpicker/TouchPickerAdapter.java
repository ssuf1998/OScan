package indi.ssuf1998.touchpicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TouchPickerAdapter<T extends Number> extends RecyclerView.Adapter<TouchPickerViewHolder> {
    private final List<TouchPickerItem> items;
    private int textAppearance;
    private boolean hideTheFirst = true;

    public TouchPickerAdapter(List<TouchPickerItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public TouchPickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new TouchPickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TouchPickerViewHolder holder, int position) {
        holder.attrText.setText(items.get(position).getAttrName());
        holder.attrText.setTextAppearance(textAppearance);
        holder.attrValueText.setText(String.valueOf(items.get(position).getAttrValue()));
        holder.attrValueText.setTextAppearance(textAppearance);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setTextAppearance(int textAppearance) {
        this.textAppearance = textAppearance;
    }

    public void setHideTheFirst(boolean hideTheFirst) {
        this.hideTheFirst = hideTheFirst;
    }

    public boolean isHideTheFirst() {
        return hideTheFirst;
    }
}
