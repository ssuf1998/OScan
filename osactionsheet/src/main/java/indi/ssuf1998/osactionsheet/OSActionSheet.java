package indi.ssuf1998.osactionsheet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import indi.ssuf1998.osactionsheet.databinding.ActionSheetLayoutBinding;

public class OSActionSheet extends BottomSheetDialogFragment {
    private ActionSheetLayoutBinding binding;

    private String OSASTitle;
    private List<OSASItem> items;
    private boolean lightNavBarMode;
    private OnItemClickListener mOnItemClickListener;

    private int originPaddingBottom;

    public OSActionSheet(String OSASTitle, List<OSASItem> items) {
        this.items = items;
        this.OSASTitle = OSASTitle.toUpperCase();
    }

    public OSActionSheet(List<OSASItem> items) {
        this("OSASTitle", items);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ActionSheetLayoutBinding.inflate(inflater);
        initUI();
        initListeners();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Dialog dialog = getDialog();
            Window win = dialog.getWindow();

            if (lightNavBarMode &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                if (originPaddingBottom == binding.OSASView.getPaddingBottom()) {
                    binding.OSASView.setPadding(
                            binding.OSASView.getPaddingLeft(),
                            binding.OSASView.getPaddingTop(),
                            binding.OSASView.getPaddingRight(),
                            getNavBarHeightInPixel() + binding.OSASView.getPaddingBottom());

                    win.findViewById(com.google.android.material.R.id.container).setFitsSystemWindows(false);
                }
            } else {
                if (originPaddingBottom != binding.OSASView.getPaddingBottom()) {
                    binding.OSASView.setPadding(
                            binding.OSASView.getPaddingLeft(),
                            binding.OSASView.getPaddingTop(),
                            binding.OSASView.getPaddingRight(),
                            originPaddingBottom);

                    win.findViewById(com.google.android.material.R.id.container).setFitsSystemWindows(true);
                }

            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getContext() != null;
        TypedArray attrArray = getContext().obtainStyledAttributes(
                null,
                R.styleable.OSActionSheet);

        int themeId = attrArray.getResourceId(R.styleable.OSActionSheet_OSActionSheetTheme,
                R.style.OSActionSheetDefaultTheme);
        lightNavBarMode = attrArray.getBoolean(R.styleable.OSActionSheet_lightNavBarMode,
                false);

        attrArray.recycle();

        setStyle(STYLE_NORMAL, themeId);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            View designView = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            designView.setBackgroundColor(Color.TRANSPARENT);
        });
        return dialog;
    }

    private void initUI() {
        OSASAdapter adapter = new OSASAdapter(items);
        LinearLayoutManager layoutMgr = new LinearLayoutManager(getContext());

        if (mOnItemClickListener != null) {
            adapter.setOnItemClickListener(view -> {
                int idx = binding.OSASItemsView.getChildAdapterPosition(view);
                mOnItemClickListener.onItemClick(idx);
                OSActionSheet.this.dismiss();
            });
        }

        binding.OSASItemsView.setLayoutManager(layoutMgr);
        binding.OSASItemsView.setAdapter(adapter);

        binding.OSASTitle.setText(OSASTitle);

        originPaddingBottom = binding.OSASView.getPaddingBottom();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListeners() {
        binding.OSASItemsView.setOnTouchListener((view, motionEvent) -> {
            boolean notOnTop = binding.OSASItemsView.canScrollVertically(-1);
            binding.OSASItemsView.requestDisallowInterceptTouchEvent(notOnTop);
            return false;
        });
    }

    private int getNavBarHeightInPixel() {
        if (getContext() != null) {
            Resources res = getContext().getResources();
            int resId = res.getIdentifier(
                    "navigation_bar_height",
                    "dimen",
                    "android");
            if (resId == 0) {
                return 0;
            }
            return res.getDimensionPixelSize(resId);
        } else {
            return 0;
        }
    }

    public String getOSASTitle() {
        return OSASTitle;
    }

    public void setOSASTitle(String OSASTitle) {
        this.OSASTitle = OSASTitle;
        binding.OSASTitle.setText(OSASTitle);
    }

    public List<OSASItem> getItems() {
        return items;
    }

    public boolean isLightNavBarMode() {
        return lightNavBarMode;
    }

    public void setLightNavBarMode(boolean lightNavBarMode) {
        this.lightNavBarMode = lightNavBarMode;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int idx);
    }
}
