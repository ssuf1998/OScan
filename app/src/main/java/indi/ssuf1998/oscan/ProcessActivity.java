package indi.ssuf1998.oscan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import indi.ssuf1998.oscan.adapter.StrItemPickerAdapter;
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

        StrItemPickerAdapter adapter = new StrItemPickerAdapter(
                this,
                new String[]{
                        "无", "默认", "去除背景"
                });
        adapter.setAroundMarginDp(4);
        LinearLayoutManager layoutMgr = new LinearLayoutManager(this);
        layoutMgr.setOrientation(RecyclerView.HORIZONTAL);
        binding.strItemPicker.setLayoutManager(layoutMgr);
        binding.strItemPicker.setAdapter(adapter);
        binding.strItemPicker.setOnCurrentIdxChangedListener((oldIdx, newIdx) -> {
            Log.d("osdebug", String.format("old: %s, new: %s", oldIdx, newIdx));
        });


    }

}