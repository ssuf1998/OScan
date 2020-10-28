package indi.ssuf1998.osactionsheet;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import indi.ssuf1998.osactionsheet.databinding.ActionSheetBaseLayoutBinding;


public class OSActionSheet extends BottomSheetDialogFragment {
    private ActionSheetBaseLayoutBinding binding;
    private String OSASTitle;
    private String OSASSubTitle;
    private boolean immersiveModeXML;
    private boolean immersiveMode = true;
    private boolean showing = false;

    private int initBottomPadding = 0;

    public OSActionSheet(String OSASTitle, String OSASSubTitle) {
        this.OSASTitle = OSASTitle.toUpperCase();
        this.OSASSubTitle = OSASSubTitle;
    }

    public OSActionSheet(String OSASTitle) {
        this(OSASTitle, "");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        showing = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        showing = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ActionSheetBaseLayoutBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        initUI();
        solveImmersive();
    }

    private void solveImmersive() {
        if (getDialog() != null && getDialog().getWindow() != null) {
            final Dialog dialog = getDialog();
            final Window win = dialog.getWindow();

            if (immersiveMode && immersiveModeXML &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {

                if (initBottomPadding == 0)
                    initBottomPadding = binding.OSASView.getPaddingBottom();

                binding.OSASView.setPadding(
                        binding.OSASView.getPaddingLeft(),
                        binding.OSASView.getPaddingTop(),
                        binding.OSASView.getPaddingRight(),
                        getNavBarHeightInPixel() + initBottomPadding);

                win.findViewById(R.id.container).setFitsSystemWindows(false);

                win.getDecorView().setSystemUiVisibility(
                        win.getDecorView().getWindowSystemUiVisibility() |
                                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                );

            } else {
                win.findViewById(R.id.container).setFitsSystemWindows(true);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyStyle();
    }

    private void applyStyle() {
        assert getContext() != null;

        final TypedArray attrArray = getContext().obtainStyledAttributes(
                null,
                R.styleable.OSAS);
        final int themeId = attrArray.getResourceId(R.styleable.OSASStyle_OSASTheme,
                R.style.OSASDefaultTheme);
        attrArray.recycle();

        final TypedArray styleArray =
                getContext().getTheme().obtainStyledAttributes(themeId, R.styleable.OSAS);
        immersiveModeXML = styleArray.getBoolean(R.styleable.OSAS_immersiveMode, true);
        styleArray.recycle();

        setStyle(STYLE_NORMAL, themeId);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            View designView = dialog.findViewById(R.id.design_bottom_sheet);
            designView.setBackgroundColor(Color.TRANSPARENT);

        });
        return dialog;
    }

    private void initUI() {
        binding.OSASTitle.setText(OSASTitle);
        if (OSASSubTitle.isEmpty()) {
            binding.OSASSubTitle.setVisibility(View.GONE);
        } else {
            binding.OSASSubTitle.setText(OSASSubTitle);
        }
    }

    private int getNavBarHeightInPixel() {
        if (getContext() != null) {
            final Resources res = getContext().getResources();
            final int resId = res.getIdentifier(
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

    public String getOSASSubTitle() {
        return OSASSubTitle;
    }

    public void setOSASSubTitle(String OSASSubTitle) {
        this.OSASSubTitle = OSASSubTitle;
        binding.OSASTitle.setText(OSASSubTitle);
    }

    public boolean isImmersiveMode() {
        return immersiveMode;
    }

    public void setImmersiveMode(boolean immersiveMode) {
        this.immersiveMode = immersiveMode;
    }

    public void setViewIntoSlot(View newView) {
        binding.slotView.addView(newView);
    }

    public View getSlotView() {
        return binding.slotView;
    }

    public boolean isShowing() {
        return showing;
    }
}
