package indi.ssuf1998.oscan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.OrientationEventListener;
import android.view.Surface;

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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import indi.ssuf1998.oscan.databinding.ScanActivityLayoutBinding;

public class ScanActivity extends AppCompatActivity {

    private ScanActivityLayoutBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private CameraSelector cameraSelector;
    private ImageCapture capture;
    private Preview preview;
    private Camera camera;

    private final SparseIntArray flashModes = new SparseIntArray() {{
        append(ImageCapture.FLASH_MODE_AUTO,
                R.drawable.ic_fluent_flash_auto_24_pressed_selector);
        append(ImageCapture.FLASH_MODE_ON,
                R.drawable.ic_fluent_flash_on_24_pressed_selector);
        append(ImageCapture.FLASH_MODE_OFF,
                R.drawable.ic_fluent_flash_off_24_pressed_selector);
    }};
    int flashNowMode = ImageCapture.FLASH_MODE_AUTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ScanActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usesPermissions();

        binding.getRoot().post(() -> {
            initUI();
            bindListeners();
        });

    }

    private void initUI() {
        binding.takePicBtn.setTranslationY(-binding.takePicBtn.getHeight() * (1 / 3f));

    }

    private void bindListeners() {

    }

    private void usesPermissions() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    Const.CAMERA_PERMISSION_REQUEST);
        }
    }


    private void bindCameraListeners() {

        // 摄像头绑定监听
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                bindToCamera(cameraProviderFuture.get());
            } catch (Exception ignore) {
            }
        }, ContextCompat.getMainExecutor(this));

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

        binding.takePicBtn.setOnClickListener(view -> {
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
                            Log.e("take pic error", String.valueOf(exception.getImageCaptureError()));
                        }
                    }
            );
        });
    }

    private void bindToCamera(@NonNull ProcessCameraProvider cameraProvider) {
        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();

        capture = new ImageCapture.Builder()
                .build();

        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
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
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, capture);

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