package indi.ssuf1998.osactionsheet;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import indi.ssuf1998.osactionsheet.databinding.MenuActionSheetLayoutBinding;

public class OSMenuActionSheet extends OSActionSheet {
    private MenuActionSheetLayoutBinding binding;

    private final List<OSMASItem> items;
    private OnItemClickListener mOnItemClickListener;
    private RecyclerView.ItemDecoration decoration;

    public OSMenuActionSheet(String OSASTitle, String OSASSubTitle, List<OSMASItem> items) {
        super(OSASTitle, OSASSubTitle);
        this.items = items;
    }

    public OSMenuActionSheet(String OSASTitle, List<OSMASItem> items) {
        this(OSASTitle, "", items);
    }

    @Override
    public void onStart() {
        super.onStart();
        initUI();
        initListeners();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View fatherView = super.onCreateView(inflater, container, savedInstanceState);
        assert fatherView != null;
        final LinearLayout slotView = (LinearLayout) fatherView.findViewById(R.id.slotView);
        binding = MenuActionSheetLayoutBinding.inflate(inflater, slotView, true);

        return fatherView;
    }

    private void initUI() {
        final OSMASAdapter adapter = new OSMASAdapter(items);
        final LinearLayoutManager layoutMgr = new LinearLayoutManager(getContext());
        if (decoration != null) {
            binding.OSMASItemsView.addItemDecoration(decoration);
        }
        binding.OSMASItemsView.setLayoutManager(layoutMgr);
        binding.OSMASItemsView.setAdapter(adapter);

        if (mOnItemClickListener != null) {
            adapter.setOnItemClickListener(view -> {
                final int idx = binding.OSMASItemsView.getChildAdapterPosition(view);
                mOnItemClickListener.onItemClick(idx);
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListeners() {
        binding.OSMASItemsView.setOnTouchListener((view, motionEvent) -> {
            final boolean notOnTop = binding.OSMASItemsView.canScrollVertically(-1);
            binding.OSMASItemsView.requestDisallowInterceptTouchEvent(notOnTop);
            return false;
        });
    }

    public List<OSMASItem> getItems() {
        return items;
    }

    public RecyclerView.ItemDecoration getDecoration() {
        return decoration;
    }

    public void setDecoration(RecyclerView.ItemDecoration decoration) {
        this.decoration = decoration;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int idx);
    }
}
