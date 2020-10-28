package indi.ssuf1998.oscan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;
import indi.ssuf1998.itempicker.ItemPickerAdapter;
import indi.ssuf1998.itempicker.ItemPickerHelper;
import indi.ssuf1998.osactionsheet.OSDialogActionSheet;
import indi.ssuf1998.osactionsheet.OSMenuActionSheet;
import indi.ssuf1998.oscan.core.OSCoreHED;
import indi.ssuf1998.oscan.databinding.ProcessActivityLayoutBinding;
import indi.ssuf1998.touchpicker.TouchPickerItem;

public class ProcessActivity extends AppCompatActivity {

    private ProcessActivityLayoutBinding binding;
    private final CacheHelper cache = CacheHelper.getInstance();
    private OSCoreHED osCore;
    private OSDialogActionSheet dropDialogAS;
    private Bitmap cropBmp;
    private Point[] cornerPts;

    private final List<List<TouchPickerItem>> effectAttrs = new ArrayList<>();
    private int rotate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ProcessActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        osCore = (OSCoreHED) cache.getData("hed");

        binding.getRoot().post(() -> {
            initUI();
            bindListeners();
        });
    }

    private void initUI() {
        cropBmp = (Bitmap) cache.getDataThenSweep("crop_bmp");
        cornerPts = (Point[]) cache.getDataThenSweep("corner_pts");

        List<TextView> texts = ItemPickerHelper.viewsFromStrings(
                this,
                new String[]{
                        getString(R.string.effect_none),
                        getString(R.string.effect_base),
                        getString(R.string.effect_grey),
                        getString(R.string.effect_extract)
                });
        for (TextView t : texts) {
            t.setTextColor(Color.WHITE);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            t.setTypeface(t.getTypeface(), Typeface.BOLD);
        }

        ItemPickerAdapter adapter = new ItemPickerAdapter(this, texts);
        adapter.setAroundMarginDp(4);
        binding.mItemPicker.setMaskColor(0xFF000000);
        binding.mItemPicker.setAdapter(adapter);

        effectAttrs.add(
                Collections.emptyList()
        );
        effectAttrs.add(
                Arrays.asList(
                        new TouchPickerItem(getString(R.string.effect_base_light),
                                20, 100, -100),
                        new TouchPickerItem(getString(R.string.effect_base_contrast),
                                20, 100, -100),
                        new TouchPickerItem(getString(R.string.effect_base_saturate),
                                50, 100, -100)
                )
        );
        effectAttrs.add(
                Collections.emptyList()
        );
        effectAttrs.add(
                Arrays.asList(
                        new TouchPickerItem(getString(R.string.effect_extract_intensity),
                                7, 10, 0),
                        new TouchPickerItem(getString(R.string.effect_extract_preserve),
                                3, 10, 0)
                )
        );

        dropDialogAS = new OSDialogActionSheet(getString(R.string.drop_dialog_title), "",
                OSDialogActionSheet.Const.DIALOG_CONFIRM_CANCEL_MODE,
                getString(R.string.dialog_confirm), getString(R.string.dialog_cancel));
        dropDialogAS.setCancelable(false);
    }

    private void bindListeners() {
        binding.mItemPicker.setOnCurrentIdxChangedListener((oldIdx, newIdx) -> {
            binding.mTouchPickerView.setItems(effectAttrs.get(newIdx));
        });

        binding.mTouchPickerView.setOnItemAttrChanged((newValue, percent, idx) -> {
            final List<TouchPickerItem> items = effectAttrs.get(binding.mItemPicker.getCurrentIdx());

            binding.attrProgressText.setVisibility(View.VISIBLE);

            binding.attrProgressText.setText(
                    String.format("%s %s", items.get(idx).getAttrName(), newValue)
            );
        });

        binding.mTouchPickerView.setOnAttrValueCommit(() -> {
            new Handler().post(() -> {

                final Bitmap procBmp;
                final int curEffectIdx = binding.mItemPicker.getCurrentIdx();

                binding.attrProgressText.setVisibility(View.INVISIBLE);

                if (curEffectIdx == 0) {
                    procBmp = cropBmp;
                } else if (curEffectIdx == 1) {
                    procBmp = osCore
                            .setRes(cropBmp)
                            .adjustContrastNBright(
                                    effectAttrs.get(1).get(1).getAttrValue() / 100f,
                                    effectAttrs.get(1).get(0).getAttrValue() / 100f
                            )
                            .colorCorrect()
                            .adjustSaturate(effectAttrs.get(1).get(2).getPercent() + 0.5f)
                            .getProcBmp();
                } else if (curEffectIdx == 2) {
                    procBmp = osCore
                            .setRes(cropBmp)
                            .grey()
                            .getProcBmp();
                } else {
                    procBmp = osCore
                            .setRes(cropBmp)
                            .removeBg((int) (effectAttrs.get(3).get(0).getPercent() * 30 + 5))
                            .binaryThruHist(effectAttrs.get(3).get(1).getAttrValue() / 100f)
                            .getProcBmp();
                }
                binding.processImgView.setImageBitmap(procBmp);

                Runtime.getRuntime().gc();
            });
        });

        binding.mItemPicker.setCurrentIdx(1);

        dropDialogAS.setOnClickListener(btn -> {
            if (btn == OSDialogActionSheet.Const.DIALOG_CONFIRM_BTN) {
                finish();
                CropActivity.outThis.finish();
            } else if (btn == OSDialogActionSheet.Const.DIALOG_CANCEL_BTN) {
                dropDialogAS.dismiss();
            }
        });

        binding.backScan.setOnClickListener(view -> {
            dropDialogAS.show(getSupportFragmentManager(), "dropDialogAS");
        });

        binding.backCrop.setOnClickListener(view -> {
            finish();
        });

        binding.rotateBtn.setOnClickListener(view -> {
            final Matrix rotateMat = new Matrix();
            rotateMat.setRotate(90);
            cropBmp = Bitmap.createBitmap(cropBmp, 0, 0,
                    cropBmp.getWidth(), cropBmp.getHeight(), rotateMat, true);

            Bitmap nowProcBmp = ((BitmapDrawable) binding.processImgView.getDrawable()).getBitmap();
            nowProcBmp = Bitmap.createBitmap(nowProcBmp, 0, 0,
                    nowProcBmp.getWidth(), nowProcBmp.getHeight(), rotateMat, true);
            binding.processImgView.setImageBitmap(nowProcBmp);

            rotate += 90;
            rotate = rotate > 270 ? 0 : rotate;
        });

        binding.saveBtn.setOnClickListener(view -> {

        });
    }

    @Override
    public void onBackPressed() {
        dropDialogAS.show(getSupportFragmentManager(), "dropDialogAS");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        binding.processImgView.setImageBitmap(null);
        cropBmp = null;

        Runtime.getRuntime().gc();
    }

}