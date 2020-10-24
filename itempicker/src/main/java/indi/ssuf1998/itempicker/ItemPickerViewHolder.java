package indi.ssuf1998.itempicker;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class ItemPickerViewHolder extends RecyclerView.ViewHolder {
    public final LinearLayout boxLayout;

    public ItemPickerViewHolder(@NonNull View itemView) {
        super(itemView);
        boxLayout = (LinearLayout) itemView;
    }
}