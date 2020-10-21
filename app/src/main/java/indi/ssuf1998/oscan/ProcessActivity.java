package indi.ssuf1998.oscan;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.core.Point;

import java.util.Arrays;

import es.dmoral.toasty.Toasty;
import indi.ssuf1998.oscan.core.OSCoreHED;
import indi.ssuf1998.oscan.databinding.ProcessActivityLayoutBinding;


public class ProcessActivity extends AppCompatActivity {

    private ProcessActivityLayoutBinding binding;

    private final SharedBlock mBlock = SharedBlock.getInstance();
    private Bitmap resBmp;
    private OSCoreHED osCoreHED;
    private Point[] cornerPts;

    private boolean pressBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        osCoreHED = (OSCoreHED) mBlock.getData("hed", null);

        binding = ProcessActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.getRoot().post(() -> {
            initUI();
            initListeners();
            runDetect();
        });
    }

    private void initUI() {
        binding.cropImgView.setVisibility(View.VISIBLE);

        resBmp = (Bitmap) mBlock.getDataThenSweep("bmp");
        binding.cropImgView.setImageBitmap(resBmp);
    }

    private void initListeners() {
        binding.confirmTextViewBtn.setOnClickListener(view -> {
            binding.cropImgView.setImageBitmap(osCoreHED
                    .clipThenTransform(binding.cropImgView.getCornerPts())
                    .getProcBmp()
            );
        });
        binding.cancelTextViewBtn.setOnClickListener(view -> onBackPressed());

        binding.cropImgView.addBadCornerPtsListener(() -> {
            final Toast toast = Toasty.warning(
                    ProcessActivity.this,
                    getString(R.string.bad_pts_tip),
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        });
    }

    private void runDetect() {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        pressBack = true;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (pressBack) {
            osCoreHED.sweep();
            binding.cropImgView.setVisibility(View.INVISIBLE);
            binding.cropImgView.setImageBitmap(null);
            pressBack = false;
        }

    }
}