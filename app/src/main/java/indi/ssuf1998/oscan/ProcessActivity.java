package indi.ssuf1998.oscan;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import indi.ssuf1998.itempicker.ItemPickerAdapter;
import indi.ssuf1998.itempicker.ItemPickerHelper;
import indi.ssuf1998.oscan.databinding.ProcessActivityLayoutBinding;

public class ProcessActivity extends AppCompatActivity {

    private ProcessActivityLayoutBinding binding;
//    private final SharedBlock mBlock = SharedBlock.getInstance();
//    private OSCoreHED osCore;
//    private Bitmap cropBmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ProcessActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        osCore = (OSCoreHED) mBlock.getData("hed", null);

        binding.getRoot().post(() -> {
            initUI();
        });

    }

    private void initUI() {

//        cropBmp = (Bitmap) mBlock.getDataThenSweep("crop_bmp");
//        binding.processImgView.setImageBitmap(cropBmp);
//        cropBmp = ((BitmapDrawable) binding.processImgView.getDrawable()).getBitmap();

//        binding.processImgView.setOnClickListener(view -> {
//            final Bitmap procBmp = osCore
//                    .setRes(cropBmp)
//                    .removeBg(25)
//                    .binaryThruHist(0.05f)
//                    .getProcBmp();
//            binding.processImgView.setImageBitmap(procBmp);
//
//        });

        ItemPickerAdapter adapter = new ItemPickerAdapter(
                this,
                ItemPickerHelper.viewsFromStrings(this, new String[]{
                        "无", "默认", "去除背景"
                })
        );
        adapter.setAroundMarginDp(4);
        binding.mItemPicker.setAdapter(adapter);
        binding.mItemPicker.setOnCurrentIdxChangedListener((oldIdx, newIdx) -> {
            Log.d("osdebug", String.format("old: %s, new: %s", oldIdx, newIdx));
        });

    }

}