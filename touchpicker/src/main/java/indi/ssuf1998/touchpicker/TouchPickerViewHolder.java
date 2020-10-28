package indi.ssuf1998.touchpicker;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class TouchPickerViewHolder extends RecyclerView.ViewHolder {

    public TextView attrText;
    public TextView attrValueText;

    public TouchPickerViewHolder(@NonNull View itemView) {
        super(itemView);

        attrText = itemView.findViewById(R.id.attrText);
        attrValueText = itemView.findViewById(R.id.attrValueText);
    }
}
