package indi.ssuf1998.oscan;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.DisplayCutout;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.osgi.OpenCVNativeLoader;

import indi.ssuf1998.oscan.core.OSCore;
import indi.ssuf1998.oscan.databinding.ScanActivityMainBinding;

public class ScanActivity extends AppCompatActivity {

    private ScanActivityMainBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    CameraSelector cameraSelector;
    private ImageCapture capture;
    private Preview preview;
    private Camera camera;

    private OSCore osCore = new OSCore();

    private final SparseIntArray flashModes = new SparseIntArray() {{
        append(ImageCapture.FLASH_MODE_AUTO, R.drawable.ic_round_flash_auto_24);
        append(ImageCapture.FLASH_MODE_ON, R.drawable.ic_round_flash_on_24);
        append(ImageCapture.FLASH_MODE_OFF, R.drawable.ic_round_flash_off_24);
    }};
    int flashNowMode = ImageCapture.FLASH_MODE_AUTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new OpenCVNativeLoader().init();

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
        }

        binding = ScanActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Rect rect = new Rect();
        binding.getRoot().getRootView().getWindowVisibleDisplayFrame(rect);
        double visibleWinWidth = (double) rect.width();
        double visibleWinHeight = (double) rect.height();
        double previewViewHeight = visibleWinWidth / 3 * 4;
        double remainHeight = visibleWinHeight - previewViewHeight;
        double topActionBatHeight = remainHeight * 0.43;
        double bottomActionBatHeight = remainHeight - topActionBatHeight;

        ConstraintLayout.LayoutParams tabLp = (ConstraintLayout.LayoutParams) binding.topActionBar.getLayoutParams();
        tabLp.height = Math.max(binding.topActionBar.getMinHeight(), (int) topActionBatHeight);
        binding.topActionBar.setLayoutParams(tabLp);
        ConstraintLayout.LayoutParams babLp = (ConstraintLayout.LayoutParams) binding.bottomActionBar.getLayoutParams();
        babLp.height = Math.max(binding.bottomActionBar.getMinHeight(), (int) bottomActionBatHeight);
        binding.bottomActionBar.setLayoutParams(babLp);


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


        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                bindToCamera(cameraProviderFuture.get());
            } catch (Exception ignore) {
            }
        }, ContextCompat.getMainExecutor(this));

        binding.takePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capture.takePicture(
                        ContextCompat.getMainExecutor(ScanActivity.this),
                        new ImageCapture.OnImageCapturedCallback() {
                            @Override
                            public void onCaptureSuccess(@NonNull ImageProxy image) {
                                Bitmap bmp = Utils.imgProxy2Bitmap(image);

                                binding.picView.setImageBitmap(bmp);
                                binding.topActionBar.setVisibility(View.GONE);
                                binding.bottomActionBar.setVisibility(View.GONE);
                                binding.previewView.setVisibility(View.GONE);
                                binding.picView.setVisibility(View.VISIBLE);

                                image.close();

                                try {
                                    cameraProviderFuture.get().unbindAll();
                                } catch (Exception ignore) {
                                }

                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                super.onError(exception);
                            }
                        }
                );
            }
        });

        binding.picView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    bindToCamera(cameraProviderFuture.get());
                } catch (Exception ignore) {
                }

                binding.topActionBar.setVisibility(View.VISIBLE);
                binding.bottomActionBar.setVisibility(View.VISIBLE);
                binding.previewView.setVisibility(View.VISIBLE);
                binding.picView.setVisibility(View.GONE);
                binding.picView.setImageBitmap(null);

            }
        });

        binding.switchFlashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    flashNowMode = flashNowMode + 1 >= flashModes.size() ? 0 : flashNowMode + 1;
                    cameraProviderFuture.get().unbind(capture);
                    capture.setFlashMode(flashModes.keyAt(flashNowMode));
                    cameraProviderFuture.get().bindToLifecycle(ScanActivity.this, cameraSelector, capture);
                    binding.switchFlashBtn.setImageDrawable(ContextCompat.getDrawable(
                            ScanActivity.this,
                            flashModes.get(flashNowMode)));
                } catch (Exception ignore) {
                }


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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 100) {
                Uri uri = data.getData();
                binding.picView.setImageURI(uri);
            }
        }

    }
}