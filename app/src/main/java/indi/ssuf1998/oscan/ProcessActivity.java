package indi.ssuf1998.oscan;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import indi.ssuf1998.oscan.core.OSCoreHED;
import indi.ssuf1998.oscan.databinding.ProcessActivityLayoutBinding;


public class ProcessActivity extends AppCompatActivity {

    private ProcessActivityLayoutBinding binding;

    private final SharedBlock mBlock = SharedBlock.getInstance();
    private Bitmap resBmp;
    private Bitmap procBmp;

    private OSCoreHED osCoreHED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ProcessActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        osCoreHED = (OSCoreHED) mBlock.getData("hed");

        binding.getRoot().post(() -> {
            initUI();
        });

    }

    private void initUI() {
        resBmp = (Bitmap) mBlock.getDataThenSweep("bmp");
        binding.cropImgView.setImageBitmap(resBmp);

        binding.cropImgView.setOnClickListener(view -> {
            procBmp = osCoreHED
                    .setResBmp(resBmp)
                    .detect()
                    .clipThenTransform()
                    .getProcBmp();

            binding.cropImgView.setImageBitmap(procBmp);
        });
    }


}