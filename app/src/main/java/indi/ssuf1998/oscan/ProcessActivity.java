package indi.ssuf1998.oscan;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.core.Point;
import org.opencv.osgi.OpenCVNativeLoader;

import indi.ssuf1998.oscan.core.OSCoreHED;
import indi.ssuf1998.oscan.databinding.ProcessActivityLayoutBinding;


public class ProcessActivity extends AppCompatActivity {

    private ProcessActivityLayoutBinding binding;

    private final SharedBlock mBlock = SharedBlock.getInstance();
    private Bitmap resBmp;
    private Bitmap procBmp;

    private OSCoreHED osCoreHED;

    private Point[] cornerPts;

    static {
        new OpenCVNativeLoader().init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ProcessActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        osCoreHED = (OSCoreHED) mBlock.getData("hed", null);

        binding.getRoot().post(() -> {
            initUI();
        });

    }

    private void initUI() {
//        resBmp = (Bitmap) mBlock.getDataThenSweep("bmp");
//        binding.cropImgView.setImageBitmap(resBmp);

        cornerPts = new Point[]{
                new Point(64, 64),
                new Point(256, 64),
                new Point(384, 384),
                new Point(64, 256),
        };

        binding.cropImgView.setCornerPts(cornerPts);

        binding.cropImgView.setOnClickListener(view -> {
//            procBmp = osCoreHED
//                    .setResBmp(resBmp)
//                    .detect()
//                    .getProcBmp();

//            cornerPts = osCoreHED.getCornerPts();

//            osCoreHED.sweep();

//            binding.cropImgView.setImageBitmap(procBmp);


        });
    }


}