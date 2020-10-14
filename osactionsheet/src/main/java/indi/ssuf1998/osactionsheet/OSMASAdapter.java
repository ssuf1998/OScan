package indi.ssuf1998.osactionsheet;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import indi.ssuf1998.osactionsheet.databinding.MenuActionSheetItemLayoutBinding;

public class OSMASAdapter extends RecyclerView.Adapter<OSMASAdapter.VH> {
    private List<OSMASItem> items;
    private OnItemClickListener mOnItemClickListener;

    public OSMASAdapter(List<OSMASItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        MenuActionSheetItemLayoutBinding binding = MenuActionSheetItemLayoutBinding.inflate(
                inflater, parent, false);

        VH vh = new VH(binding);

        if (mOnItemClickListener != null) {
            vh.itemView.setOnClickListener(view -> mOnItemClickListener.onItemClick(view));
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final OSMASItem item = items.get(position);
        if (item.getItemIcon() != null) {
            holder.getBinding().OSMASItemIcon.setImageDrawable(item.getItemIcon());
        } else {
            holder.getBinding().OSMASItemIcon.setVisibility(View.GONE);
        }
        holder.getBinding().OSMASItemText.setText(item.getItemText());
        if (item.getItemTextColor() != 0) {
            holder.getBinding().OSMASItemText.setTextColor(item.getItemTextColor());
            holder.getBinding().OSMASItemIcon.setImageTintList(
                    ColorStateList.valueOf(item.getItemTextColor()));
        }
        holder.getBinding().OSMASItemText.setTypeface(
                holder.getBinding().OSMASItemText.getTypeface(),
                item.getTypefaceStyle()
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public static class VH extends RecyclerView.ViewHolder {
        private MenuActionSheetItemLayoutBinding binding;

        public MenuActionSheetItemLayoutBinding getBinding() {
            return binding;
        }

        public VH(@NonNull MenuActionSheetItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view);
    }
}
