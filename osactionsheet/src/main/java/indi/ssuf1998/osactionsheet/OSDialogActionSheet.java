package indi.ssuf1998.osactionsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import indi.ssuf1998.osactionsheet.databinding.DialogActionSheetLayoutBinding;

public class OSDialogActionSheet extends OSActionSheet {
    private DialogActionSheetLayoutBinding binding;

    private int dialogMode;
    private String confirmBtnText;
    private String cancelBtnText;
    private OnClickListener mOnClickListener;

    public OSDialogActionSheet(String OSASTitle, String OSASSubTitle,
                               int dialogMode, String confirmBtnText, String cancelBtnText) {
        super(OSASTitle, OSASSubTitle);
        this.dialogMode = dialogMode;
        this.cancelBtnText = confirmBtnText;
        this.confirmBtnText = cancelBtnText;
    }

    public OSDialogActionSheet(String OSASTitle, int dialogMode) {
        this(OSASTitle, "", dialogMode, "", "");
    }

    public OSDialogActionSheet(String OSASTitle) {
        this(OSASTitle, "", Const.DIALOG_CONFIRM_MODE, "", "");
    }

    public OSDialogActionSheet(String OSASTitle, String confirmBtnText, String cancelBtnText) {
        this(OSASTitle, "", Const.DIALOG_CONFIRM_MODE, confirmBtnText, cancelBtnText);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View fatherView = super.onCreateView(inflater, container, savedInstanceState);
        assert fatherView != null;
        final LinearLayout slotView = (LinearLayout) fatherView.findViewById(R.id.slotView);
        binding = DialogActionSheetLayoutBinding.inflate(inflater, slotView, true);

        return fatherView;
    }

    @Override
    public void onStart() {
        super.onStart();
        initUI();
        initListeners();
    }

    private void initUI() {
        if (dialogMode == Const.DIALOG_CONFIRM_MODE) {
            binding.cancelBtn.setVisibility(View.GONE);
        } else if (dialogMode == Const.DIALOG_CONFIRM_CANCEL_MODE) {
            binding.cancelBtn.setVisibility(View.VISIBLE);
        }

        if (!confirmBtnText.isEmpty()) {
            binding.confirmBtn.setText(confirmBtnText);
        }

        if (!cancelBtnText.isEmpty()) {
            binding.confirmBtn.setText(cancelBtnText);
        }
    }

    private void initListeners() {
        binding.confirmBtn.setOnClickListener(view -> {
            mOnClickListener.onClick(Const.DIALOG_CONFIRM_BTN);
            OSDialogActionSheet.this.dismiss();
        });

        binding.cancelBtn.setOnClickListener(view -> {
            mOnClickListener.onClick(Const.DIALOG_CANCEL_BTN);
            OSDialogActionSheet.this.dismiss();
        });
    }

    public int getDialogMode() {
        return dialogMode;
    }

    public void setDialogMode(int dialogMode) {
        this.dialogMode = dialogMode;
    }

    public String getConfirmBtnText() {
        return confirmBtnText;
    }

    public void setConfirmBtnText(String confirmBtnText) {
        this.confirmBtnText = confirmBtnText;
    }

    public String getCancelBtnText() {
        return cancelBtnText;
    }

    public void setCancelBtnText(String cancelBtnText) {
        this.cancelBtnText = cancelBtnText;
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mOnClickListener = listener;
    }

    public static class Const {
        public static int DIALOG_CONFIRM_MODE = 10;
        public static int DIALOG_CONFIRM_CANCEL_MODE = 11;
        public static int DIALOG_CONFIRM_BTN = 20;
        public static int DIALOG_CANCEL_BTN = 21;
    }

    public interface OnClickListener {
        void onClick(int btn);
    }
}

