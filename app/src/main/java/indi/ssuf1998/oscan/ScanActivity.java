package indi.ssuf1998.oscan;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import indi.ssuf1998.oscan.databinding.ScanActivityMainBinding;

public class ScanActivity extends AppCompatActivity {

    private ScanActivityMainBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    CameraSelector cameraSelector;
    private ImageCapture capture;
    private Preview preview;
    private Camera camera;

    private final SparseIntArray flashModes = new SparseIntArray() {{
        append(ImageCapture.FLASH_MODE_AUTO, R.drawable.ic_round_flash_auto_24);
        append(ImageCapture.FLASH_MODE_ON, R.drawable.ic_round_flash_on_24);
        append(ImageCapture.FLASH_MODE_OFF, R.drawable.ic_round_flash_off_24);
    }};
    int flashNowMode = ImageCapture.FLASH_MODE_AUTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usesPermissions();

        binding = ScanActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bindNormalListeners();

//        binding.pickImgBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "选择照片"),
//                        100);
//            }
//        });

//        binding.applyBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                long s = new Date().getTime();
//
//                osCore.setResDrawable(binding.imgView.getDrawable())
//                        .grey()
//                        .reverseColor()
//                        .adaptiveLightThreshold(9, false)
//                        .computeBiggestRectPts()
//                        .computeCornerPts();
//
////                Log.d("osdebug", String.valueOf(new Date().getTime() - s));
////                Bitmap bmp = osCore.getProcBmp();
////                binding.imgView.setImageBitmap(bmp);
//            }
//        });

    }

    private void usesPermissions() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    Const.CAMERA_PERMISSION_REQUEST);
        }
    }

    private void bindNormalListeners() {
        binding.backBtn.setOnClickListener(view -> finish());

    }


    @SuppressLint("ClickableViewAccessibility")
    private void bindCameraListeners() {

        // 摄像头绑定监听
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                bindToCamera(cameraProviderFuture.get());
            } catch (Exception ignore) {
            }
        }, ContextCompat.getMainExecutor(this));

        // 切换闪光灯模式按钮
        binding.switchFlashBtn.setOnClickListener(view -> {
            try {
                flashNowMode = flashNowMode + 1 >= flashModes.size() ? 0 : flashNowMode + 1;
                capture.setFlashMode(flashModes.keyAt(flashNowMode));
                binding.switchFlashBtn.setImageDrawable(ContextCompat.getDrawable(
                        ScanActivity.this,
                        flashModes.get(flashNowMode)));
            } catch (Exception ignore) {
            }
        });

        final ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(
                binding.takePicBtn,
                "scaleX", 1f, 0.85f);
        final ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(
                binding.takePicBtn,
                "scaleY", 1f, 0.85f);
        final AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleXAnim).with(scaleYAnim);
        animSet.setInterpolator(new DecelerateInterpolator());

        binding.takePicBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    scaleXAnim.setFloatValues(binding.takePicBtn.getScaleX(), 1f);
                    scaleYAnim.setFloatValues(binding.takePicBtn.getScaleY(), 1f);
                    animSet.setDuration(50);
                    animSet.start();
                    capture.takePicture(
                            ContextCompat.getMainExecutor(ScanActivity.this),
                            new ImageCapture.OnImageCapturedCallback() {
                                @Override
                                public void onCaptureSuccess(@NonNull ImageProxy image) {
                                    Bitmap bmp = Utils.imgProxy2Bitmap(image);
                                    image.close();
                                }

                                @Override
                                public void onError(@NonNull ImageCaptureException exception) {
                                    super.onError(exception);
                                }
                            }
                    );
                } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    scaleXAnim.setFloatValues(binding.takePicBtn.getScaleX(), 0.85f);
                    scaleYAnim.setFloatValues(binding.takePicBtn.getScaleY(), 0.85f);
                    animSet.setDuration(150);
                    animSet.start();
                }
                return true;
            }
        });
    }

    private void bindToCamera(@NonNull ProcessCameraProvider cameraProvider) {
        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();

        capture = new ImageCapture.Builder()
                .setFlashMode(flashNowMode)
                .build();

        OrientationEventListener orientationEventListener = new OrientationEventListener((Context) this) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation;

                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;
                }

                capture.setTargetRotation(rotation);
            }
        };

        orientationEventListener.enable();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, capture, preview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Const.CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bindCameraListeners();
            }
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
////        if (resultCode == RESULT_OK && data != null) {
////            if (requestCode == 100) {
////                Uri uri = data.getData();
////                binding.picView.setImageURI(uri);
////            }
////        }
//    }
}