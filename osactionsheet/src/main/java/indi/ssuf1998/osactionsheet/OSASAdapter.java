package indi.ssuf1998.osactionsheet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import indi.ssuf1998.osactionsheet.databinding.ActionSheetItemLayoutBinding;

public class OSASAdapter extends RecyclerView.Adapter<OSASAdapter.VH> {
    private List<OSASItem> items;
    private OnItemClickListener mOnItemClickListener;

    public OSASAdapter(List<OSASItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ActionSheetItemLayoutBinding binding = ActionSheetItemLayoutBinding.inflate(
                inflater, parent, false);

        VH vh = new VH(binding);

        if (mOnItemClickListener != null) {
            vh.itemView.setOnClickListener(view -> mOnItemClickListener.onItemClick(view));
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final OSASItem item = items.get(position);

        holder.getBinding().OSASItemText.setText(item.getItemText());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public static class VH extends RecyclerView.ViewHolder {
        private ActionSheetItemLayoutBinding binding;

        public ActionSheetItemLayoutBinding getBinding() {
            return binding;
        }

        public VH(@NonNull ActionSheetItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view);
    }
}
