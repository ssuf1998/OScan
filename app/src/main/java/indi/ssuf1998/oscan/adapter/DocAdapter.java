package indi.ssuf1998.oscan.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import indi.ssuf1998.oscan.databinding.DocLayoutBinding;

public class DocAdapter extends RecyclerView.Adapter<DocAdapter.VH> {

    private final List<Doc> docList;

    public DocAdapter(List<Doc> docList) {
        this.docList = docList;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        DocLayoutBinding binding = DocLayoutBinding.inflate(inflater, parent, false);
        return new VH(binding);
    }

    @Override
    public int getItemCount() {
        return docList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.getBinding().docTitle.setText(docList.get(position).getTitle());

    }

    public static class VH extends RecyclerView.ViewHolder {
        private final DocLayoutBinding binding;

        public DocLayoutBinding getBinding() {
            return binding;
        }

        public VH(@NonNull DocLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
