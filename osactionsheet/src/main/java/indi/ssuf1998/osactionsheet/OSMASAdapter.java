package indi.ssuf1998.osactionsheet;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import indi.ssuf1998.osactionsheet.databinding.MenuActionSheetItemLayoutBinding;

class OSMASAdapter extends RecyclerView.Adapter<OSMASAdapter.VH> {
    private final List<OSMASItem> items;
    private OnItemClickListener mOnItemClickListener;

    public OSMASAdapter(List<OSMASItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final MenuActionSheetItemLayoutBinding binding = MenuActionSheetItemLayoutBinding.inflate(
                inflater, parent, false);

        final VH vh = new VH(binding);

        if (mOnItemClickListener != null) {
            vh.itemView.setOnClickListener(view -> mOnItemClickListener.onItemClick(view));
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final OSMASItem item = items.get(position);
        if (item.getItemIcon() != null) {
            holder.binding.OSMASItemIcon.setImageDrawable(item.getItemIcon());
        } else {
            holder.binding.OSMASItemIcon.setVisibility(View.GONE);
        }
        holder.binding.OSMASItemText.setText(item.getItemText());
        if (item.getItemTextColor() != 0) {
            holder.binding.OSMASItemText.setTextColor(item.getItemTextColor());
            holder.binding.OSMASItemIcon.setImageTintList(
                    ColorStateList.valueOf(item.getItemTextColor()));
        }
        holder.binding.OSMASItemText.setTypeface(
                holder.binding.OSMASItemText.getTypeface(),
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
        public final MenuActionSheetItemLayoutBinding binding;

        public VH(@NonNull MenuActionSheetItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view);
    }
}
