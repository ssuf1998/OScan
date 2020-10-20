package indi.ssuf1998.oscan;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.core.Point;
import org.opencv.osgi.OpenCVNativeLoader;

import indi.ssuf1998.oscan.core.OSCoreHED;
import indi.ssuf1998.oscan.databinding.ProcessActivityLayoutBinding;


public class ProcessActivity extends AppCompatActivity {

    private ProcessActivityLayoutBinding binding;

    private final SharedBlock mBlock = SharedBlock.getInstance();
    private Bitmap resBmp;

    private OSCoreHED osCoreHED;

    private Point[] cornerPts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ProcessActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        osCoreHED = (OSCoreHED) mBlock.getData("hed", null);

        binding.getRoot().post(() -> {
            initUI();
            initListeners();
        });

    }

    private void initUI() {
        binding.cropImgView.setVisibility(View.VISIBLE);

        resBmp = (Bitmap) mBlock.getDataThenSweep("bmp");
        binding.cropImgView.setImageBitmap(resBmp);

        new Thread(() -> {
            cornerPts = osCoreHED
                    .setRes(resBmp)
                    .detect()
                    .getCornerPts();
            binding.cropImgView.setCornerPts(cornerPts);

            runOnUiThread(() -> {
                final ObjectAnimator animator =
                        ObjectAnimator.ofFloat(binding.loadingView, "alpha",
                                1f, 0f);
                animator.setDuration(500);
                animator.start();
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.loadingView.setVisibility(View.GONE);
                    }
                });
            });

        }).start();

    }

    private void initListeners() {
        binding.confirmTextViewBtn.setOnClickListener(view -> {
            binding.cropImgView.setImageBitmap(osCoreHED
                    .clipThenTransform(binding.cropImgView.getCornerPts())
                    .getProcBmp()
            );
        });
        binding.cancelTextViewBtn.setOnClickListener(view-> finish());
    }

    @Override
    protected void onStop() {
        super.onStop();

        osCoreHED.sweep();
        binding.cropImgView.setVisibility(View.INVISIBLE);
        binding.cropImgView.setImageBitmap(null);

    }
}