package indi.ssuf1998.oscan;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.core.Point;

import java.util.Optional;

import es.dmoral.toasty.Toasty;
import indi.ssuf1998.oscan.core.OSCoreHED;
import indi.ssuf1998.oscan.core.Utils;
import indi.ssuf1998.oscan.databinding.CropActivityLayoutBinding;


public class CropActivity extends AppCompatActivity {

    private CropActivityLayoutBinding binding;

    private final CacheHelper cache = CacheHelper.getInstance();
    private Bitmap scanBmpThumb;
    private OSCoreHED osCore;

    private float thumbMultiple;

    public static CropActivity outThis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        osCore = (OSCoreHED) cache.getData("hed");

        binding = CropActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.getRoot().post(() -> {
            initUI();
            bindListeners();
            runDetect();
        });

        outThis = this;
    }

    private void initUI() {
        final Bitmap scanBmp = Optional
                .ofNullable((Bitmap) cache.getData("scan_bmp"))
                .orElse((Bitmap) cache.getBmpFromCache("scan_bmp_cache", this))
                .copy(Bitmap.Config.RGB_565, true);
        final double[] thumbSize = Utils.getAspectSize(scanBmp.getWidth(), scanBmp.getHeight(), 1536);
        thumbMultiple = scanBmp.getWidth() / (float) thumbSize[0];
        scanBmpThumb = Bitmap.createScaledBitmap(scanBmp, (int) thumbSize[0], (int) thumbSize[1], true);

        binding.cropImgView.setImageBitmap(scanBmpThumb);

        new Thread(() -> {
            cache.putBmpIntoCache("scan_bmp_cache", scanBmp, this);
            cache.removeData("scan_bmp");
            Runtime.getRuntime().gc();
        }).start();
    }

    private void bindListeners() {
        binding.confirmTextViewBtn.setOnClickListener(view -> {
            final Point[] cornerPts = binding.cropImgView.getCornerPts();

            final Bitmap bmp = osCore
                    .setRes(scanBmpThumb)
                    .clipThenTransform(cornerPts)
                    .getProcBmp();

            for (Point p : cornerPts) {
                p.x *= thumbMultiple;
                p.y *= thumbMultiple;
            }

            giveBmp2Process(bmp, cornerPts);
        });
        binding.cancelTextViewBtn.setOnClickListener(view -> finish());

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
        new Handler().post(() -> {
            binding.cropImgView.setCornerPts(
                    osCore
                            .setRes(scanBmpThumb)
                            .detect()
                            .getCornerPts()
            );

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
    }

    private void giveBmp2Process(Bitmap bmp, Point[] cornerPts) {
        cache.putData("crop_bmp", bmp);
        cache.putData("corner_pts", cornerPts);
        this.startActivity(new Intent(this, ProcessActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        osCore.sweep();
        binding.cropImgView.setImageBitmap(null);
        scanBmpThumb = null;
//            虽然好像没什么大用……
        Runtime.getRuntime().gc();
    }
}