package indi.ssuf1998.oscan;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.core.Point;

import es.dmoral.toasty.Toasty;
import indi.ssuf1998.oscan.core.OSCoreHED;
import indi.ssuf1998.oscan.databinding.CropActivityLayoutBinding;


public class CropActivity extends AppCompatActivity {

    private CropActivityLayoutBinding binding;

    private final SharedBlock mBlock = SharedBlock.getInstance();
    private Bitmap scanBmp;
    private OSCoreHED osCore;
    private Point[] cornerPts;

    private boolean pressBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        osCore = (OSCoreHED) mBlock.getData("hed", null);

        binding = CropActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.getRoot().post(() -> {
            initUI();
            bindListeners();
            runDetect();
        });
    }

    private void initUI() {
        binding.cropImgView.setVisibility(View.VISIBLE);

        scanBmp = (Bitmap) mBlock.getDataThenSweep("scan_bmp");
        binding.cropImgView.setImageBitmap(scanBmp);
    }

    private void bindListeners() {
        binding.confirmTextViewBtn.setOnClickListener(view -> {
            final Bitmap bmp = osCore
                    .clipThenTransform(binding.cropImgView.getCornerPts())
                    .getProcBmp();
            giveBmp2Process(bmp);
        });
        binding.cancelTextViewBtn.setOnClickListener(view -> onBackPressed());

        binding.cropImgView.setOnBadCornerPtsListener(() -> {
            final Toast toast = Toasty.warning(
                    CropActivity.this,
                    getString(R.string.bad_pts_tip),
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        });
    }

    private void runDetect() {
        new Thread(() -> {
            cornerPts = osCore
                    .setRes(scanBmp)
                    .detect()
                    .getCornerPts();
            binding.cropImgView.setCornerPts(cornerPts);
            osCore.sweep();

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

    private void giveBmp2Process(Bitmap bmp) {
        mBlock.putData("crop_bmp", bmp);
        this.startActivity(new Intent(this, ProcessActivity.class));
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
            binding.cropImgView.setVisibility(View.INVISIBLE);
            binding.cropImgView.setImageBitmap(null);
            pressBack = false;
//            虽然好像没什么大用……
//            Runtime.getRuntime().gc();
        }
    }
}